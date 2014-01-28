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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Date;

import com.mongodb.BasicDBObject;

import org.bson.BSONObject;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.metadata.MetadataParser;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Extensions;

public class BSONParser extends MetadataParser<BSONObject> {

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
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
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
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
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
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<String> getStringList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                ArrayList<String> ret = new ArrayList<String>();
                for (Object o : (List) x) {
                    ret.add(o.toString());
                }
                return ret;
            } else {
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BSONObject> getObjectList(BSONObject object, String name) {
        Object x = object.get(name);
        if (x != null) {
            if (x instanceof List) {
                return (List<BSONObject>) x;
            } else {
                throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
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

    @SuppressWarnings({ "rawtypes"})
    @Override
    public Object newArrayField(BSONObject object, String name) {
        Object ret = new ArrayList();
        object.put(name, ret);
        return ret;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addStringToArray(Object array, String value) {
        ((List) array).add(value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addObjectToArray(Object array, Object value) {
        ((List) array).add(value);
    }
}
