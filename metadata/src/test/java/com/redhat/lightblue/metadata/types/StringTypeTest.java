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

public class StringTypeTest {

	Type stringType;
	
	@Before
	public void setUp() throws Exception {
		stringType = StringType.TYPE;
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
    	assertEquals(stringType.getName(), StringType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertTrue(stringType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertTrue(stringType.supportsOrdering());
    }

    @Test
    public void toJsonTest() {
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = stringType.toJson(jsonNodeFactory, "json");
    	assertTrue(new Boolean(jsonNode.asText().equals("json")));    	
    }

    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("textNode");
    	Object fromJson = stringType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof String);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	stringType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(stringType.cast(null));
    }
                
    @Test
    public void castTrueStringTest() {
    	assertEquals(stringType.cast("true"), "true");
    }
    
    @Test
    public void castFalseStringTest() {
    	assertFalse(stringType.cast("false").equals("true"));
    }
    
    @Test
    public void castJsonNodeTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("textNode");
    	assertTrue(stringType.cast(jsonNode).equals("textNode"));
    }
        
    @Test
    public void compareBothNullTest() {
    	assertEquals(stringType.compare(null, null), 0);
    }
    
    @Test
    public void compareV1NullTest() {
    	assertEquals(stringType.compare(null, new Object()), -1);
    }
    
    @Test
    public void compareV2NullTest() {
    	assertEquals(stringType.compare(new Object(), null), 1);
    }

    @Test
    public void compareEqualTest() {
    	assertEquals(stringType.compare((Object)BigInteger.ONE, (Object)BigInteger.ONE), 0);
    }
    
    @Test
    public void compareNotEqualTest() {
    	assertEquals(stringType.compare((Object)BigInteger.ZERO, (Object)BigInteger.ONE), -1);
    }
    
    @Test
    public void equalsTrueTest() {
    	assertTrue(stringType.equals(StringType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(stringType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(stringType.hashCode(), 4);
    }

    @Test
    public void toStringTest() {
    	assertEquals(stringType.toString(), StringType.NAME);
    }

}
