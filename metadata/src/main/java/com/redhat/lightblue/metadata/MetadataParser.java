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
import java.util.Set;
import java.util.Iterator;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.parser.Parser;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.EntityConstraintParser;
import com.redhat.lightblue.metadata.parser.FieldConstraintParser;
import com.redhat.lightblue.metadata.parser.Extensions;

/**
 * Base class for converting metadata to/from json/bson and
 * potentially other formats represented as a tree.
 *
 * The metadata parser is thread safe.
 */
public abstract class MetadataParser<T> {

    public static final String PARSE_MISSING_ELEMENT="PARSE_MISSING_ELEMENT";
    public static final String INVALID_ARRAY_ELEMENT_TYPE="INVALID_ARRAY_ELEMENT_TYPE";
    public static final String ILL_FORMED_MD="ILL_FORMED_METADATA";
    public static final String INVALID_DATASTORE="INVALID_DATASTORE";
    public static final String UNKNOWN_DATASTORE="UNKNOWN_DATASTORE";
    public static final String INVALID_CONSTRAINT="INVALID_CONSTRAINT";

    private final Extensions<T> extensions;

    public MetadataParser(Extensions<T> ex) {
        this.extensions=ex;
    }

    public DataStoreParser<T> getDataStoreParser(String dataStoreName) {
        return extensions.getDataStoreParser(dataStoreName);
    }

    public EntityConstraintParser<T> getEntityConstraintParser(String constraintName) {
        return extensions.getEntityConstraintParser(constraintName);
    }

    public FieldConstraintParser<T> getFieldConstraintParser(String constraintName) {
        return extensions.getFieldConstraintParser(constraintName);
    }

    /**
     * Entry point for entity metadata parser. Expects an Object
     * corresponding to the EntityMetadata object.
     */
    public EntityMetadata parseEntityMetadata(T object) {
        Error.push("parseEntityMetadata");
        try {
            String name=getStringProperty(object,"name");
            if(name==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"name");
        
            EntityMetadata md=new EntityMetadata(name);
            String ex=getStringProperty(object,"extends");
            if(ex!=null)
                md.setExtendsFrom(ex);
            T version=getObjectProperty(object,"version");
            if(version==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"version");
            md.setVersion(parseVersion(version));
            T access=getObjectProperty(object,"access");
            if(access!=null)
                parseEntityAccess(md.getAccess(),access);
            T fields=getObjectProperty(object,"fields");
            if(fields==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"fields");
            parseFields(md.getFields(),fields);
            List<T> constraints=getObjectList(object,"constraints");
            parseEntityConstraints(md.getConstraints(),constraints);
            T datastore=getObjectProperty(object,"datastore");
            if(datastore==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"datastore");
            md.setDataStore(parseDataStore(datastore));
            return md;
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses metadata version
     *
     * @param object Object corresponding to the version element
     *
     * @return The parsed version
     */
    public Version parseVersion(T object) {
        Error.push("version");
        try {
            if(object!=null) {
                Version v=new Version();
                String value=getStringProperty(object,"value");
                if(value==null||value.trim().length()==0)
                    throw Error.get(PARSE_MISSING_ELEMENT,"value");
                v.setValue(value);
                List<String> l=getStringList(object,"extendedVersions");
                if(l!=null) 
                    v.setExtendsVersions(l.toArray(new String[l.size()]));
                v.setChangelog(getStringProperty(object,"changelog"));
                return v;
            } else
                return null;
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses metadata entity access
     *
     * @param access The entity access object to be initialized
     * @param object The object corresponding to the entity access
     * element
     */
    public void parseEntityAccess(EntityAccess access,
                                  T object) {
        Error.push("access");
        try {
            if(object!=null) {
                parseAccess(access.getFind(),getStringList(object,"find"));
                parseAccess(access.getUpdate(),getStringList(object,"update"));
                parseAccess(access.getDelete(),getStringList(object,"delete"));
                parseAccess(access.getInsert(),getStringList(object,"insert"));
            } 
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses entity constraints using the registered entity constraint parsers.
     *
     * Entity constraints are an object array where each object
     * contains only one field, the constraint name. The constraint
     * data can be a simple value, an array, or an object.
     */
    public void parseEntityConstraints(List<EntityConstraint> dest,
                                       List<T> constraintList) {
        if(constraintList!=null) {
            for(T x:constraintList) {
                // The constraint object must contain a single field
                String name=getSingleFieldName(x,INVALID_CONSTRAINT);
                Error.push(name);
                try {
                    EntityConstraintParser<T> parser=getEntityConstraintParser(name);
                    if(parser==null)
                        throw Error.get(INVALID_CONSTRAINT,name);
                    EntityConstraint constraint=parser.parse(x);
                    dest.add(constraint);
                } finally {
                    Error.pop();
                }
            }
        }
    }

    public void parseFieldConstraints(List<FieldConstraint> dest,
                                      List<T> constraintList) {
        if(constraintList!=null) {
            for(T x:constraintList) {
                // The constraint object must contain a single field
                String name=getSingleFieldName(x,INVALID_CONSTRAINT);
                Error.push(name);
                try {
                    FieldConstraintParser<T> parser=getFieldConstraintParser(name);
                    if(parser==null)
                        throw Error.get(INVALID_CONSTRAINT,name);
                    FieldConstraint constraint=parser.parse(x);
                    dest.add(constraint);
                } finally {
                    Error.pop();
                }
            }
        }
    }
    
    /**
     * Parses field access
     *
     * @param access The field access object to be initialized
     * @param object The object corresponding to the field access
     * element
     */
    public void parseFieldAccess(FieldAccess access,
                                 T object) {
        Error.push("access");
        try {
            if(object!=null) {
                parseAccess(access.getFind(),getStringList(object,"find"));
                parseAccess(access.getUpdate(),getStringList(object,"update"));
            }
        } finally {
            Error.pop();
        }
    }

    private void parseAccess(Access access,List<String> roles) {
        if(roles!=null)
            access.setRoles(roles);
    }

    /**
     * Parses and initializes fields
     * 
     * @param fields The destination object to be initialized
     * @param object The object corresponding to the fields element
     */
    public void parseFields(Fields fields,
                            T object) {
        Error.push("fields");
        try {
            if(object!=null) {
                Set<String> names=getChildNames(object);
                for(String name:names) {
                    T fieldObject=getObjectProperty(object,name);
                    Field field=parseField(name,fieldObject);
                    fields.addNew(field);
                }
            }
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses a datastore using a registered datastore parser
     *
     * @param object The object for the datastore element. The object
     * must contain only one object field whose name is used to
     * resolve the datastore parser
     *
     * @return The parsed datastore. Returns null if object is null.
     */
    public DataStore parseDataStore(T object) {
        if(object!=null) {
            String name=getSingleFieldName(object,INVALID_DATASTORE);
            DataStoreParser<T> p=getDataStoreParser(name);
            if(p==null)
                throw Error.get(UNKNOWN_DATASTORE,name);
            return p.parse(getObjectProperty(object,name));
        } else
            return null;
    }

    /**
     * Returns the single field name contained in the object. If the
     * object contains more fields or no fields, throws an error with
     * the given error code.
     */
    private String getSingleFieldName(T object,String errorCode) {
        Set<String> names=getChildNames(object);
        if(names.size()!=-1)
            throw Error.get(errorCode,names.toString());
        return names.iterator().next();
    }

    private Field parseField(String name,T object) {
        Field field;
        if(object!=null) {
            String type=getStringProperty(object,"type");
            if(type==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"type");
            if(type.equals(Constants.TYPE_ARRAY))
                field=parseArrayField(name,object);
            else if(type.equals(Constants.TYPE_OBJECT))
                field=parseObjectField(name,object);
            //else if(type.equals(Constants.TYPE_RELATION))
            //    field=parseRelationField(name,object);
            else
                field=parseSimpleField(name,type);
            parseFieldAccess(field.getAccess(),
                             getObjectProperty(object,"access"));
            parseFieldConstraints(field.getConstraints(),
                                  getObjectList(object,"constraints"));
        } else
            field=null;
        return field;
    }

    private Field parseSimpleField(String name,
                                   String type) {
        SimpleField field=new SimpleField(name);
        field.setType(type);
        return field;
    }

    private Field parseObjectField(String name,
                                   T object) {
        ObjectField field=new ObjectField(name);
        T fields=getObjectProperty(object,"fields");
        if(fields==null)
            throw Error.get(PARSE_MISSING_ELEMENT,"fields");
        parseFields(field.getFields(),fields);
        return field;
    }

    private Field parseArrayField(String name,
                                  T object) {
        ArrayField field=new ArrayField(name);
        T items=getObjectProperty(object,"items");
        if(items==null)
            throw Error.get(PARSE_MISSING_ELEMENT,"items");
        field.setElement(parseArrayItem(items));
        return field;
    }

    private ArrayElement parseArrayItem(T items) {
        String type=getStringProperty(items,"type");
        if(type==null)
            Error.get(PARSE_MISSING_ELEMENT,"type");
        if(type.equals(Constants.TYPE_OBJECT)) {
            T fields=getObjectProperty(items,"fields");
            if(fields==null)
                throw Error.get(PARSE_MISSING_ELEMENT,"fields");
            ObjectArrayElement ret=new ObjectArrayElement();
            ret.setType(type);
            parseFields(ret.getFields(),fields);
            return ret; 
        } else if(type.equals(Constants.TYPE_ARRAY)||
                  type.equals(Constants.TYPE_RELATION)) {
            throw Error.get(INVALID_ARRAY_ELEMENT_TYPE,type);
        } else {
            SimpleArrayElement ret=new SimpleArrayElement();
            ret.setType(type);
            return ret;
        }
    }

    /**
     * Converts the entity metadata to T
     */
    public T convert(EntityMetadata md) {
        T ret=newNode();
        if(md.getName()!=null)
            putString(ret,"name",md.getName());
        if(md.getExtendsFrom()!=null)
            putString(ret,"extends",md.getExtendsFrom());    
        putObject(ret,"version",convert(md.getVersion()));
        putObject(ret,"access",convert(md.getAccess()));
        putObject(ret,"fields",convert(md.getFields()));
        convertEntityConstraints(ret,md.getConstraints());
        if(md.getDataStore()!=null) {
            String dataStoreName=md.getDataStore().getType();
            T dsNode=newNode();
            convertDataStore(dsNode,md.getDataStore());
            putObject(ret,"datastore",dsNode);
        }
        return ret;
    }

    /**
     * Converts metadata version to T
     */
    public T convert(Version v) {
        if(v!=null) {
            T obj=newNode();
            if(v.getValue()!=null)
                putString(obj,"value",v.getValue());
            String[] ex=v.getExtendsVersions();
            if(ex!=null&&ex.length>0) {
                Object arr=newArrayField(obj,"extendsVersions");
                for(String x:ex)
                    addStringToArray(arr,x);
            }
            if(v.getChangelog()!=null)
                putString(obj,"changelog",v.getChangelog());
            return obj;
        } else
            return null;
    }

    /**
     * Converts entity access to T
     */
    public  T convert(EntityAccess access) {
        if(access!=null) {
            T ret=newNode();
            convertRoles(ret,"insert",access.getInsert());
            convertRoles(ret,"update",access.getUpdate());
            convertRoles(ret,"find",access.getFind());
            convertRoles(ret,"delete",access.getDelete());
            return ret;
        } else
            return null;
    }

    /**
     * Converts field access to T
     */
    public T convert(FieldAccess access) {
        if(access!=null) {
            T ret=newNode();
            convertRoles(ret,"find",access.getFind());
            convertRoles(ret,"update",access.getUpdate());
            return ret;
        } else
            return null;
    }

    /**
     * Converts fields to T
     */
    public T convert(Fields fields) {
        T ret=newNode();
        for(Iterator<Field> itr=fields.getFields();itr.hasNext();) {
            Field field=itr.next();
            T fieldObject=newNode();
            putObject(ret,field.getName(),fieldObject);
            putString(fieldObject,"type",field.getType());
            if(field instanceof SimpleField) {
                ; // Nothing to do
            } else if(field instanceof ArrayField) {
                convertArrayField((ArrayField)field,fieldObject);
            } else if(field instanceof ObjectField) {
                convertObjectField((ObjectField)field,fieldObject);
            } //else if(field instanceof RelationField) {
            //            }
            putObject(fieldObject,"access",convert(field.getAccess()));
            convertFieldConstraints(fieldObject,field.getConstraints());
        }
        return ret;
    }

    /**
     * Creates a "constraints" array in <code>parent</code> and fills
     * it up with constraints
     */
    public void convertFieldConstraints(T parent,List<FieldConstraint> constraints) {
        if(constraints!=null&&!constraints.isEmpty()) {
            Object arr=newArrayField(parent,"constraints");
            for(FieldConstraint constraint:constraints) {
                String constraintType=constraint.getType();
                FieldConstraintParser<T> parser=getFieldConstraintParser(constraintType);
                if(parser==null)
                    throw Error.get(INVALID_CONSTRAINT,constraintType);
                T constraintNode=newNode();
                parser.convert(constraintNode,constraint);
                addObjectToArray(arr,constraintNode);
            }
        }
    }

    /**
     * Creates a "constraints" array in <code>parent</code> and fills
     * it up with constraints
     */
    public void convertEntityConstraints(T parent,List<EntityConstraint> constraints) {
        if(constraints!=null&&!constraints.isEmpty()) {
            Object arr=newArrayField(parent,"constraints");
            for(EntityConstraint constraint:constraints) {
                String constraintType=constraint.getType();
                EntityConstraintParser<T> parser=getEntityConstraintParser(constraintType);
                if(parser==null)
                    throw Error.get(INVALID_CONSTRAINT,constraintType);
                T constraintNode=newNode();
                parser.convert(constraintNode,constraint);
                addObjectToArray(arr,constraintNode);
            }
        }
    }

    /**
     * Adds the description of datastore to parent as a field named by
     * the type of the datastore
     */
    public void convertDataStore(T parent,DataStore store) {
        String type=store.getType();
        DataStoreParser<T> parser=getDataStoreParser(type);
        if(parser==null)
            throw Error.get(UNKNOWN_DATASTORE,type);
        T dsNode=newNode();
        parser.convert(dsNode,store);
        putObject(parent,type,dsNode);
    }
    
    private void convertObjectField(ObjectField field,T fieldObject) {
        putObject(fieldObject,"fields",convert(field.getFields()));
    }

    private void convertArrayField(ArrayField field,T fieldObject) {
        ArrayElement el=field.getElement();
        T items=newNode();
        putObject(fieldObject,"items",items);
        putString(items,"type",el.getType());
        if(el instanceof SimpleArrayElement) {
            ; // Nothing to do
        } else if(el instanceof ObjectArrayElement) {
            convertObjectArrayElement((ObjectArrayElement)el,items);
        }
    }

    private void convertObjectArrayElement(ObjectArrayElement el,T items) {
        putObject(items,"fields",convert(el.getFields()));
    }

    private void convertRoles(T node,String name, Access roles) {
        if(roles!=null) {
            Object arr=newArrayField(node,name);
            Set<String> r=roles.getRoles();
            for(String x:r) 
                addStringToArray(arr,x);
        }
    }

    /**
     * Returns a string child property
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * @return The string property requested, or null if property does
     * not exist
     */
    protected abstract String getStringProperty(T object,String name);

    /**
     * Returns an object child property
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * @return The property requested, or null if property does not
     * exist
     */
    protected abstract T getObjectProperty(T object,String name);

    /**
     * Returns a string list child property
     *
     * @param object The object containing the property
     * @param name Name of the string list
     *
     * @return The string list property, or null if property does not
     * exist
     */
    protected abstract List<String> getStringList(T object,String name);

    /**
     * Returns an object list of child property
     *
     * @param object The object containing the property
     * @param name Name of the property
     *
     * @return Object list property, or null if property does not
     * exist
     */
    protected abstract List<T> getObjectList(T object,String name);

    /**
     * Returns the names of the child elements
     *
     * @param object The object
     *
     * @return The names of child elements
     */
    protected abstract Set<String> getChildNames(T object);

    /**
     * Creates a new node
     */
    protected abstract T newNode();

    /**
     * Adds a new string field to the object. 
     */
    protected abstract void putString(T object,String name,String value);

    /**
     * Adds a new object field to the object. 
     */
    protected abstract void putObject(T object,String name,Object value);

    /**
     * Creates a new array field
     */
    protected abstract Object newArrayField(T object,String name);

    /**
     * Adds an element to the array
     */
    protected abstract void addStringToArray(Object array,String value);

    /**
     * Adds an element to the array
     */
    protected abstract void addObjectToArray(Object array,Object value);


}
