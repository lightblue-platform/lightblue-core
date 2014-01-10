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

public class BooleanTypeTest {

	Type booleanType;
	
	@Before
	public void setUp() throws Exception {
		booleanType = BooleanType.TYPE;
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
    	assertEquals(booleanType.getName(), BooleanType.NAME);
    }

    @Test
    public void testSupportsEq() {
    	assertTrue(booleanType.supportsEq());
    }
    
    @Test
    public void testSupportsOrdering() {
    	assertTrue(booleanType.supportsOrdering());
    }

    @Test
    public void testToJson() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = booleanType.toJson(jsonNodeFactory, Boolean.TRUE);
    	assertTrue(new Boolean(jsonNode.asBoolean()).equals(Boolean.TRUE));    	
    }

    @Test
    public void testFromJson() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).numberNode(BigInteger.TEN);
    	Object fromJson = booleanType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Boolean);
    }
    
    @Test(expected=Error.class)
    public void testFromJsonWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	booleanType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
    	assertNull(booleanType.cast(null));
    }
    
    @Test
    public void testCastBigInteger() {
    	assertTrue(booleanType.cast(BigInteger.ONE) instanceof Boolean);
    }

    @Test
    public void testCastLong() {
    	Long laung = Long.MAX_VALUE;
    	assertTrue(booleanType.cast(laung) instanceof Boolean);
    }
    
    @Test
    public void testCastBooleanTrue() {
    	assertTrue(booleanType.cast(Boolean.TRUE) instanceof Boolean);
    }
    
    @Test
    public void testCastBooleanFalse() {
    	assertTrue(booleanType.cast(Boolean.FALSE) instanceof Boolean);
    }
        
    @Test
    public void testCastTrueString() {
    	assertEquals(booleanType.cast(Boolean.TRUE.toString()), Boolean.TRUE);
    }
    
    @Test
    public void testCastFalseString() {
    	assertEquals(booleanType.cast(Boolean.FALSE.toString()), Boolean.FALSE);
    }
    
    @Test(expected=Error.class)
    public void testCastOther() {
    	Object object = new Object();
    	booleanType.cast(object);
    }
    
    @Test
    public void testCompareBothNull() {
    	assertEquals(booleanType.compare(null, null), 0);
    }
    
    @Test
    public void testCompareV1Null() {
    	assertEquals(booleanType.compare(null, new Object()), -1);
    }
    
    @Test
    public void testCompareV2Null() {
    	assertEquals(booleanType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
    	assertEquals(booleanType.compare((Object)BigInteger.ONE, (Object)BigInteger.ONE), 0);
    }
    
    @Test
    public void testCompareNotEqual() {
    	assertEquals(booleanType.compare((Object)BigInteger.ZERO, (Object)BigInteger.ONE), -1);
    }
    
    @Test
    public void testEqualsTrue() {
    	assertTrue(booleanType.equals(BooleanType.TYPE));
    }
    
    @Test
    public void testEqualsFalse() {
    	assertFalse(booleanType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void testHashCode() {
    	assertEquals(booleanType.hashCode(), 1);
    }

    @Test
    public void testToString() {
    	assertEquals(booleanType.toString(), BooleanType.NAME);
    }

}
