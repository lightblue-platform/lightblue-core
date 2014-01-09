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
    public void getNameTest() {
    	assertEquals(booleanType.getName(), BooleanType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(booleanType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertTrue(booleanType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = booleanType.toJson(jsonNodeFactory, Boolean.TRUE);
    	assertTrue(new Boolean(jsonNode.asBoolean()).equals(Boolean.TRUE));    	
    }

    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).numberNode(BigInteger.TEN);
    	Object fromJson = booleanType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Boolean);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	booleanType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(booleanType.cast(null));
    }
    
    @Test
    public void castBigIntegerTest() {
    	assertTrue(booleanType.cast(BigInteger.ONE) instanceof Boolean);
    }

    @Test
    public void castLongTest() {
    	Long laung = Long.MAX_VALUE;
    	assertTrue(booleanType.cast(laung) instanceof Boolean);
    }
    
    @Test
    public void castBooleanTrueTest() {
    	assertTrue(booleanType.cast(Boolean.TRUE) instanceof Boolean);
    }
    
    @Test
    public void castBooleanFalseTest() {
    	assertTrue(booleanType.cast(Boolean.FALSE) instanceof Boolean);
    }
        
    @Test
    public void castTrueStringTest() {
    	assertEquals(booleanType.cast("true"), Boolean.TRUE);
    }
    
    @Test
    public void castFalseStringTest() {
    	assertEquals(booleanType.cast("false"), Boolean.FALSE);
    }
    
    @Test(expected=Error.class)
    public void castOtherTest() {
    	Object object = new Object();
    	booleanType.cast(object);
    }
    
    @Test
    public void compareBothNullTest() {
    	assertEquals(booleanType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(booleanType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(booleanType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(booleanType.compare((Object)BigInteger.ONE, (Object)BigInteger.ONE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(booleanType.compare((Object)BigInteger.ZERO, (Object)BigInteger.ONE), -1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(booleanType.equals(BooleanType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(booleanType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(booleanType.hashCode(), 1);
    }

    @Test
    public void toStringTest() {
    	assertEquals(booleanType.toString(), BooleanType.NAME);
    }

}
