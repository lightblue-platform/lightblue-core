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
package com.redhat.lightblue.eval;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.FieldAccess;
import com.redhat.lightblue.metadata.EntityAccess;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.Access;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

public final class FieldAccessRoleEvaluator {
    private final EntityMetadata md;
    private final Set<String> roles;

    public static enum Operation {
        insert, update, insert_and_update, find
    };

    public FieldAccessRoleEvaluator(EntityMetadata md, Set<String> callerRoles) {
        this.md = md;
        this.roles = callerRoles;
    }

    /**
     * Returns whether the current caller has access to all the given fields based on the operation
     */
    public boolean hasAccess(Set<Path> fields, Operation op) {
        for (Path x : fields) {
            if (!hasAccess(x, op)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the current caller has access to the given field based on the operation
     */
    public boolean hasAccess(Path field, Operation op) {
        FieldTreeNode fn = md.resolve(field);
        if (fn != null) {
            if (fn instanceof Field) {
                return hasAccess((Field) fn, op);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns a set of fields that are inaccessible to the user for the given operation
     */
    public Set<Path> getInaccessibleFields(Operation op) {
        FieldCursor cursor = md.getFieldCursor();
        Set<Path> fields = new HashSet<>();
        while (cursor.next()) {
            FieldTreeNode fn = cursor.getCurrentNode();
            if (fn instanceof Field && !hasAccess((Field) fn, op)) {
                fields.add(cursor.getCurrentPath());
            }
        }
        return fields;
    }

    /**
     * Returns a list of fields in the doc inaccessible to the current user during insertion. If the returned list is
     * empty, the user can insert the doc.
     */
    public List<Path> getInaccessibleFields_Insert(JsonDoc doc) {
        Set<Path> inaccessibleFields = getInaccessibleFields(Operation.insert);
        List<Path> ret = new ArrayList<>(inaccessibleFields.size());
        for (Path x : inaccessibleFields) {
            KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(x);
            if (cursor.hasNext()) {
                ret.add(x);
            }
        }
        return ret;
    }

    /**
     * Returns a list of fields in the doc inaccessible to the current user during update.
     *
     * @param newDoc The new version of the document
     * @param oldDoc The old version of the document
     */
    public List<Path> getInaccessibleFields_Update(JsonDoc newDoc, JsonDoc oldDoc) {
        Set<Path> inaccessibleFields = getInaccessibleFields(Operation.update);
        List<Path> ret = new ArrayList<>(inaccessibleFields.size());
        for (Path x : inaccessibleFields) {
            KeyValueCursor<Path, JsonNode> oldCursor = oldDoc.getAllNodes(x);
            KeyValueCursor<Path, JsonNode> newCursor = newDoc.getAllNodes(x);
            if (different(oldCursor, newCursor)) {
                ret.add(x);
            }
        }
        return ret;
    }

    /**
     * Returns a projection that excludes the fields the caller does not have access to based on the operation
     */
    public Projection getExcludedFields(Operation op) {
        Set<Path> inaccessibleFields = getInaccessibleFields(op);
        Projection ret;
        if (inaccessibleFields.isEmpty()) {
            ret = null;
        } else {
            if (inaccessibleFields.size() == 1) {
                ret = new FieldProjection(inaccessibleFields.iterator().next(), false, true);
            } else {
                List<Projection> list = new ArrayList<>(inaccessibleFields.size());
                for (Path x : inaccessibleFields) {
                    list.add(new FieldProjection(x, false, true));
                }
                ret = new ProjectionList(list);
            }
        }
        return ret;
    }

    private static abstract class AccAccessor {
        public abstract Access getFieldAccess(FieldAccess f);
    }

    private static final AccAccessor INS_ACC=new AccAccessor() {
            public Access getFieldAccess(FieldAccess f) {
                return f.getInsert();
            }
        };

    private static final AccAccessor UPD_ACC=new AccAccessor() {
            public Access getFieldAccess(FieldAccess f) {
                return f.getUpdate();
            }
        };

    private static final AccAccessor FIND_ACC=new AccAccessor() {
            public Access getFieldAccess(FieldAccess f) {
                return f.getFind();
            }
        };


    private Access getEffAccess(Field f,AccAccessor acc,Access entityAccess) {
        Access access=acc.getFieldAccess(f.getAccess());
        if(access.isEmpty()) {
            FieldTreeNode trc=f;
            do {
                trc=trc.getParent();
                if(trc!=null) {
                    if(trc instanceof Field) {
                        access=acc.getFieldAccess(((Field)trc).getAccess());
                        if(!access.isEmpty()) {
                            break;
                        }
                    }
                }
            }  while(trc!=null);
        }
        if(access.isEmpty()) {
            access=entityAccess;
        }
        return access;
    }
                
                
        

    private boolean hasAccess(Field f, Operation op) {
        EntityAccess eaccess = md.getAccess();
        switch (op) {
        case insert:
            return getEffAccess(f,INS_ACC,eaccess.getInsert()).hasAccess(roles);
        case update:
            return getEffAccess(f,UPD_ACC,eaccess.getUpdate()).hasAccess(roles);
        case insert_and_update:
            return getEffAccess(f,INS_ACC,eaccess.getInsert()).hasAccess(roles)
                && getEffAccess(f,UPD_ACC,eaccess.getUpdate()).hasAccess(roles);
        case find:
            return getEffAccess(f,FIND_ACC,eaccess.getFind()).hasAccess(roles);
        }
        return false;
    }

    private boolean different(KeyValueCursor<Path, JsonNode> c1,
                              KeyValueCursor<Path, JsonNode> c2) {
        while (c1.hasNext()) {
            if (c2.hasNext()) {
                c1.next();
                c2.next();
                JsonNode v1 = c1.getCurrentValue();
                JsonNode v2 = c2.getCurrentValue();
                if (!v1.equals(v2)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return c2.hasNext();
    }
}
