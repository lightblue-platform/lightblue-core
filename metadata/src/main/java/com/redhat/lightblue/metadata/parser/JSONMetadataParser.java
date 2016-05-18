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
package com.redhat.lightblue.metadata.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.Error;

public class JSONMetadataParser extends MetadataParser<JsonNode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONMetadataParser.class);

    private final JsonNodeFactory factory;

    public JSONMetadataParser(Extensions<JsonNode> ex,
                              TypeResolver resolver,
                              JsonNodeFactory factory) {
        super(ex, resolver);
        this.factory = factory;
    }

    @Override
    public JsonNode newMap() {
        return factory.objectNode();
    }

    @Override
    public JsonNode getMapProperty(JsonNode map, String name) {
        return ((ObjectNode) map).get(name);
    }

    @Override
    public Set<String> getMapPropertyNames(JsonNode map) {
        if (map instanceof ObjectNode) {
            HashSet<String> names = new HashSet<>();
            for (Iterator<String> itr = ((ObjectNode) map).fieldNames(); itr.hasNext();) {
                names.add(itr.next());
            }
            return names;
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public void setMapProperty(JsonNode map, String name, JsonNode value) {
        ((ObjectNode) map).set(name, value);
    }

    @Override
    public JsonNode newList() {
        return factory.arrayNode();
    }

    @Override
    public int getListSize(JsonNode list) {
        return ((ArrayNode) list).size();
    }

    @Override
    public JsonNode getListElement(JsonNode list, int n) {
        return ((ArrayNode) list).get(n);
    }

    @Override
    public void addListElement(JsonNode list, JsonNode element) {
        ((ArrayNode) list).add(element);
    }

    @Override
    public Object asValue(JsonNode value) {
        if (value == null) {
            return null;
        } else if (value instanceof NullNode) {
            return null;
        } else if (value instanceof BigIntegerNode) {
            return ((ValueNode) value).bigIntegerValue();
        } else if (value instanceof BooleanNode) {
            return ((ValueNode) value).booleanValue();
        } else if (value instanceof DecimalNode) {
            return ((ValueNode) value).decimalValue();
        } else if (value instanceof DoubleNode || value instanceof FloatNode) {
            return ((ValueNode) value).doubleValue();
        } else if (value instanceof IntNode) {
            return ((ValueNode) value).intValue();
        } else if (value instanceof LongNode) {
            return ((ValueNode) value).longValue();
        } else {
            return ((ValueNode) value).asText();
        }
    }

    @Override
    public JsonNode asRepresentation(Object value) {
        if (value == null) {
            return factory.nullNode();
        } else if (value instanceof JsonNode) {
            return (JsonNode) value;
        } else if (value instanceof Boolean) {
            return factory.booleanNode((Boolean) value);
        } else if (value instanceof BigDecimal) {
            return factory.numberNode((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return factory.numberNode((BigInteger) value);
        } else if (value instanceof Double) {
            return factory.numberNode((Double) value);
        } else if (value instanceof Float) {
            return factory.numberNode((Float) value);
        } else if (value instanceof Integer) {
            return factory.numberNode((Integer) value);
        } else if (value instanceof Long) {
            return factory.numberNode((Long) value);
        } else if (value instanceof Short) {
            return factory.numberNode((Short) value);
        } else {
            return factory.textNode(value.toString());
        }
    }

    @Override
    public MetadataParser.PropertyType getType(JsonNode object) {
        if (object instanceof ArrayNode) {
            return MetadataParser.PropertyType.LIST;
        } else if (object instanceof ObjectNode) {
            return MetadataParser.PropertyType.MAP;
        } else if (object == null || object instanceof NullNode) {
            return MetadataParser.PropertyType.NULL;
        } else {
            return MetadataParser.PropertyType.VALUE;
        }
    }

    @Override
    public Projection getProjection(JsonNode object, String name) {
        JsonNode node = object.get(name);
        return node == null ? null : Projection.fromJson(node);
    }

    @Override
    public QueryExpression getQuery(JsonNode object, String name) {
        JsonNode node = object.get(name);
        return node == null ? null : QueryExpression.fromJson(node);
    }

    @Override
    public Sort getSort(JsonNode object, String name) {
        JsonNode node = object.get(name);
        return node == null ? null : Sort.fromJson(node);
    }

    @Override
    public void putProjection(JsonNode object, String name, Projection p) {
        if (p != null) {
            ((ObjectNode) object).set(name, p.toJson());
        }
    }

    @Override
    public void putQuery(JsonNode object, String name, QueryExpression q) {
        if (q != null) {
            ((ObjectNode) object).set(name, q.toJson());
        }
    }

    @Override
    public void putSort(JsonNode object, String name, Sort s) {
        if (s != null) {
            ((ObjectNode) object).set(name, s.toJson());
        }
    }
}
