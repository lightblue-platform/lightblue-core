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

public class BigIntegerTypeTest {

	Type bigIntegerType;
	
	@Before
	public void setUp() throws Exception {
		bigIntegerType = BigIntegerType.TYPE;
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
    	assertEquals(bigIntegerType.getName(), BigIntegerType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(bigIntegerType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertFalse(bigIntegerType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = bigIntegerType.toJson(jsonNodeFactory, BigInteger.ZERO);
    	assertTrue(new BigInteger(jsonNode.asText()).equals(BigInteger.ZERO));    	
    }

    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).numberNode(BigInteger.TEN);
    	Object fromJson = bigIntegerType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof BigInteger);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	bigIntegerType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(bigIntegerType.cast(null));
    }
    
    @Test
    public void castBigIntegerTest() {
    	assertTrue(bigIntegerType.cast(BigInteger.ONE) instanceof BigInteger);
    }

    @Test
    public void castLongTest() {
    	Long laung = Long.MAX_VALUE;
    	assertTrue(bigIntegerType.cast(laung) instanceof BigInteger);
    }
    
    @Test
    public void castBooleanTrueTest() {
    	assertTrue(bigIntegerType.cast(Boolean.TRUE) instanceof BigInteger);
    }
    
    @Test
    public void castBooleanFalseTest() {
    	assertTrue(bigIntegerType.cast(Boolean.FALSE) instanceof BigInteger);
    }
    
    @Test
    public void castGoodStringTest() {
    	assertTrue(bigIntegerType.cast("8675309") instanceof BigInteger);
    }
    
    @Test(expected=Error.class)
    public void castBadStringTest() {
    	bigIntegerType.cast("string");
    }
    
    @Test(expected=Error.class)
    public void castOtherTest() {
    	Object object = new Object();
    	bigIntegerType.cast(object);
    }
    
    @Test
    public void compareBothNullTest() {
    	assertEquals(bigIntegerType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(bigIntegerType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(bigIntegerType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(bigIntegerType.compare((Object)BigInteger.ONE, (Object)BigInteger.ONE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(bigIntegerType.compare((Object)BigInteger.ZERO, (Object)BigInteger.ONE), -1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(bigIntegerType.equals(BigIntegerType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(bigIntegerType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(bigIntegerType.hashCode(), 5);
    }

    @Test
    public void toStringTest() {
    	assertEquals(bigIntegerType.toString(), BigIntegerType.NAME);
    }

}
