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
package com.redhat.lightblue.util.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;

public abstract class AbstractJsonNodeTest {
    protected static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);
    protected JsonDoc jsonDoc;

    /**
     * Load resource as json document.
     *
     * @param resourceName
     * @return the root json node
     * @throws IOException
     */
    public static final JsonNode loadJsonNode(String resourceName) throws IOException {
        return JsonUtils.json(loadResource(resourceName), true);
    }

    /**
     * Load contents of resource on classpath as String using the currentThreads {@link ClassLoader}.
     *
     * @param resourceName - path to resource
     * @return the resource as a String
     * @throws IOException
     */
    public static final String loadResource(String resourceName) throws IOException {
        return loadResource(resourceName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads contents of resource on classpath as String using the passed in {@link ClassLoader}.
     * @param resourceName - path to resource
     * @param loader - {@link ClassLoader} to use
     * @return the resource as a String
     * @throws IOException
     */
    public static final String loadResource(String resourceName, ClassLoader loader) throws IOException {
        try (InputStream is = loader.getResourceAsStream(resourceName)) {
            if (null == is) {
                throw new FileNotFoundException(resourceName);
            }
            return loadResource(is);
        }
    }

    /**
     * Loads contents of resource on classpath as String using the passed in {@link Class}.
     * @param resourceName
     * @param loader - {@link Class} to use.
     * @return the resource as a String
     * @throws IOException
     */
    public static final String loadResource(String resourceName, Class<?> loader) throws IOException {
        try (InputStream is = loader.getResourceAsStream(resourceName)) {
            if (null == is) {
                throw new FileNotFoundException(resourceName);
            }
            return loadResource(is);
        }
    }

    public static final String loadResource(InputStream is) throws IOException {
        StringBuilder buff = new StringBuilder();

        try (InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buff.append(line).append("\n");
            }
        }

        return buff.toString();
    }

    public JsonNode stringArrayNode(String[] expectedValues) {
        ArrayNode expectedNode = JsonNodeFactory.withExactBigDecimals(true).arrayNode();
        for (String value : expectedValues) {
            expectedNode.add(value);
        }
        return expectedNode;
    }

    public JsonNode intArrayNode(Integer[] expectedValues) {
        ArrayNode expectedNode = JsonNodeFactory.withExactBigDecimals(true).arrayNode();
        for (Integer value : expectedValues) {
            expectedNode.add(value);
        }
        return expectedNode;
    }

    public JsonNode doubleArrayNode(Double[] expectedValues) {
        ArrayNode expectedNode = JsonNodeFactory.withExactBigDecimals(true).arrayNode();
        for (Double value : expectedValues) {
            expectedNode.add(value);
        }
        return expectedNode;
    }

    public boolean arrayNodesHaveSameValues(JsonNode expected, JsonNode actual) {
        int i = 0;
        for (Iterator<JsonNode> nodes = expected.elements(); nodes.hasNext(); i++) {

            JsonNode node = nodes.next();

            if (node instanceof TextNode) {
                return textNodesHaveSameValue(node, actual.get(i));
            } else if (node instanceof IntNode) {
                return intNodesHaveSameValue(node, actual.get(i));
            } else if (node instanceof DoubleNode) {
                return doubleNodesHaveSameValue(node, actual.get(i));
            }
        }
        return true;
    }

    public boolean textNodesHaveSameValue(JsonNode expected, JsonNode actual) {
        return expected.asText().equals(actual.asText());
    }

    public boolean intNodesHaveSameValue(JsonNode expected, JsonNode actual) {
        return expected.asInt() == actual.asInt();
    }

    public boolean doubleNodesHaveSameValue(JsonNode expected, JsonNode actual) {
        return expected.asDouble() == actual.asDouble();
    }
}
