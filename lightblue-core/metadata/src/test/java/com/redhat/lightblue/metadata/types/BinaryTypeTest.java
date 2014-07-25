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
package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import org.junit.Ignore;

public class BinaryTypeTest {

    Type binaryType;

    @Before
    public void setUp() throws Exception {
        binaryType = BinaryType.TYPE;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsAContainerType() {
        assertTrue(ContainerType.class.isAssignableFrom(ArrayType.class));
    }

    @Test
    public void testGetName() {
        assertEquals(binaryType.getName(), BinaryType.NAME);
    }

    @Test
    public void testSupportsEq() {
        assertFalse(binaryType.supportsEq());
    }

    @Test
    public void testSupportsOrdering() {
        assertFalse(binaryType.supportsOrdering());
    }

    @Test
    public void testToJson() throws IOException {
        byte[] bite = new byte[1];
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = binaryType.toJson(jsonNodeFactory, bite);
        assertTrue(Arrays.toString(jsonNode.binaryValue()).equals(Arrays.toString(bite)));
    }

    @Test
    public void testFromJsonString() throws IOException {
        String jsonString = "{\"binaryData\": \"asdf\"}";

        JsonNode node = JsonUtils.json(jsonString);

        assertTrue(node != null);

        JsonNode binaryDataNode = node.get("binaryData");

        assertTrue(binaryDataNode != null);
        byte[] bytes = binaryDataNode.binaryValue();
        assertTrue(bytes != null);
        assertTrue(bytes.length > 0);

        // try to convert back to json, verify we get the exact same thing
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode binaryDataNodeOut = binaryType.toJson(jsonNodeFactory, bytes);
        
        assertTrue(binaryDataNodeOut != null);
        assertEquals("asdf", binaryDataNodeOut.asText());
        assertEquals("\"asdf\"", binaryDataNodeOut.toString());
    }

    @Test
    public void testFromJson() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).binaryNode(new byte[0]);
        Object fromJson = binaryType.fromJson(jsonNode);
        assertTrue(fromJson instanceof byte[]);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithIncompatibleValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
        binaryType.fromJson(jsonNode);
    }

    @Test
    public void testCast() {
        assertNull(binaryType.cast(null));
    }

    @Test(expected = Error.class)
    public void testCastIncompatible() {
        binaryType.cast(new Object());
    }

    @Test(expected = Error.class)
    public void testCastNonByte() {
        binaryType.cast(new String[1]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCompareBothNull() {
        binaryType.compare(null, null);
    }

    @Test
    public void testEqualsTrue() {
        assertTrue(binaryType.equals(BinaryType.TYPE));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(binaryType.equals(Double.MAX_VALUE));
    }

    @Test
    public void testHashCode() {
        assertEquals(binaryType.hashCode(), 8);
    }

    @Test
    public void testToString() {
        assertEquals(binaryType.toString(), BinaryType.NAME);
    }

}
