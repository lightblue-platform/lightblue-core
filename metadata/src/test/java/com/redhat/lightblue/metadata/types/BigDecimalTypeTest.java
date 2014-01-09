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
    public void getNameTest() {
    	assertEquals(bigDecimalType.getName(), BigDecimalType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(bigDecimalType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertFalse(bigDecimalType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = bigDecimalType.toJson(jsonNodeFactory, Double.MAX_VALUE);
    	assertTrue(new Double(jsonNode.asDouble()).equals(Double.MAX_VALUE));
    }

    @Test
    public void toJsonNullTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
    	JsonNode jsonNode = bigDecimalType.toJson(jsonNodeFactory, null);
    	assertNotNull(jsonNode);
    }
    
    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(true).numberNode(BigDecimal.TEN);
    	Object fromJson = bigDecimalType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof BigDecimal);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	bigDecimalType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(bigDecimalType.cast(null));
    }
    
    @Test
    public void castBigDecimalTest() {
    	assertTrue(bigDecimalType.cast(BigDecimal.TEN) instanceof BigDecimal);
    }
    
    @Test
    public void castDoubleTest() {
    	assertTrue(bigDecimalType.cast(Double.MAX_VALUE) instanceof BigDecimal);
    }
    
    @Test
    public void castFloatTest() {
    	assertTrue(bigDecimalType.cast(Float.MAX_VALUE) instanceof BigDecimal);
    }

    @Test
    public void castLongTest() {
    	Long laung = Long.MAX_VALUE;
    	assertTrue(bigDecimalType.cast(laung) instanceof BigDecimal);
    }
    
    @Test
    public void castBooleanTrueTest() {
    	assertTrue(bigDecimalType.cast(Boolean.TRUE) instanceof BigDecimal);
    }
    
    @Test
    public void castBooleanFalseTest() {
    	assertTrue(bigDecimalType.cast(Boolean.FALSE) instanceof BigDecimal);
    }
    
    @Test
    public void castGoodStringTest() {
    	assertTrue(bigDecimalType.cast("8675309") instanceof BigDecimal);
    }
    
    @Test(expected=Error.class)
    public void castBadStringTest() {
    	bigDecimalType.cast("string");
    }
    
    @Test(expected=Error.class)
    public void castOtherTest() {
    	bigDecimalType.cast(new Object());
    }
    
    @Test
    public void compareBothNullTest() {
    	assertEquals(bigDecimalType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(bigDecimalType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(bigDecimalType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(bigDecimalType.compare((Object)BigDecimal.ONE, (Object)BigDecimal.ONE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(bigDecimalType.compare((Object)BigDecimal.ZERO, (Object)BigDecimal.ONE), -1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(bigDecimalType.equals(BigDecimalType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(bigDecimalType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(bigDecimalType.hashCode(), 6);
    }

    @Test
    public void toStringTest() {
    	assertEquals(bigDecimalType.toString(), BigDecimalType.NAME);
    }

}
