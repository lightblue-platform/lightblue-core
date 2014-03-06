package com.redhat.lightblue.util;

import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

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
