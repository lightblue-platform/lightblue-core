package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.util.Error;

public class IntegerTypeTest {

	Type integerType;
	
	@Before
	public void setUp() throws Exception {
		integerType = IntegerType.TYPE;
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
    	assertEquals(integerType.getName(), IntegerType.NAME);
    }

    @Test
    public void testSupportsEq() {
    	assertTrue(integerType.supportsEq());
    }
    
    @Test
    public void testSupportsOrdering() {
    	assertTrue(integerType.supportsOrdering());
    }

    @Test
    public void testToJson() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = integerType.toJson(jsonNodeFactory, Integer.MAX_VALUE);
    	assertTrue(new Integer(jsonNode.asText()).equals(Integer.MAX_VALUE));    	
    }

    @Test
    public void testFromJson() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).numberNode(Integer.MAX_VALUE);
    	Object fromJson = integerType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Long);
    }
    
    @Test(expected=Error.class)
    public void testFromJsonWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	integerType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
    	assertNull(integerType.cast(null));
    }
    
    @Test
    public void testCastBigInteger() {
    	assertTrue(integerType.cast(Integer.MAX_VALUE) instanceof Long);
    }

    @Test
    public void testCastLong() {
    	assertTrue(integerType.cast(Long.MAX_VALUE) instanceof Long);
    }
    
    @Test
    public void testCastBooleanTrue() {
    	assertTrue(integerType.cast(Boolean.TRUE) instanceof Long);
    }
    
    @Test
    public void testCastBooleanFalse() {
    	assertTrue(integerType.cast(Boolean.FALSE) instanceof Long);
    }
    
    @Test
    public void testCastGoodString() {
    	assertTrue(integerType.cast(String.valueOf(Long.MAX_VALUE)) instanceof Long);
    }
    
    @Test(expected=Error.class)
    public void testCastBadString() {
    	integerType.cast("badstring");
    }
    
    @Test(expected=Error.class)
    public void testCastOther() {
    	Object object = new Object();
    	integerType.cast(object);
    }
    
    @Test
    public void testCompareBothNull() {
    	assertEquals(integerType.compare(null, null), 0);
    }
    
    @Test
    public void testCompareV1Null() {
    	assertEquals(integerType.compare(null, new Object()), -1);
    }
    
    @Test
    public void testCompareV2Null() {
    	assertEquals(integerType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
    	assertEquals(integerType.compare((Object)Integer.MAX_VALUE, (Object)Integer.MAX_VALUE), 0);
    }
    
    @Test
    public void testCompareNotEqual() {
    	assertEquals(integerType.compare((Object)Integer.MAX_VALUE, (Object)Integer.MIN_VALUE), 1);
    }
    
    @Test
    public void testEqualsTrue() {
    	assertTrue(integerType.equals(IntegerType.TYPE));
    }
    
    @Test
    public void testEqualsFalse() {
    	assertFalse(integerType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void testHashCode() {
    	assertEquals(integerType.hashCode(), 2);
    }

    @Test
    public void testToString() {
    	assertEquals(integerType.toString(), IntegerType.NAME);
    }

}
