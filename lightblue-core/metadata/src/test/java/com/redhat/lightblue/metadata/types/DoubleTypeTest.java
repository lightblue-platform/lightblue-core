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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;

public class DoubleTypeTest {

    Type doubleType;

    @Before
    public void setUp() throws Exception {
        doubleType = DoubleType.TYPE;
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
        assertEquals(doubleType.getName(), DoubleType.NAME);
    }

    @Test
    public void testSupportsEq() {
        assertTrue(doubleType.supportsEq());
    }

    @Test
    public void testSupportsOrdering() {
        assertTrue(doubleType.supportsOrdering());
    }

    @Test
    public void testToJson() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = doubleType.toJson(jsonNodeFactory, Double.MAX_VALUE);
        assertTrue(new Double(jsonNode.asDouble()).equals(Double.MAX_VALUE));
    }

    @Test
    public void testToJsonNull() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = doubleType.toJson(jsonNodeFactory, null);
        assertNotNull(jsonNode);
    }

    @Test
    public void testFromJson() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(true).numberNode(Double.MAX_VALUE);
        Object fromJson = doubleType.fromJson(jsonNode);
        assertTrue(fromJson instanceof Double);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithIncompatibleValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
        doubleType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
        assertNull(doubleType.cast(null));
    }

    @Test
    public void testCastBigDecimal() {
        assertTrue(doubleType.cast(Double.MAX_VALUE) instanceof Double);
    }

    @Test
    public void testCastDouble() {
        assertTrue(doubleType.cast(Double.MAX_VALUE) instanceof Double);
    }

    @Test
    public void testCastFloat() {
        assertTrue(doubleType.cast(Float.MAX_VALUE) instanceof Double);
    }

    @Test
    public void testCastLong() {
        Long laung = Long.MAX_VALUE;
        assertTrue(doubleType.cast(laung) instanceof Double);
    }

    @Test
    public void testCastBooleanTrue() {
        assertTrue(doubleType.cast(Boolean.TRUE) instanceof Double);
    }

    @Test
    public void testCastBooleanFalse() {
        assertTrue(doubleType.cast(Boolean.FALSE) instanceof Double);
    }

    @Test
    public void testCastGoodString() {
        assertTrue(doubleType.cast("8675309") instanceof Double);
    }

    @Test(expected = Error.class)
    public void testCastBadString() {
        doubleType.cast("string");
    }

    @Test(expected = Error.class)
    public void testCcastOther() {
        doubleType.cast(new Object());
    }

    @Test
    public void testCompareBothNull() {
        assertEquals(doubleType.compare(null, null), 0);
    }

    @Test
    public void testCompareV1Null() {
        assertEquals(doubleType.compare(null, new Object()), -1);
    }

    @Test
    public void testCompareV2Null() {
        assertEquals(doubleType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(doubleType.compare((Object) Double.MAX_VALUE, (Object) Double.MAX_VALUE), 0);
    }

    @Test
    public void testCompareNotEqual() {
        assertEquals(doubleType.compare((Object) BigDecimal.ZERO, (Object) Double.MAX_VALUE), -1);
    }

    @Test
    public void testEequalsTrue() {
        assertTrue(doubleType.equals(DoubleType.TYPE));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(doubleType.equals(Double.MAX_VALUE));
    }

    @Test
    public void testHashCode() {
        assertEquals(doubleType.hashCode(), 3);
    }

    @Test
    public void testToString() {
        assertEquals(doubleType.toString(), DoubleType.NAME);
    }

}
