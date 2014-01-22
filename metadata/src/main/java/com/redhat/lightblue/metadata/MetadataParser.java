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

import com.redhat.lightblue.metadata.types.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.QueryExpression;

import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.EntityConstraintParser;
import com.redhat.lightblue.metadata.parser.FieldConstraintParser;

import com.redhat.lightblue.metadata.types.ArrayType;
import com.redhat.lightblue.metadata.types.ObjectType;
import com.redhat.lightblue.metadata.types.ReferenceType;
import com.redhat.lightblue.metadata.types.DateType;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import java.io.IOException;
import java.text.ParseException;

/**
 * Base class for converting metadata to/from json/bson and potentially other formats represented as a tree.
 *
 * The metadata parser is thread safe.
 */
public abstract class MetadataParser<T> {

    public static final String ERR_PARSE_MISSING_ELEMENT = "PARSE_MISSING_ELEMENT";
    public static final String ERR_PARSE_INVALID_STATUS = "PARSE_INVALID_STATUS";
    public static final String ERR_INVALID_ARRAY_ELEMENT_TYPE = "INVALID_ARRAY_ELEMENT_TYPE";
    public static final String ERR_ILL_FORMED_METADATA = "ILL_FORMED_METADATA";
    public static final String ERR_INVALID_DATASTORE = "INVALID_DATASTORE";
    public static final String ERR_UNKNOWN_DATASTORE = "UNKNOWN_DATASTORE";
    public static final String ERR_INVALID_CONSTRAINT = "INVALID_CONSTRAINT";
    public static final String ERR_INVALID_TYPE = "INVALID_TYPE";

    private static final String STR_VALUE = "value";
    private static final String STR_VERSION = "version";
    private static final String STR_FIND = "find";
    private static final String STR_STATUS = "status";
    private static final String STR_CONSTRAINTS = "constraints";
    private static final String STR_ACCESS = "access";
    private static final String STR_UPDATE = "update";
    private static final String STR_FIELDS = "fields";
    private static final String STR_TYPE = "type";

    private final Extensions<T> extensions;
    private final TypeResolver typeResolver;

    public MetadataParser(Extensions<T> ex,
                          TypeResolver typeResolver) {
        this.extensions = ex;
        this.typeResolver = typeResolver;
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
     * Entry point for entity metadata parser. Expects an Object corresponding to the EntityMetadata object.
     *
     * @throws ParseException
     */
    public EntityMetadata parseEntityMetadata(T object) {
        Error.push("parseEntityMetadata");
        try {
            String name = getRequiredStringProperty(object, "name");

            EntityMetadata md = new EntityMetadata(name);
            T version = getRequiredObjectProperty(object, STR_VERSION);
            md.setVersion(parseVersion(version));

            T status = getRequiredObjectProperty(object, STR_STATUS);
            parseStatus(md, status);

            // TODO hooks
            T access = getObjectProperty(object, STR_ACCESS);
            if (access != null) {
                parseEntityAccess(md.getAccess(), access);
            }

            T fields = getRequiredObjectProperty(object, STR_FIELDS);
            parseFields(md.getFields(), fields, md.getFieldTreeRoot());

            List<T> constraints = getObjectList(object, STR_CONSTRAINTS);
            parseEntityConstraints(md, constraints);

            T datastore = getRequiredObjectProperty(object, "datastore");
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
        Error.push(STR_VERSION);
        try {
            if (object != null) {
                Version v = new Version();
                v.setValue(getRequiredStringProperty(object, STR_VALUE));
                List<String> l = getStringList(object, "extendedVersions");
                if (l != null) {
                    v.setExtendsVersions(l.toArray(new String[l.size()]));
                }
                v.setChangelog(getRequiredStringProperty(object, "changelog"));
                return v;
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses metadata status, and populates metadata
     *
     * @throws ParseException
     */
    public void parseStatus(EntityMetadata md, T object) {
        Error.push(STR_STATUS);
        try {
            md.setStatus(statusFromString(getRequiredStringProperty(object, STR_VALUE)));
            List<T> logList = getObjectList(object, "log");
            List<StatusChange> list = new ArrayList<>();
            if (logList != null) {
                for (T log : logList) {
                    StatusChange item = new StatusChange();
                    String d = getRequiredStringProperty(log, "date");
                    try {
                        item.setDate(DateType.getDateFormat().parse(d));
                    } catch (ParseException e) {
                        throw Error.get(ERR_ILL_FORMED_METADATA, d);
                    }
                    item.setStatus(statusFromString(getRequiredStringProperty(log, STR_VALUE)));
                    item.setComment(getRequiredStringProperty(log, "comment"));
                    list.add(item);
                }
                md.setStatusChangeLog(list);
            }
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses metadata entity access
     *
     * @param access The entity access object to be initialized
     * @param object The object corresponding to the entity access element
     */
    public void parseEntityAccess(EntityAccess access,
                                  T object) {
        Error.push(STR_ACCESS);
        try {
            if (object != null) {
                parseAccess(access.getFind(), getStringList(object, STR_FIND));
                parseAccess(access.getUpdate(), getStringList(object, STR_UPDATE));
                parseAccess(access.getDelete(), getStringList(object, "delete"));
                parseAccess(access.getInsert(), getStringList(object, "insert"));
            }
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses entity constraints using the registered entity constraint parsers.
     *
     * Entity constraints are an object array where each object contains only one field, the constraint name. The
     * constraint data can be a simple value, an array, or an object.
     */
    public void parseEntityConstraints(EntityMetadata md,
                                       List<T> constraintList) {
        if (constraintList != null) {
            List<EntityConstraint> entityConstraintList = new ArrayList<>();
            for (T x : constraintList) {
                // The constraint object must contain a single field
                String name = getSingleFieldName(x, ERR_INVALID_CONSTRAINT);
                Error.push(name);
                try {
                    EntityConstraintParser<T> parser = getEntityConstraintParser(name);
                    if (parser == null) {
                        throw Error.get(ERR_INVALID_CONSTRAINT, name);
                    }
                    EntityConstraint constraint = parser.parse(name, this, x);
                    entityConstraintList.add(constraint);
                } finally {
                    Error.pop();
                }
            }
            if (!entityConstraintList.isEmpty()) {
                md.setConstraints(entityConstraintList);
            }
        }
    }

    public void parseFieldConstraints(Field field,
                                      T fieldConstraints) {
        if (fieldConstraints != null) {
            // The constraint object must contain a single field
            Set<String> childNames = getChildNames(fieldConstraints);
            List<FieldConstraint> constraints = new ArrayList<>();
            for (String name : childNames) {
                Error.push(name);
                try {
                    FieldConstraintParser<T> parser = getFieldConstraintParser(name);
                    if (parser == null) {
                        throw Error.get(ERR_INVALID_CONSTRAINT, name);
                    }
                    // for each FieldConstraint call parse on the parent object
                    FieldConstraint constraint = parser.parse(name, this, fieldConstraints);
                    constraints.add(constraint);
                } finally {
                    Error.pop();
                }
            }
            if (!constraints.isEmpty()) {
                field.setConstraints(constraints);
            }
        }
    }

    /**
     * Parses field access
     *
     * @param access The field access object to be initialized
     * @param object The object corresponding to the field access element
     */
    public void parseFieldAccess(FieldAccess access,
                                 T object) {
        Error.push(STR_ACCESS);
        try {
            if (object != null) {
                parseAccess(access.getFind(), getStringList(object, STR_FIND));
                parseAccess(access.getUpdate(), getStringList(object, STR_UPDATE));
            }
        } finally {
            Error.pop();
        }
    }

    private void parseAccess(Access access, List<String> roles) {
        if (roles != null) {
            access.setRoles(roles);
        }
    }

    /**
     * Parses and initializes fields
     *
     * @param fields The destination object to be initialized
     * @param object The object corresponding to the fields element
     */
    public void parseFields(Fields fields, T object, FieldTreeNode parent) {
        Error.push(STR_FIELDS);
        try {
            if (object != null) {
                Set<String> names = getChildNames(object);
                for (String name : names) {
                    T fieldObject = getObjectProperty(object, name);
                    Field field = parseField(name, fieldObject);
                    fields.addNew(field, parent);
                }
            }
        } finally {
            Error.pop();
        }
    }

    /**
     * Parses a datastore using a registered datastore parser
     *
     * @param object The object for the datastore element. The object must contain only one object field whose name is
     * used to resolve the datastore parser
     *
     * @return The parsed datastore. Returns null if object is null.
     */
    public DataStore parseDataStore(T object) {
        if (object != null) {
            String name = getSingleFieldName(object, ERR_INVALID_DATASTORE);
            DataStoreParser<T> p = getDataStoreParser(name);
            if (p == null) {
                throw Error.get(ERR_UNKNOWN_DATASTORE, name);
            }
            return p.parse(name, this, getObjectProperty(object, name));
        } else {
            return null;
        }
    }

    /**
     * Returns the single field name contained in the object. If the object contains more fields or no fields, throws an
     * error with the given error code.
     */
    private String getSingleFieldName(T object, String errorCode) {
        Set<String> names = getChildNames(object);
        if (names.size() != 1) {
            throw Error.get(errorCode, names.toString());
        }
        return names.iterator().next();
    }

    private Field parseField(String name, T object) {
        Field field;
        Error.push(name);
        try {
            if (object != null) {
                String type = getRequiredStringProperty(object, STR_TYPE);
                if (type.equals(ArrayType.TYPE.getName())) {
                    field = parseArrayField(name, object);
                } else if (type.equals(ObjectType.TYPE.getName())) {
                    field = parseObjectField(name, object);
                } else if (type.equals(ReferenceType.TYPE.getName())) {
                    field = parseReferenceField(name, object);
                } else {
                    field = parseSimpleField(name, type);
                }
                parseFieldAccess(field.getAccess(),
                        getObjectProperty(object, STR_ACCESS));
                parseFieldConstraints(field,
                        getObjectProperty(object, STR_CONSTRAINTS));
            } else {
                field = null;
            }
            return field;
        } finally {
            Error.pop();
        }
    }

    private Field parseSimpleField(String name,
                                   String type) {
        SimpleField field = new SimpleField(name);
        Type t = typeResolver.getType(type);
        if (t == null) {
            throw Error.get(ERR_INVALID_TYPE, type);
        }
        field.setType(t);
        return field;
    }

    private Field parseReferenceField(String name,
                                      T object) {
        ReferenceField field = new ReferenceField(name);
        field.setEntityName(getRequiredStringProperty(object, "entity"));
        field.setVersionValue(getRequiredStringProperty(object, "versionValue"));
        try {
            String x = getRequiredStringProperty(object, "projection");
            field.setProjection(Projection.fromJson(JsonUtils.json(x)));
            x = getRequiredStringProperty(object, "query");
            field.setQuery(QueryExpression.fromJson(JsonUtils.json(x)));
            x = getStringProperty(object, "sort");
            if (x != null) {
                field.setSort(Sort.fromJson(JsonUtils.json(x)));
            }
        } catch (IOException e) {
            throw Error.get(ERR_ILL_FORMED_METADATA, e.toString());
        }
        return field;
    }

    private Field parseObjectField(String name,
                                   T object) {
        ObjectField field = new ObjectField(name);
        T fields = getRequiredObjectProperty(object, STR_FIELDS);
        parseFields(field.getFields(), fields, field.getParent());
        return field;
    }

    private Field parseArrayField(String name,
                                  T object) {
        ArrayField field = new ArrayField(name);
        T items = getRequiredObjectProperty(object, "items");
        field.setElement(parseArrayItem(items), field.getParent());
        return field;
    }

    private ArrayElement parseArrayItem(T items) {
        String type = getRequiredStringProperty(items, STR_TYPE);

        if (type.equals(ObjectType.TYPE.getName())) {
            T fields = getRequiredObjectProperty(items, STR_FIELDS);
            ObjectArrayElement ret = new ObjectArrayElement();
            ret.setType(ObjectType.TYPE);
            parseFields(ret.getFields(), fields, ret.getParent());
            return ret;
        } else if (type.equals(ArrayType.TYPE.getName())
                || type.equals(ReferenceType.TYPE.getName())) {
            throw Error.get(ERR_INVALID_ARRAY_ELEMENT_TYPE, type);
        } else {
            SimpleArrayElement ret = new SimpleArrayElement();
            Type t = typeResolver.getType(type);
            if (t == null) {
                throw Error.get(ERR_INVALID_TYPE, type);
            }
            ret.setType(t);
            return ret;
        }
    }

    /**
     * Converts the entity metadata to T
     */
    public T convert(EntityMetadata md) {
        Error.push("convert");
        try {
            T ret = newNode();
            if (md.getName() != null) {
                putString(ret, "name", md.getName());
            }
            putObject(ret, STR_VERSION, convert(md.getVersion()));
            putObject(ret, STR_STATUS, convert(md.getStatus(), md.getStatusChangeLog()));
            putObject(ret, STR_ACCESS, convert(md.getAccess()));
            putObject(ret, STR_FIELDS, convert(md.getFields()));
            convertEntityConstraints(ret, md.getConstraints());
            if (md.getDataStore() != null) {
                T dsNode = newNode();
                convertDataStore(dsNode, md.getDataStore());
                putObject(ret, "datastore", dsNode);
            }
            return ret;
        } finally {
            Error.pop();
        }
    }

    /**
     * Converts metadata version to T
     */
    public T convert(Version v) {
        if (v != null) {
            Error.push(STR_VERSION);
            try {
                T obj = newNode();
                if (v.getValue() != null) {
                    putString(obj, STR_VALUE, v.getValue());
                }
                String[] ex = v.getExtendsVersions();
                if (ex != null && ex.length > 0) {
                    Object arr = newArrayField(obj, "extendsVersions");
                    for (String x : ex) {
                        addStringToArray(arr, x);
                    }
                }
                if (v.getChangelog() != null) {
                    putString(obj, "changelog", v.getChangelog());
                }
                return obj;
            } finally {
                Error.pop();
            }
        } else {
            return null;
        }
    }

    public T convert(MetadataStatus status, List<StatusChange> changeLog) {
        if (status != null && changeLog != null) {
            Error.push(STR_STATUS);
            try {
                T obj = newNode();
                putString(obj, STR_VALUE, toString(status));

                // only create log if you have a value for status, else isn't schema compliant 
                if (!changeLog.isEmpty()) {
                    Object logArray = newArrayField(obj, "log");
                    for (StatusChange x : changeLog) {
                        T log = newNode();
                        if (x.getDate() != null) {
                            putString(log, "date", DateType.getDateFormat().format(x.getDate()));
                        }
                        if (x.getStatus() != null) {
                            putString(log, STR_VALUE, toString(x.getStatus()));
                        }
                        if (x.getComment() != null) {
                            putString(log, "comment", x.getComment());
                        }
                        addObjectToArray(logArray, log);
                    }
                }
                return obj;
            } finally {
                Error.pop();
            }
        }
        return null;
    }

    /**
     * Converts entity access to T
     */
    public T convert(EntityAccess access) {
        if (access != null) {
            Error.push(STR_ACCESS);
            try {
                T ret = newNode();
                convertRoles(ret, "insert", access.getInsert());
                convertRoles(ret, STR_UPDATE, access.getUpdate());
                convertRoles(ret, STR_FIND, access.getFind());
                convertRoles(ret, "delete", access.getDelete());
                return ret;
            } finally {
                Error.pop();
            }
        } else {
            return null;
        }
    }

    /**
     * Converts field access to T
     */
    public T convert(FieldAccess access) {
        if (access != null && (access.getFind().getRoles().size() > 0 || access.getUpdate().getRoles().size() > 0)) {
            Error.push(STR_ACCESS);
            try {
                T ret = newNode();
                if (access.getFind().getRoles().size() > 0) {
                    convertRoles(ret, STR_FIND, access.getFind());
                }
                if (access.getUpdate().getRoles().size() > 0) {
                    convertRoles(ret, STR_UPDATE, access.getUpdate());
                }
                return ret;
            } finally {
                Error.pop();
            }
        } else {
            return null;
        }
    }

    /**
     * Converts fields to T
     */
    public T convert(Fields fields) {
        T ret = newNode();
        for (Iterator<Field> itr = fields.getFields(); itr.hasNext();) {
            Field field = itr.next();
            T fieldObject = newNode();
            Error.push(field.getName());
            try {
                putObject(ret, field.getName(), fieldObject);
                putString(fieldObject, STR_TYPE, field.getType().getName());
                if (field instanceof ArrayField) {
                    convertArrayField((ArrayField) field, fieldObject);
                } else if (field instanceof ObjectField) {
                    convertObjectField((ObjectField) field, fieldObject);
                } else if (field instanceof ReferenceField) {
                    convertReferenceField((ReferenceField) field, fieldObject);
                }
                T access = convert(field.getAccess());
                if (access != null) {
                    putObject(fieldObject, STR_ACCESS, access);
                }
                convertFieldConstraints(fieldObject, field.getConstraints());
            } finally {
                Error.pop();
            }
        }
        return ret;
    }

    /**
     * Creates a STR_CONSTRAINTS array in <code>parent</code> and fills it up with constraints
     */
    public void convertFieldConstraints(T parent, List<FieldConstraint> constraints) {
        if (constraints != null && !constraints.isEmpty()) {
            Error.push(STR_CONSTRAINTS);
            try {
                T constraintNode = newNode();
                putObject(parent, STR_CONSTRAINTS, constraintNode);
                for (FieldConstraint constraint : constraints) {
                    String constraintType = constraint.getType();
                    FieldConstraintParser<T> parser = getFieldConstraintParser(constraintType);
                    if (parser == null) {
                        throw Error.get(ERR_INVALID_CONSTRAINT, constraintType);
                    }
                    parser.convert(this, constraintNode, constraint);
                }
            } finally {
                Error.pop();
            }
        }
    }

    /**
     * Creates a STR_CONSTRAINTS array in <code>parent</code> and fills it up with constraints
     */
    public void convertEntityConstraints(T parent, List<EntityConstraint> constraints) {
        if (constraints != null && !constraints.isEmpty()) {
            Error.push(STR_CONSTRAINTS);
            try {
                Object arr = newArrayField(parent, STR_CONSTRAINTS);
                for (EntityConstraint constraint : constraints) {
                    String constraintType = constraint.getType();
                    EntityConstraintParser<T> parser = getEntityConstraintParser(constraintType);
                    if (parser == null) {
                        throw Error.get(ERR_INVALID_CONSTRAINT, constraintType);
                    }
                    T constraintNode = newNode();
                    parser.convert(this, constraintNode, constraint);
                    addObjectToArray(arr, constraintNode);
                }
            } finally {
                Error.pop();
            }
        }
    }

    /**
     * Adds the description of datastore to parent as a field named by the type of the datastore
     */
    public void convertDataStore(T parent, DataStore store) {
        Error.push("datastore");
        try {
            String type = store.getType();
            DataStoreParser<T> parser = getDataStoreParser(type);
            if (parser == null) {
                throw Error.get(ERR_UNKNOWN_DATASTORE, type);
            }
            T dsNode = newNode();
            parser.convert(this, dsNode, store);
            putObject(parent, type, dsNode);
        } finally {
            Error.pop();
        }
    }

    private void convertObjectField(ObjectField field, T fieldObject) {
        putObject(fieldObject, STR_FIELDS, convert(field.getFields()));
    }

    private void convertArrayField(ArrayField field, T fieldObject) {
        ArrayElement el = field.getElement();
        T items = newNode();
        putObject(fieldObject, "items", items);
        putString(items, STR_TYPE, el.getType().getName());
        if (el instanceof ObjectArrayElement) {
            convertObjectArrayElement((ObjectArrayElement) el, items);
        }
    }

    private void convertReferenceField(ReferenceField field, T fieldObject) {
        putString(fieldObject, "entity", field.getEntityName());
        putString(fieldObject, "versionValue", field.getVersionValue());
        if (field.getProjection() != null) {
            putString(fieldObject, "projection", field.getProjection().toString());
        }
        if (field.getQuery() != null) {
            putString(fieldObject, "query", field.getQuery().toString());
        }
        if (field.getSort() != null) {
            putString(fieldObject, "sort", field.getSort().toString());
        }
    }

    private void convertObjectArrayElement(ObjectArrayElement el, T items) {
        putObject(items, STR_FIELDS, convert(el.getFields()));
    }

    private void convertRoles(T node, String name, Access roles) {
        if (roles != null) {
            Object arr = newArrayField(node, name);
            Set<String> r = roles.getRoles();
            for (String x : r) {
                addStringToArray(arr, x);
            }
        }
    }

    private String toString(MetadataStatus status) {
        switch (status) {
            case ACTIVE:
                return "active";
            case DEPRECATED:
                return "deprecated";
            case DISABLED:
                return "disabled";
        }
        return null;
    }

    private MetadataStatus statusFromString(String status) {
        switch (status) {
            case "active":
                return MetadataStatus.ACTIVE;
            case "deprecated":
                return MetadataStatus.DEPRECATED;
            case "disabled":
                return MetadataStatus.DISABLED;
            default:
                throw Error.get(ERR_PARSE_INVALID_STATUS, status);
        }
    }

    /**
     * Returns a string child property
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * If the property is not a string, should throw exception
     *
     * @return The string property requested, or null if property does not exist
     */
    public abstract String getStringProperty(T object, String name);

    /**
     * Returns a string child property, fail if the child property is not found.
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * If the property is not a string, should throw exception
     *
     * @return The string property requested, or null if property does not exist
     */
    public String getRequiredStringProperty(T object, String name) {
        Error.push("required");
        Error.push(name);
        try {
            String property = getStringProperty(object, name);
            if (property == null || property.trim().length() == 0) {
                throw Error.get(ERR_PARSE_MISSING_ELEMENT, name);
            }
            return property;
        } finally {
            Error.pop();
            Error.pop();
        }
    }

    /**
     * Returns an object child property
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * If the property is not an object, should throw an exception
     *
     * @return The property requested, or null if property does not exist
     */
    public abstract T getObjectProperty(T object, String name);

    /**
     * Returns an object child property, fail if the child property is not found.
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * If the property is not an object, should throw an exception
     *
     * @return The property requested, or null if property does not exist
     */
    public T getRequiredObjectProperty(T object, String name) {
        Error.push("required");
        Error.push(name);
        try {
            T property = getObjectProperty(object, name);
            if (property == null) {
                throw Error.get(ERR_PARSE_MISSING_ELEMENT, name);
            }
            return property;
        } finally {
            Error.pop();
            Error.pop();
        }
    }

    /**
     * Returns a property that is a simple value
     *
     * @param object The object containing the property
     * @param name Name of the property to return
     *
     * If the property is not a simple java value, should throw exception
     *
     * @return The property value requested (String, Number, Boolean, etc), or null if property does not exist
     */
    public abstract Object getValueProperty(T object, String name);

    /**
     * Returns a string list child property
     *
     * @param object The object containing the property
     * @param name Name of the string list
     *
     * @return The string list property, or null if property does not exist
     */
    public abstract List<String> getStringList(T object, String name);

    /**
     * Returns an object list of child property
     *
     * @param object The object containing the property
     * @param name Name of the property
     *
     * @return Object list property, or null if property does not exist
     */
    public abstract List<T> getObjectList(T object, String name);

    /**
     * Returns the names of the child elements
     *
     * @param object The object
     *
     * @return The names of child elements
     */
    public abstract Set<String> getChildNames(T object);

    /**
     * Creates a new node
     */
    public abstract T newNode();

    /**
     * Adds a new string field to the object.
     */
    public abstract void putString(T object, String name, String value);

    /**
     * Adds a new object field to the object.
     */
    public abstract void putObject(T object, String name, Object value);

    /**
     * Adds a simple value field
     */
    public abstract void putValue(T object, String name, Object value);

    /**
     * Creates a new array field
     */
    public abstract Object newArrayField(T object, String name);

    /**
     * Adds an element to the array
     */
    public abstract void addStringToArray(Object array, String value);

    /**
     * Adds an element to the array
     */
    public abstract void addObjectToArray(Object array, Object value);

}
