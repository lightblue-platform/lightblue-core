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

import java.math.BigInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;

public class StringTypeTest {

    Type stringType;

    @Before
    public void setUp() throws Exception {
        stringType = StringType.TYPE;
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
        assertEquals(stringType.getName(), StringType.NAME);
    }

    @Test
    public void testSupportsEq() {
        assertTrue(stringType.supportsEq());
    }

    @Test
    public void testSupportsOrdering() {
        assertTrue(stringType.supportsOrdering());
    }

    @Test
    public void testToJson() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = stringType.toJson(jsonNodeFactory, "json");
        assertTrue(new Boolean(jsonNode.asText().equals("json")));
    }

    @Test
    public void testFromJson() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("textNode");
        Object fromJson = stringType.fromJson(jsonNode);
        assertTrue(fromJson instanceof String);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithIncompatibleValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
        stringType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
        assertNull(stringType.cast(null));
    }

    @Test
    public void testCastTrueString() {
        assertEquals(stringType.cast("true"), "true");
    }

    @Test
    public void testCastFalseString() {
        assertFalse(stringType.cast("false").equals("true"));
    }

    @Test
    public void testCastJsonNode() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("textNode");
        assertTrue(stringType.cast(jsonNode).equals("textNode"));
    }

    @Test
    public void testCompareBothNull() {
        assertEquals(stringType.compare(null, null), 0);
    }

    @Test
    public void testCompareV1Null() {
        assertEquals(stringType.compare(null, new Object()), -1);
    }

    @Test
    public void testCompareV2Null() {
        assertEquals(stringType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(stringType.compare((Object) BigInteger.ONE, (Object) BigInteger.ONE), 0);
    }

    @Test
    public void testCompareNotEqual() {
        assertEquals(stringType.compare((Object) BigInteger.ZERO, (Object) BigInteger.ONE), -1);
    }

    @Test
    public void testEqualsTrue() {
        assertTrue(stringType.equals(StringType.TYPE));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(stringType.equals(Double.MAX_VALUE));
    }

    @Test
    public void testHashCode() {
        assertEquals(stringType.hashCode(), 4);
    }

    @Test
    public void testToString() {
        assertEquals(stringType.toString(), StringType.NAME);
    }

}
