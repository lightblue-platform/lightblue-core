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
    public void getNameTest() {
    	assertEquals(doubleType.getName(), DoubleType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(doubleType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertTrue(doubleType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = doubleType.toJson(jsonNodeFactory, Double.MAX_VALUE);
    	assertTrue(new Double(jsonNode.asDouble()).equals(Double.MAX_VALUE));
    }

    @Test
    public void toJsonNullTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
    	JsonNode jsonNode = doubleType.toJson(jsonNodeFactory, null);
    	assertNotNull(jsonNode);
    }
    
    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(true).numberNode(Double.MAX_VALUE);
    	Object fromJson = doubleType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Double);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	doubleType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(doubleType.cast(null));
    }
    
    @Test
    public void castBigDecimalTest() {
    	assertTrue(doubleType.cast(Double.MAX_VALUE) instanceof Double);
    }
    
    @Test
    public void castDoubleTest() {
    	assertTrue(doubleType.cast(Double.MAX_VALUE) instanceof Double);
    }
    
    @Test
    public void castFloatTest() {
    	assertTrue(doubleType.cast(Float.MAX_VALUE) instanceof Double);
    }

    @Test
    public void castLongTest() {
    	Long laung = Long.MAX_VALUE;
    	assertTrue(doubleType.cast(laung) instanceof Double);
    }
    
    @Test
    public void castBooleanTrueTest() {
    	assertTrue(doubleType.cast(Boolean.TRUE) instanceof Double);
    }
    
    @Test
    public void castBooleanFalseTest() {
    	assertTrue(doubleType.cast(Boolean.FALSE) instanceof Double);
    }
    
    @Test
    public void castGoodStringTest() {
    	assertTrue(doubleType.cast("8675309") instanceof Double);
    }
    
    @Test(expected=Error.class)
    public void castBadStringTest() {
    	doubleType.cast("string");
    }
    
    @Test(expected=Error.class)
    public void castOtherTest() {
    	doubleType.cast(new Object());
    }
    
    @Test
    public void compareBothNullTest() {
    	assertEquals(doubleType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(doubleType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(doubleType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(doubleType.compare((Object)Double.MAX_VALUE, (Object)Double.MAX_VALUE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(doubleType.compare((Object)BigDecimal.ZERO, (Object)Double.MAX_VALUE), -1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(doubleType.equals(DoubleType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(doubleType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(doubleType.hashCode(), 3);
    }

    @Test
    public void toStringTest() {
    	assertEquals(doubleType.toString(), DoubleType.NAME);
    }

}
