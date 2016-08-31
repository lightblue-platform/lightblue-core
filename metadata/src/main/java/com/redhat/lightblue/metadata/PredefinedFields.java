/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Ensures that the predefined fields are included in the metadata.
 *
 * <ul>
 * <li> _id, type is string, int or biginteger. Unique constraint, roles setup
 * to allow read by anyone, noone updates</li>
 * <li> objectType, type is string. required and minimum length=1, roles setup
 * to allow read by anyone, noone updates</li>
 * <li> for every array field with name "x", a field "x#" of type int, roles
 * setup to allow read by anyone, noone updates</li>
 * </ul>
 */
public final class PredefinedFields {

    public static final String OBJECTTYPE_FIELD = "objectType";

    public static final Path OBJECTTYPE_PATH = new Path(OBJECTTYPE_FIELD);

    public static void ensurePredefinedFields(EntityMetadata md) {
        ensureObjectType(md);
        // Recursively find all arrays, and add array size fields
        List<ParentNewChild> l = new ArrayList<>();
        // We have to go through all the array fields, and queue up
        // the new size fields to add, otherwise the following loop
        // throws a concurrent modification exception
        FieldCursor cursor = md.getFieldCursor();
        while (cursor.next()) {
            FieldTreeNode f = cursor.getCurrentNode();
            if (f instanceof ArrayField) {
                ParentNewChild x = ensureArraySize(md, (ArrayField) f);
                if (x != null) {
                    l.add(x);
                }
            }
        }
        for (ParentNewChild x : l) {
            x.parent.addNew(x.newChild);
        }
    }

    /**
     * Updates all array size values in the given document
     */
    public static void updateArraySizes(EntityMetadata md, JsonNodeFactory factory, JsonDoc doc) {
        FieldCursor cursor = md.getFieldCursor();
        while (cursor.next()) {
            FieldTreeNode f = cursor.getCurrentNode();
            if (f.getName().endsWith("#")) {
                Path lengthField = cursor.getCurrentPath();
                String ls = lengthField.toString();
                Path arrField = new Path(ls.substring(0, ls.length() - 1));
                try {
                    if (md.resolve(arrField) != null) {
                        JsonNode arrNode = doc.get(arrField);
                        if (arrNode == null || arrNode instanceof NullNode) {
                            doc.modify(lengthField, factory.numberNode(0), false);
                        } else {
                            doc.modify(lengthField, factory.numberNode(arrNode.size()), false);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private static void ensureObjectType(EntityMetadata md) {
        Field f = md.getFields().getField(OBJECTTYPE_FIELD);
        if (f == null) {
            f = new SimpleField(OBJECTTYPE_FIELD, StringType.TYPE);
            md.getFields().addNew(f);
        }

        // Object type must be string
        if (f instanceof SimpleField
                && f.getType().equals(StringType.TYPE)) {
            // Required constraint
            if (findConstraint(f.getConstraints(), new ConstraintSearchCB<FieldConstraint>() {
                @Override
                public boolean checkMatch(FieldConstraint c) {
                    return c instanceof RequiredConstraint;
                }
            }) == null) {
                f.setConstraints(addConstraint(f.getConstraints(), new RequiredConstraint()));
            }
            // Can't be empty
            if (findConstraint(f.getConstraints(), new ConstraintSearchCB<FieldConstraint>() {
                @Override
                public boolean checkMatch(FieldConstraint c) {
                    if (c instanceof StringLengthConstraint && ((StringLengthConstraint) c).getType().equals(StringLengthConstraint.MINLENGTH)) {
                        return true;
                    }
                    return false;
                }
            }) == null) {
                f.setConstraints(addConstraint(f.getConstraints(), new StringLengthConstraint(StringLengthConstraint.MINLENGTH, 1)));
            }
            setRoleIfEmpty(f.getAccess().getFind(), MetadataConstants.ROLE_ANYONE);
            setRoleIfEmpty(f.getAccess().getUpdate(), MetadataConstants.ROLE_NOONE);
        } else {
            throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, OBJECTTYPE_FIELD + ":" + f.getType().getName());
        }
    }

    private static void setRoleIfEmpty(Access access, String role) {
        if (access.getRoles().isEmpty()) {
            List<String> l = new ArrayList<>(1);
            l.add(role);
            access.setRoles(l);
        }
    }

    private interface ConstraintSearchCB<T> {
        boolean checkMatch(T c);
    }

    private static <T> T findConstraint(List<T> list, ConstraintSearchCB<T> cb) {
        if (list != null) {
            for (T x : list) {
                if (cb.checkMatch(x)) {
                    return x;
                }
            }
        }
        return null;
    }

    private static <T> List<T> addConstraint(List<T> constraints, T newConstraint) {
        List<T> ret = constraints == null ? new ArrayList<T>(1) : constraints;
        ret.add(newConstraint);
        return ret;
    }

    private static final class ParentNewChild {
        private final Fields parent;
        private final Field newChild;

        public ParentNewChild(Fields parent, Field newChild) {
            this.parent = parent;
            this.newChild = newChild;
        }
    }

    private static ParentNewChild ensureArraySize(EntityMetadata md, ArrayField arr) {
        // Get the parent. The parent is either an object field, object element, or the root
        FieldTreeNode parent = arr.getParent();
        Fields fields;
        if (parent instanceof ObjectField) {
            fields = ((ObjectField) parent).getFields();
        } else if (parent instanceof ObjectArrayElement) {
            fields = ((ObjectArrayElement) parent).getFields();
        } else {
            fields = md.getFields();
        }
        String fieldName = arr.getName() + "#";
        Field f = fields.getField(fieldName);
        ParentNewChild ret;
        if (f == null) {
            f = new SimpleField(fieldName, IntegerType.TYPE);
            /*
             * If array has Find roles on it, then also set on count field.
             * Other roles should not be copied over as they may cause problems
             * with save operations.
             */
            f.getAccess().getFind().setRoles(arr.getAccess().getFind());
            ret = new ParentNewChild(fields, f);
        } else {
            ret = null;
        }

        // Must be int
        if (f instanceof SimpleField
                && f.getType().equals(IntegerType.TYPE)) {
            setRoleIfEmpty(f.getAccess().getFind(), MetadataConstants.ROLE_ANYONE);
        } else {
            throw Error.get(MetadataConstants.ERR_FIELD_WRONG_TYPE, fieldName + ":" + f.getType().getName());
        }
        return ret;
    }

    private PredefinedFields() {
    }
}
