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
package com.redhat.lightblue.util;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.nio.charset.Charset;

import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;

/**
 * Generic utilities for dealing with JSON
 */
public final class JsonUtils {

    /**
     * <p>
     * Returns an object mapper to parse JSON text.</p>
     * <p>
     * <b>NOTE:</b> {@link ObjectMapper} should not be shared among threads.</p>
     */
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        mapper.setDateFormat(Constants.getDateFormat());
        return mapper;
    }

    /**
     * Parses a string and retruns a JSON tree
     */
    public static JsonNode json(String s)
            throws IOException {
        return json(s, false);
    }

    public static JsonNode json(String s, boolean systemPropertySubstitution)
            throws IOException {
        String jsonString;
        if (systemPropertySubstitution) {
            // do system property expansion
            jsonString = StrSubstitutor.replaceSystemProperties(s);
        } else {
            jsonString = s;
        }
        return getObjectMapper().readTree(jsonString);
    }

    /**
     * Parses a JSON stream
     */
    public static JsonNode json(InputStream stream, boolean systemPropertySubstitution) throws IOException {
        return json(new InputStreamReader(stream, Charset.defaultCharset()), systemPropertySubstitution);
    }

    public static JsonNode json(InputStream stream) throws IOException {
        return json(new InputStreamReader(stream, Charset.defaultCharset()));
    }

    /**
     * Parses a JSON stream
     */
    public static JsonNode json(Reader reader) throws IOException {
        return json(reader, false);
    }

    public static JsonNode json(Reader reader, boolean systemPropertySubstitution) throws IOException {
        StringBuilder bld = new StringBuilder(512);
        int c;
        while ((c = reader.read()) >= 0) {
            bld.append((char) c);
        }
        return json(bld.toString(), systemPropertySubstitution);
    }

    /**
     * Returns a Java object for a json value node based on the node type.
     */
    public static Object valueFromJson(ValueNode node) {
        if (node instanceof NullNode) {
            return null;
        } else {
            if(node instanceof TextNode) {
                return node.textValue();
            } else if(node instanceof BooleanNode) {
                return node.booleanValue();
            } else if(node instanceof NumericNode) {
                return node.numberValue();
            } else {
                throw new RuntimeException("Unsupported node type:"+node.getClass().getName());
            }
        }
    }

    /**
     * Returns a Json value node for the given value. Dates are converted to strings
     */
    public static ValueNode valueToJson(Object value) {
        if(value==null) {
            return JsonNodeFactory.instance.nullNode();
        } else if(value instanceof String) {
            return JsonNodeFactory.instance.textNode((String)value);
        } else if(value instanceof Number) {
            if (value instanceof BigDecimal) {
                return JsonNodeFactory.instance.numberNode((BigDecimal) value);
            } else if (value instanceof BigInteger) {
                return JsonNodeFactory.instance.numberNode((BigInteger) value);
            } else if (value instanceof Double) {
                return JsonNodeFactory.instance.numberNode((Double) value);
            } else if (value instanceof Float) {
                return JsonNodeFactory.instance.numberNode((Float) value);
            } else if (value instanceof Long) {
                return JsonNodeFactory.instance.numberNode((Long) value);
            } else {
                return JsonNodeFactory.instance.numberNode( ((Number)value).intValue());
            }
        } else if(value instanceof Boolean) {
            return JsonNodeFactory.instance.booleanNode( ((Boolean)value).booleanValue());
        } else if(value instanceof Date) {
            return JsonNodeFactory.instance.textNode(Constants.getDateFormat().format((Date)value));
        } else {
            return JsonNodeFactory.instance.textNode(value.toString());
        }            
    }

    /**
     * Converts a json document to an object tree of maps/lists/values
     *
     * If json is a value, then the corresponding Java object is returned.
     *
     * If json is an array, a List is returned. Elements of the list are converted recursively
     *
     * If json is an object, a Map is returned.
     */
    public static Object fromJson(JsonNode json) {
        if(json==null||json instanceof NullNode) {
            return null;
        } else if(json instanceof ObjectNode) {
            return fromJson((ObjectNode)json);
        } else if(json instanceof ArrayNode) {
            return fromJson((ArrayNode)json);
        } else {
            return valueFromJson( (ValueNode)json);
        }
    }

    private static Object fromJson(ObjectNode json) {
        HashMap ret=new HashMap();
        for(Iterator<Map.Entry<String,JsonNode>> itr=json.fields();itr.hasNext();) {
            Map.Entry<String,JsonNode> entry=itr.next();
            ret.put(entry.getKey(),fromJson(entry.getValue()));
        }
        return ret;
    }

    private static Object fromJson(ArrayNode json) {
        ArrayList ret=new ArrayList(json.size());
        for(Iterator<JsonNode> itr=json.elements();itr.hasNext();) {
            ret.add(fromJson(itr.next()));
        }
        return ret;
    }

    /**
     * Converts a Java object tree containing maps and collections to json
     */
    public static JsonNode toJson(Object obj) {
        if(obj==null) {
            return JsonNodeFactory.instance.nullNode();
        } else if(obj instanceof Map) {
            return toJson( (Map)obj );
        } else if(obj instanceof Collection) {
            return toJson( (Collection)obj);
        } else {
            return valueToJson(obj);
        }
    }

    private static JsonNode toJson(Map obj) {
        ObjectNode node=JsonNodeFactory.instance.objectNode();
        for(Iterator<Map.Entry> itr=obj.entrySet().iterator();itr.hasNext();) {
            Map.Entry entry=itr.next();
            node.set((String)entry.getKey(),toJson(entry.getValue()));
        }
        return node;
    }

    private static JsonNode toJson(Collection obj) {
        ArrayNode node=JsonNodeFactory.instance.arrayNode();
        for(Object x:obj) {
            node.add(toJson(x));
        }
        return node;
    }

    /**
     * Pretty print a json doc
     */
    public static String prettyPrint(JsonNode node) {
        StringBuilder bld = new StringBuilder();
        prettyPrint(bld, node);
        return bld.toString();
    }

    /**
     * Pretty print a json doc
     */
    public static void prettyPrint(StringBuilder bld, JsonNode node) {
        toString(bld, node, 0, true);
    }

    /**
     * Utility method to convert array of strings to a json document.
     *
     * @param strings
     * @return
     */
    public static String toJson(String[] strings) {
        return _toJson(strings);
    }

    /**
     * Load a schema from given resourceName.
     *
     * @param resourceName
     * @return the schema
     * @throws ProcessingException
     * @throws IOException
     */
    public static JsonSchema loadSchema(String resourceName) throws ProcessingException, IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        SyntaxValidator validator = factory.getSyntaxValidator();

        JsonNode node = loadJsonNode(resourceName);

        ProcessingReport report = validator.validateSchema(node);
        if (!report.isSuccess()) {
            throw Error.get(UtilConstants.ERR_JSON_SCHEMA_INVALID);
        }

        JsonSchema schema = factory.getJsonSchema("resource:/" + resourceName);
        if (null == schema) {
            throw Error.get(UtilConstants.ERR_JSON_SCHEMA_INVALID);
        }

        return schema;
    }

    /**
     * Validates input node against given schema. Returns NULL if no errors
     * reported, else returns string representing violations.
     *
     * @param schema the json schema (see #loadSchema)
     * @param node the json node to validate
     * @return null if there are no errors, else string with all errors and
     * warnings
     * @throws ProcessingException
     */
    public static String jsonSchemaValidation(JsonSchema schema, JsonNode node) throws ProcessingException, JsonProcessingException {
        ProcessingReport report = schema.validate(node);
        Iterator<ProcessingMessage> i = report.iterator();
        StringBuilder buff = new StringBuilder();
        while (!report.isSuccess() && i != null && i.hasNext()) {
            ProcessingMessage pm = i.next();

            // attempting to pretty print the json
            ObjectMapper mapper = new ObjectMapper();
            String prettyPrintJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pm.asJson());

            buff.append(prettyPrintJson).append("\n\n");
        }

        return report.isSuccess() ? null : buff.toString();
    }

    private static String _toJson(Object[] objects) {
        StringBuilder buff = new StringBuilder("[\"");
        for (int x = 0; x < objects.length; x++) {
            if (objects[x] != null) {
                buff.append(objects[x].toString());
                if (x + 1 < objects.length) {
                    buff.append("\",\"");
                }
            }
        }
        buff.append("\"]");
        return buff.toString();
    }

    private static boolean toString(StringBuilder bld,
                                    JsonNode node,
                                    int depth,
                                    boolean newLine) {
        if (node instanceof ArrayNode) {
            return arrayToString(bld, (ArrayNode) node, depth, newLine);
        } else if (node instanceof ObjectNode) {
            return objectToString(bld, (ObjectNode) node, depth, newLine);
        } else {
            return valueToString(bld, node, depth, newLine);
        }
    }

    private static boolean arrayToString(StringBuilder bld,
                                         ArrayNode node,
                                         int depth,
                                         boolean newLine) {
        if (newLine) {
            indent(bld, depth);
            newLine = false;
        }
        bld.append("[");
        boolean first = true;
        for (Iterator<JsonNode> itr = node.elements();
                itr.hasNext();) {
            if (first) {
                first = false;
            } else {
                bld.append(',');
            }
            newLine = toString(bld, itr.next(), depth + 1, newLine);
        }
        if (newLine) {
            indent(bld, depth);
        }
        bld.append(']');
        return false;
    }

    private static boolean objectToString(StringBuilder bld,
                                          ObjectNode node,
                                          int depth,
                                          boolean newLine) {
        if (newLine) {
            indent(bld, depth);
            newLine = false;
        }
        bld.append('{');
        if (node.size() > 0) {
            bld.append("\n");
            newLine = true;
        }
        boolean first = true;
        for (Iterator<Map.Entry<String, JsonNode>> itr = node.fields();
                itr.hasNext();) {
            if (first) {
                first = false;
            } else {
                if (newLine) {
                    indent(bld, depth);
                }
                bld.append(',');
                bld.append('\n');
            }
            Map.Entry<String, JsonNode> entry = itr.next();
            indent(bld, depth);
            bld.append('\"');
            bld.append(entry.getKey());
            bld.append('\"');
            bld.append(':');
            newLine = toString(bld, entry.getValue(), depth + 1, false);
            if (newLine) {
                indent(bld, depth);
                newLine = false;
            }
        }
        if (node.size() > 0) {
            bld.append('\n');
            newLine = true;
        }
        if (newLine) {
            indent(bld, depth);
        }
        bld.append('}');
        return false;
    }

    private static boolean valueToString(StringBuilder bld,
                                         JsonNode node,
                                         int depth,
                                         boolean newLine) {
        if (newLine) {
            indent(bld, depth);
            newLine = false;
        }
        bld.append(node.toString());
        return newLine;
    }

    private static void indent(StringBuilder bld, int depth) {
        int n = depth * 2;
        for (int i = 0; i < n; i++) {
            bld.append(' ');
        }
    }

    private JsonUtils() {
    }
}
