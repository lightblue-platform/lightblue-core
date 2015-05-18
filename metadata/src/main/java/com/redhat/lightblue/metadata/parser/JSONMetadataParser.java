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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public String getStringProperty(JsonNode object, String name) {
        Error.push(name);
        try {
            JsonNode x = object.get(name);
            if (x != null) {
                if (x.isContainerNode()) {
                    throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
                } else if (x instanceof com.fasterxml.jackson.databind.node.NullNode) {
                    return null;
                } else {
                    return x.asText();
                }
            } else {
                return null;
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
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
                    throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
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
                    ArrayList<String> ret = new ArrayList<>();
                    for (Iterator<JsonNode> itr = ((ArrayNode) x).elements(); itr.hasNext();) {
                        ret.add(itr.next().asText());
                    }
                    return ret;
                } else {
                    throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
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
                    ArrayList<JsonNode> ret = new ArrayList<>();
                    for (Iterator<JsonNode> itr = ((ArrayNode) x).elements(); itr.hasNext();) {
                        ret.add(itr.next());
                    }
                    return ret;
                } else {
                    throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
                }
            } else {
                return null;
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    @Override
    public List<JsonNode> getObjectList(JsonNode object) {
        Error.push("getObjectList");
        try {
            if (object instanceof ArrayNode) {
                ArrayList<JsonNode> ret = new ArrayList<>();
                for (Iterator<JsonNode> itr = ((ArrayNode) object).elements(); itr.hasNext();) {
                    ret.add(itr.next());
                }
                return ret;
            } else {
                return null;
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    @Override
    public ObjectNode newNode() {
        return factory.objectNode();
    }

    @Override
    public Set<String> getChildNames(JsonNode object) {
        if (object instanceof ObjectNode) {
            HashSet<String> names = new HashSet<>();
            for (Iterator<String> itr = ((ObjectNode) object).fieldNames(); itr.hasNext();) {
                names.add(itr.next());
            }
            return names;
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public void putString(JsonNode object, String name, String value) {
        putValue(object, name, value);
    }

    @Override
    public void putObject(JsonNode object, String name, Object value) {
        ((ObjectNode) object).set(name, (JsonNode) value);
    }

    @Override
    public void putValue(JsonNode object, String name, Object value) {
        ObjectNode o = (ObjectNode) object;
        if (value == null) {
            o.set(name, factory.nullNode());
        } else if (value instanceof Boolean) {
            o.put(name, (Boolean) value);
        } else if (value instanceof BigDecimal) {
            o.put(name, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            o.set(name, factory.numberNode((BigInteger) value));
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
        ((ObjectNode) object).set(name, node);
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

    @Override
    public Set<String> findFieldsNotIn(JsonNode elements, Set<String> removeAllFields) {
        final HashSet<String> strings = new HashSet<String>();
        final Iterator<String> stringIterator = elements.fieldNames();
        while (stringIterator.hasNext()) {
            String next = stringIterator.next();
            if (!removeAllFields.contains(next)) {
                strings.add(next);
            }
        }

        return strings;
    }

    @Override
    public Projection getProjection(JsonNode object,String name) {
        JsonNode node=object.get(name);
        return node == null ? null : Projection.fromJson(node);
    }

    @Override
    public QueryExpression getQuery(JsonNode object,String name) {
        JsonNode node=object.get(name);
        return node == null ? null : QueryExpression.fromJson(node);
    }

    @Override
    public Sort getSort(JsonNode object,String name) {
        JsonNode node=object.get(name);
        return node == null ? null : Sort.fromJson(node);
    }

    @Override
    public void putProjection(JsonNode object,String name,Projection p) {
        if(p!=null)
            ((ObjectNode)object).set(name,p.toJson());
    }

    @Override
    public void putQuery(JsonNode object,String name,QueryExpression q) {
        if(q!=null)
            ((ObjectNode)object).set(name,q.toJson());
    }

    @Override
    public void putSort(JsonNode object,String name,Sort s) {
        if(s!=null)
            ((ObjectNode)object).set(name,s.toJson());
    }
}
