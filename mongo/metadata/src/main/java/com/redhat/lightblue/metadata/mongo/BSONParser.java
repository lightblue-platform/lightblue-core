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
package com.redhat.lightblue.metadata.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.bson.BSONObject;

import com.mongodb.BasicDBObject;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BSONParser extends MetadataParser<BSONObject> {

    public static final String DELIMITER_ID = "|";

    public BSONParser(Extensions<BSONObject> ex,
                      TypeResolver resolver) {
        super(ex, resolver);
    }

    @Override
    public String getStringProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof String) {
                return (String) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public BSONObject getObjectProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof BSONObject) {
                return (BSONObject) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public Object getValueProperty(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof Number
                    || x instanceof String
                    || x instanceof Date
                    || x instanceof Boolean) {
                return x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<String> getStringList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                ArrayList<String> ret = new ArrayList<>();
                for (Object o : (List) x) {
                    ret.add(o.toString());
                }
                return ret;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<BSONObject> getObjectList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                return (List<BSONObject>) x;
            } else {
                throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @Override
    public BSONObject newNode() {
        return new BasicDBObject();
    }

    @Override
    public Set<String> getChildNames(BSONObject object) {
        return object.keySet();
    }

    @Override
    public void putString(BSONObject object, String name, String value) {
        object.put(name, value);
    }

    @Override
    public void putObject(BSONObject object, String name, Object value) {
        object.put(name, value);
    }

    @Override
    public void putValue(BSONObject object, String name, Object value) {
        object.put(name, value);
    }

    @Override
    public Object newArrayField(BSONObject object, String name) {
        Object ret = new ArrayList();
        object.put(name, ret);
        return ret;
    }

    @Override
    public void addStringToArray(Object array, String value) {
        ((List) array).add(value);
    }

    @Override
    public void addObjectToArray(Object array, Object value) {
        ((List) array).add(value);
    }

    /**
     * Override to set _id appropriately.
     */
    @Override
    public BSONObject convert(EntityInfo info) {
        Error.push("convert[info|bson]");
        try {
            BSONObject doc = super.convert(info);

            // entityInfo._id = {entityInfo.name}|
            putValue(doc, "_id", getStringProperty(doc, "name") + DELIMITER_ID);

            return doc;
        } finally {
            Error.pop();
        }
    }

    /**
     * Override to set _id appropriately.
     */
    @Override
    public BSONObject convert(EntitySchema schema) {
        Error.push("convert[info|bson]");
        try {
            BSONObject doc = super.convert(schema);
            putValue(doc, "_id", getStringProperty(doc, "name") + DELIMITER_ID + getStringProperty(getObjectProperty(doc, "version"), "value"));

            return doc;
        } finally {
            Error.pop();
        }
    }

    @Override
    public List<BSONObject> getObjectList(BSONObject object) {
        if (object instanceof List) {
            return (List<BSONObject>) object;
        } else {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA);
        }
    }

    @Override
    public Projection parseProjection(BSONObject object) {
        return object == null ? null : Projection.fromJson(toJson(object));
    }

    @Override
    public QueryExpression parseQuery(BSONObject object) {
        return object == null ? null : QueryExpression.fromJson(toJson(object));
    }

    @Override
    public Sort parseSort(BSONObject object) {
        return object == null ? null : Sort.fromJson(toJson(object));
    }

    private JsonNode toJson(BSONObject object) {
        try {
            return JsonUtils.json(object.toString());
        } catch (Exception e) {
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, object.toString());
        }
    }
}
