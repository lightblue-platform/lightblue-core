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
import com.redhat.lightblue.util.Error;

public class BigDecimalTypeTest {

    Type bigDecimalType;

    @Before
    public void setUp() throws Exception {
        bigDecimalType = BigDecimalType.TYPE;
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
        assertEquals(bigDecimalType.getName(), BigDecimalType.NAME);
    }

    @Test
    public void testSupportsEq() {
        assertTrue(bigDecimalType.supportsEq());
    }

    @Test
    public void testSupportsOrdering() {
        assertFalse(bigDecimalType.supportsOrdering());
    }

    @Test
    public void testToJson() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = bigDecimalType.toJson(jsonNodeFactory, Double.MAX_VALUE);
        assertTrue(new Double(jsonNode.asDouble()).equals(Double.MAX_VALUE));
    }

    @Test
    public void testToJsonNull() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        JsonNode jsonNode = bigDecimalType.toJson(jsonNodeFactory, null);
        assertNotNull(jsonNode);
    }

    @Test
    public void testFromJson() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(true).numberNode(BigDecimal.TEN);
        Object fromJson = bigDecimalType.fromJson(jsonNode);
        assertTrue(fromJson instanceof BigDecimal);
    }

    @Test(expected = Error.class)
    public void testFromJsonWithIncompatibleValue() {
        JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
        bigDecimalType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
        assertNull(bigDecimalType.cast(null));
    }

    @Test
    public void testCastBigDecimal() {
        assertTrue(bigDecimalType.cast(BigDecimal.TEN) instanceof BigDecimal);
    }

    @Test
    public void testCastDouble() {
        assertTrue(bigDecimalType.cast(Double.MAX_VALUE) instanceof BigDecimal);
    }

    @Test
    public void testCastFloat() {
        assertTrue(bigDecimalType.cast(Float.MAX_VALUE) instanceof BigDecimal);
    }

    @Test
    public void testCastLong() {
        assertTrue(bigDecimalType.cast(Long.MAX_VALUE) instanceof BigDecimal);
    }

    @Test
    public void testCastBooleanTrue() {
        assertTrue(bigDecimalType.cast(Boolean.TRUE) instanceof BigDecimal);
    }

    @Test
    public void testCastBooleanFalse() {
        assertTrue(bigDecimalType.cast(Boolean.FALSE) instanceof BigDecimal);
    }

    @Test
    public void testCastGoodString() {
        assertTrue(bigDecimalType.cast(String.valueOf(BigDecimal.TEN)) instanceof BigDecimal);
    }

    @Test(expected = Error.class)
    public void testCastBadString() {
        bigDecimalType.cast("string");
    }

    @Test(expected = Error.class)
    public void testCastOtherTest() {
        bigDecimalType.cast(new Object());
    }

    @Test
    public void testCompareBothNull() {
        assertEquals(bigDecimalType.compare(null, null), 0);
    }

    @Test
    public void testCompareV1Null() {
        assertEquals(bigDecimalType.compare(null, new Object()), -1);
    }

    @Test
    public void testCompareV2Null() {
        assertEquals(bigDecimalType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
        assertEquals(bigDecimalType.compare((Object) BigDecimal.ONE, (Object) BigDecimal.ONE), 0);
    }

    @Test
    public void testCompareNotEqual() {
        assertEquals(bigDecimalType.compare((Object) BigDecimal.ZERO, (Object) BigDecimal.ONE), -1);
    }

    @Test
    public void testEqualsTrue() {
        assertTrue(bigDecimalType.equals(BigDecimalType.TYPE));
    }

    @Test
    public void testEqualsFalse() {
        assertFalse(bigDecimalType.equals(Double.MAX_VALUE));
    }

    @Test
    public void testHashCode() {
        assertEquals(bigDecimalType.hashCode(), 6);
    }

    @Test
    public void testToString() {
        assertEquals(bigDecimalType.toString(), BigDecimalType.NAME);
    }

}
