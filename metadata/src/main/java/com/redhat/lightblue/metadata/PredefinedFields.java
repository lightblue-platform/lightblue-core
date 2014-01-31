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

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.BigIntegerType;

import com.redhat.lightblue.metadata.constraints.UniqueConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Ensures that the predefined fields are included in the metadata.
 *
 * <ul>
 * <li> _id, type is string, int or biginteger. Unique constraint, roles setup to allow read by anyone, noone updates</li?
 * <li> object_type, type is string. required and minimum length=1, roles setup to allow read by anyone, noone updates</li>
 * <li> for every array field with name "x", a field "x#" of type int, roles setup to allow read by anyone, noone updates</li>
 * </ul>
 */
public final class PredefinedFields {

    public static final String ERR_FIELD_WRONG_TYPE="FIELD_WRONG_TYPE";

    public static final String ID_FIELD="_id";
    public static final String OBJECTTYPE_FIELD="object_type";

    public static final Path ID_PATH=new Path(ID_FIELD);
    public static final Path OBJECTTYPE_PATH=new Path(OBJECTTYPE_FIELD);

    public static void ensurePredefinedFields(EntityMetadata md) {
        ensureID(md);
        ensureObjectType(md);
        // Recursively find all arrays, and add array size fields
        List<ParentNewChild> l=new ArrayList<ParentNewChild>();
        // We have to go through all the array fields, and queue up
        // the new size fields to add, otherwise the following loop
        // throws a concurrent modification exception
        FieldCursor cursor=md.getFieldCursor();
        while(cursor.next()) {
            FieldTreeNode f=cursor.getCurrentNode();
            if(f instanceof ArrayField) {
                ParentNewChild x=ensureArraySize(md,(ArrayField)f);
                if(x!=null)
                    l.add(x);
            }
        }
        for(ParentNewChild x:l)
            x.parent.addNew(x.newChild);
    }


    /**
     * Creates, or makes sure that there exists an ID field with:
     *  - name is _id
     *  - type string, int, or biginteger
     *  - unique constraint
     *  - access roles to allow read by anyone, and allow update by noone
     */
    private static void ensureID(EntityMetadata md) {
        Field f=md.getFields().getField(ID_FIELD);
        if(f==null)
            md.getFields().addNew(f=new SimpleField(ID_FIELD,StringType.TYPE));
        if(f instanceof SimpleField &&
           // ID can be string, int, bigint
           (f.getType().equals(IntegerType.TYPE)||
            f.getType().equals(StringType.TYPE)||
            f.getType().equals(BigIntegerType.TYPE))) {
            // Need a unique constraint for ID
            if(findConstraint(EntityConstraint.class,
                              md.getConstraints(),
                              new ConstraintSearchCB<EntityConstraint>() {
                                  public boolean checkMatch(EntityConstraint c) {
                                      if(c instanceof UniqueConstraint) {
                                          List<Path> fields=((UniqueConstraint)c).getFields();
                                          if(fields.size()==1&&fields.get(0).equals(ID_PATH))
                                              return true;
                                      }
                                      return false;
                                  }
                              })==null) {
                md.setConstraints(addConstraint(md.getConstraints(),new UniqueConstraint(ID_PATH)));
            }
            setRoleIfEmpty(f.getAccess().getFind(),Constants.ROLE_ANYONE);
            setRoleIfEmpty(f.getAccess().getUpdate(),Constants.ROLE_NOONE);
        } else {
            throw Error.get(ERR_FIELD_WRONG_TYPE,ID_FIELD+":"+f.getType().getName());
        }
    }

    private static void ensureObjectType(EntityMetadata md) {
        Field f=md.getFields().getField(OBJECTTYPE_FIELD);
        if(f==null)
            md.getFields().addNew(f=new SimpleField(OBJECTTYPE_FIELD,StringType.TYPE));
        if(f instanceof SimpleField&&
           // Object type must be string
           f.getType().equals(StringType.TYPE)) {
            // Required constraint
            if(findConstraint(FieldConstraint.class,f.getConstraints(),new ConstraintSearchCB<FieldConstraint>() {
                        public boolean checkMatch(FieldConstraint c) {
                            return c instanceof RequiredConstraint;
                        }
                    })==null) {
                f.setConstraints(addConstraint(f.getConstraints(),new RequiredConstraint()));
            }
            // Can't be empty
            if(findConstraint(FieldConstraint.class,f.getConstraints(),new ConstraintSearchCB<FieldConstraint>() {
                        public boolean checkMatch(FieldConstraint c) {
                            if(c instanceof StringLengthConstraint) {
                                if( ((StringLengthConstraint)c).getType().equals(StringLengthConstraint.MINLENGTH) )
                                    return true;
                            }
                            return false;
                        }
                    })==null) {
                f.setConstraints(addConstraint(f.getConstraints(),new StringLengthConstraint(StringLengthConstraint.MINLENGTH,1)));
            }
            setRoleIfEmpty(f.getAccess().getFind(),Constants.ROLE_ANYONE);
            setRoleIfEmpty(f.getAccess().getUpdate(),Constants.ROLE_NOONE);
        } else {
            throw Error.get(ERR_FIELD_WRONG_TYPE,OBJECTTYPE_FIELD+":"+f.getType().getName());
        }
    }

    private static void setRoleIfEmpty(Access access,String role) {
        if(access.getRoles().isEmpty()) {
            List<String> l=new ArrayList<String>(1);
            l.add(role);
            access.setRoles(l);
        }
    }

    private interface ConstraintSearchCB<T> {
        boolean checkMatch(T c);
    }

    private static<T> T findConstraint(Class<T> clazz,List<T> list,ConstraintSearchCB<T> cb) {
        if(list!=null) {
            for(T x:list) {
                if(cb.checkMatch(x))
                    return x;
            }
        }
        return null;
    }

    private static<T> List<T> addConstraint(List<T> constraints,T newConstraint) {
        List<T> ret=constraints==null?new ArrayList<T>(1):constraints;
        ret.add(newConstraint);
        return ret;
    }

    private static final class ParentNewChild {
        private final Fields parent;
        private final Field newChild;

        public ParentNewChild(Fields parent,Field newChild) {
            this.parent=parent;
            this.newChild=newChild;
        }
    }

    private static ParentNewChild ensureArraySize(EntityMetadata md,ArrayField arr) {
        // Get the parent. The parent is either an object field, or the root
        FieldTreeNode parent=arr.getParent();
        Fields fields;
        if(parent instanceof ObjectField)
            fields=((ObjectField)parent).getFields();
        else
            fields=md.getFields();
        String fieldName=arr.getName()+"#";
        Field f=fields.getField(fieldName);
        ParentNewChild ret;
        if(f==null) {
            ret=new ParentNewChild(fields,f=new SimpleField(fieldName,IntegerType.TYPE));
        } else
            ret=null;
        if(f instanceof SimpleField &&
           // Must be int
           f.getType().equals(IntegerType.TYPE)) {
            setRoleIfEmpty(f.getAccess().getFind(),Constants.ROLE_ANYONE);
            setRoleIfEmpty(f.getAccess().getUpdate(),Constants.ROLE_NOONE);
        } else {
            throw Error.get(ERR_FIELD_WRONG_TYPE,fieldName+":"+f.getType().getName());
        }
        return ret;
    }

    private PredefinedFields() {}
}
