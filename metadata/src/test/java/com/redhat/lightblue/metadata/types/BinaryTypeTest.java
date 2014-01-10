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
import com.redhat.lightblue.util.Error;

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
