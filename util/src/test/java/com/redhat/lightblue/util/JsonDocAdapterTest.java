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

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class JsonDocAdapterTest extends AbstractJsonNodeTest {

    protected JsonNode createJsonNode() {
        try {
            return loadJsonNode("test-JsonDocAdapter.json");
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void getNumChildren() {
        JsonNode n = createJsonNode();
        int expected = n.size();

        JsonDocAdapter a = new JsonDocAdapter();
        int actual = a.getNumChildren(n);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Recursive test.
     * 
     * @param location
     *            where are we?
     * @param n
     * @param a
     */
    public boolean[] testGetChild(String location, JsonNode n, JsonDocAdapter a) {
        boolean testedString = false;
        boolean testedInteger = false;

        // string test is for object children
        Iterator<String> fieldNames = n.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            JsonNode expected = n.get(fieldName);

            try {
                JsonNode actual = a.getChild(n, fieldName);
                testedString |= true;

                Assert.assertEquals(expected, actual);

                if (expected.size() > 0) {
                    boolean[] x = testGetChild(location + "." + fieldName, expected, a);
                    testedString |= x[0];
                    testedInteger |= x[1];
                }
            } catch (InvalidArrayAccessException e) {
                if (!(expected instanceof ObjectNode)) {
                    // expected
                } else {
                    // unexpected
                    throw e;
                }
            }
        }

        // integer test is for array children
        for (int i = 0; i < n.size(); i++) {
            JsonNode expected = n.get(i);

            try {
                JsonNode actual = a.getChild(n, i);
                testedInteger |= true;

                Assert.assertEquals(expected, actual);

                if (expected.size() > 0) {
                    boolean[] x = testGetChild(location + "." + i, expected, a);
                    testedString |= x[0];
                    testedInteger |= x[1];
                }
            } catch (InvalidArrayAccessException e) {
                if (!(expected instanceof ArrayNode)) {
                    // expected
                } else {
                    // unexpected
                    throw e;
                }
            }
        }

        return new boolean[] { testedString, testedInteger };
    }

    @Test
    public void getChildren() {
        JsonNode current = createJsonNode();
        JsonDocAdapter a = new JsonDocAdapter();

        Assert.assertTrue(current instanceof ObjectNode);

        Iterator<JsonNode> i = a.getChildren(current);

        Assert.assertNotNull(i);
        Assert.assertTrue(i.hasNext());

        boolean testedArray = false;

        while (i.hasNext()) {
            JsonNode child = i.next();

            if (child instanceof ArrayNode) {
                Iterator<JsonNode> ci = a.getChildren(child);
                Assert.assertNotNull(ci);
                Assert.assertTrue(ci.hasNext());
                testedArray = true;
            }
        }

        Assert.assertTrue(testedArray);
    }

    @Test
    public void getChild() {
        JsonNode n = createJsonNode();
        JsonDocAdapter a = new JsonDocAdapter();

        boolean[] x = testGetChild("root", n, a);

        Assert.assertTrue("didn't test string", x[0]);
        Assert.assertTrue("didn't test int", x[1]);
    }

    @Test
    public void acceptsAny() {
        JsonNode n = createJsonNode();
        JsonDocAdapter a = new JsonDocAdapter();

        Assert.assertFalse(a.acceptsAny(n));
    }

    @Test
    public void acceptsIndex() {
        JsonNode n = createJsonNode();
        JsonDocAdapter a = new JsonDocAdapter();

        Iterator<JsonNode> i = a.getChildren(n);
        
        boolean testedArray = false;
        
        while (i.hasNext()) {
            JsonNode current = i.next();

            if (current instanceof ArrayNode) {
                Assert.assertTrue(a.acceptsIndex(current));
                testedArray = true;
            } else {
                Assert.assertFalse(a.acceptsIndex(current));
            }
        }
        
        Assert.assertTrue(testedArray);
    }

    @Test
    public void acceptsName() {
        JsonNode n = createJsonNode();
        JsonDocAdapter a = new JsonDocAdapter();

        Iterator<JsonNode> i = a.getChildren(n);
        
        boolean testedObject = false;
        
        while (i.hasNext()) {
            JsonNode current = i.next();

            if (current instanceof ObjectNode) {
                Assert.assertTrue(a.acceptsName(current));
                testedObject = true;
            } else {
                Assert.assertFalse(a.acceptsName(current));
            }
        }
        
        Assert.assertTrue(testedObject);
    }

}
