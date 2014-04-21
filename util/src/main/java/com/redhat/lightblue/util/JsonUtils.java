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

import java.util.Map;
import java.util.Iterator;

import java.nio.charset.Charset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Generic utilities for dealing with JSON
 */
public final class JsonUtils {

    /**
     * Returns an object mapper to parse JSON text
     */
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        return mapper;
    }

    /**
     * Parses a string and retruns a JSON tree
     */
    public static JsonNode json(String s)
            throws IOException {
        return getObjectMapper().readTree(s);
    }

    /**
     * Parses a JSON stream
     */
    public static JsonNode json(InputStream stream) throws IOException {
        return json((Reader)new InputStreamReader(stream, Charset.defaultCharset()));
    }

    /**
     * Parses a JSON stream
     */
    public static JsonNode json(Reader reader) throws IOException {
        StringBuilder bld=new StringBuilder(512);
        int c;
        while( (c=reader.read()) >= 0 )
            bld.append((char)c);
        return json(bld.toString());
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
        if (node.size() > 0) {
            bld.append("{\n");
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
                    newLine = false;
                }
                bld.append(',');
                bld.append('\n');
                newLine = true;
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
            newLine = false;
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
