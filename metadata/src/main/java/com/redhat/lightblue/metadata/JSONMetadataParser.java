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

import java.math.BigInteger;
import java.math.BigDecimal;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.parser.Extensions;

import com.redhat.lightblue.util.Error;

public class JSONMetadataParser extends MetadataParser<JsonNode> {

    private final JsonNodeFactory factory;

    public JSONMetadataParser(Extensions<JsonNode> ex,
            TypeResolver resolver,
            JsonNodeFactory factory) {
        super(ex, resolver);
        this.factory = factory;
    }

    @Override
    public String getStringProperty(JsonNode object, String name) {
        Error.push(name);
        try {
            JsonNode x = object.get(name);
            if (x != null) {
                if (x.isContainerNode()) {
                    throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
                } else {
                    return x.asText();
                }
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    @Override
    public JsonNode getObjectProperty(JsonNode object, String name) {
        return object.get(name);
    }

    @Override
    public Object getValueProperty(JsonNode object, String name) {
        Error.push(name);
        try {
            JsonNode x = object.get(name);
            if (x != null) {
                if (x.isValueNode()) {
                    if (x.isNumber()) {
                        return x.numberValue();
                    } else if (x.isBoolean()) {
                        return x.booleanValue();
                    } else {
                        return x.asText();
                    }
                } else {
                    throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    @Override
    public List<String> getStringList(JsonNode object, String name) {
        Error.push(name);
        try {
            JsonNode x = object.get(name);
            if (x != null) {
                if (x instanceof ArrayNode) {
                    ArrayList<String> ret = new ArrayList<String>();
                    for (Iterator<JsonNode> itr = ((ArrayNode) x).elements(); itr.hasNext();) {
                        ret.add(itr.next().asText());
                    }
                    return ret;
                } else {
                    throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    @Override
    public List<JsonNode> getObjectList(JsonNode object, String name) {
        Error.push(name);
        try {
            JsonNode x = object.get(name);
            if (x != null) {
                if (x instanceof ArrayNode) {
                    ArrayList<JsonNode> ret = new ArrayList<JsonNode>();
                    for (Iterator<JsonNode> itr = ((ArrayNode) x).elements(); itr.hasNext();) {
                        ret.add(itr.next());
                    }
                    return ret;
                } else {
                    throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    @Override
    public JsonNode newNode() {
        return factory.objectNode();
    }

    @Override
    public Set<String> getChildNames(JsonNode object) {
        if (object instanceof ObjectNode) {
            HashSet<String> names = new HashSet<String>();
            for (Iterator<String> itr = ((ObjectNode) object).fieldNames(); itr.hasNext();) {
                names.add(itr.next());
            }
            return names;
        } else {
            return new HashSet<String>();
        }
    }

    @Override
    public void putString(JsonNode object, String name, String value) {
        ((ObjectNode) object).put(name, value);
    }

    @Override
    public void putObject(JsonNode object, String name, Object value) {
        ((ObjectNode) object).put(name, (JsonNode) value);
    }

    @Override
    public void putValue(JsonNode object, String name, Object value) {
        ObjectNode o = (ObjectNode) object;
        if (value instanceof Boolean) {
            o.put(name, (Boolean) value);
        } else if (value instanceof BigDecimal) {
            o.put(name, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            o.put(name, factory.numberNode((BigInteger) value));
        } else if (value instanceof Double) {
            o.put(name, (Double) value);
        } else if (value instanceof Float) {
            o.put(name, (Float) value);
        } else if (value instanceof Integer) {
            o.put(name, (Integer) value);
        } else if (value instanceof Long) {
            o.put(name, (Long) value);
        } else if (value instanceof Short) {
            o.put(name, (Short) value);
        } else {
            o.put(name, value.toString());
        }
    }

    @Override
    public Object newArrayField(JsonNode object, String name) {
        ArrayNode node = factory.arrayNode();
        ((ObjectNode) object).put(name, node);
        return node;
    }

    @Override
    public void addStringToArray(Object array, String value) {
        ((ArrayNode) array).add(value);
    }

    @Override
    public void addObjectToArray(Object array, Object value) {
        ((ArrayNode) array).add((JsonNode) value);
    }
}
