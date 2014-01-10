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
    public void getNameTest() {
    	assertEquals(integerType.getName(), IntegerType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(integerType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertTrue(integerType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = integerType.toJson(jsonNodeFactory, Integer.MAX_VALUE);
    	assertTrue(new Integer(jsonNode.asText()).equals(Integer.MAX_VALUE));    	
    }

    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).numberNode(Integer.MAX_VALUE);
    	Object fromJson = integerType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Long);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	integerType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(integerType.cast(null));
    }
    
    @Test
    public void castBigIntegerTest() {
    	assertTrue(integerType.cast(Integer.MAX_VALUE) instanceof Long);
    }

    @Test
    public void castLongTest() {
    	assertTrue(integerType.cast(Long.MAX_VALUE) instanceof Long);
    }
    
    @Test
    public void castBooleanTrueTest() {
    	assertTrue(integerType.cast(Boolean.TRUE) instanceof Long);
    }
    
    @Test
    public void castBooleanFalseTest() {
    	assertTrue(integerType.cast(Boolean.FALSE) instanceof Long);
    }
    
    @Test
    public void castGoodStringTest() {
    	assertTrue(integerType.cast("8675309") instanceof Long);
    }
    
    @Test(expected=Error.class)
    public void castBadStringTest() {
    	integerType.cast("string");
    }
    
    @Test(expected=Error.class)
    public void castOtherTest() {
    	Object object = new Object();
    	integerType.cast(object);
    }
    
    @Test
    public void compareBothNullTest() {
    	assertEquals(integerType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(integerType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(integerType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(integerType.compare((Object)Integer.MAX_VALUE, (Object)Integer.MAX_VALUE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(integerType.compare((Object)Integer.MAX_VALUE, (Object)Integer.MIN_VALUE), 1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(integerType.equals(IntegerType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(integerType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(integerType.hashCode(), 2);
    }

    @Test
    public void toStringTest() {
    	assertEquals(integerType.toString(), IntegerType.NAME);
    }

}
