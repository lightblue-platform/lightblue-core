package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
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
    public void getNameTest() {
    	assertEquals(binaryType.getName(), BinaryType.NAME);
    }

    @Test
    public void supportsEqTest() {
    	assertFalse(binaryType.supportsEq());
    }
    
    @Test
    public void supportsOrderingTest() {
    	assertFalse(binaryType.supportsOrdering());
    }

    @Test
    public void toJsonTest() throws IOException {
    	byte[] bite = new byte[1];
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = binaryType.toJson(jsonNodeFactory, bite);
    	assertTrue(Arrays.toString(jsonNode.binaryValue()).equals(Arrays.toString(bite)));    	
    }

    @Test
    public void fromJsonTest() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).binaryNode(new byte[0]);
    	Object fromJson = binaryType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof byte[]);
    }
    
    @Test(expected=Error.class)
    public void fromJsonTestWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	binaryType.fromJson(jsonNode);
    }

    @Test
    public void castNull() {
    	assertNull(binaryType.cast(null));
    }
           
    @Test(expected=Error.class)
    public void castIncompatibleTest() {
    	binaryType.cast(new Object());
    }
    
    @Test(expected=Error.class)
    public void castNonByteTest() {
    	binaryType.cast(new String[1]);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void compareBothNullTest() {
    	binaryType.compare(null, null);
    }
       
    @Test
    public void equalsTrueTest() {
    	assertTrue(binaryType.equals(BinaryType.TYPE));
    }
    
    @Test
    public void equalsFalseTest() {
    	assertFalse(binaryType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void hashCodeTest() {
    	assertEquals(binaryType.hashCode(), 8);
    }

    @Test
    public void toStringTest() {
    	assertEquals(binaryType.toString(), BinaryType.NAME);
    }

}
