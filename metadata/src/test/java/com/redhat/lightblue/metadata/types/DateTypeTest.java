package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.util.Error;

public class DateTypeTest {

	Type dateType;
	
	@Before
	public void setUp() throws Exception {
		dateType = DateType.TYPE;
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
    	assertEquals(dateType.getName(), DateType.NAME);
    }

    @Test
    public void testSupportsEq() {
    	assertTrue(dateType.supportsEq());
    }
    
    @Test
    public void testSupportsOrdering() {
    	assertTrue(dateType.supportsOrdering());
    }

    @Test
    public void testToJson() {
    	DateFormat dateFormat = new SimpleDateFormat(DateType.DATE_FORMAT_STR);
    	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    	
    	String date = dateFormat.format(new Date());
    	JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true); 
    	JsonNode jsonNode = dateType.toJson(jsonNodeFactory, date);
    	assertTrue(jsonNode.asText().equals(date));    	
    }

    @Test
    public void testFromJson() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode(new SimpleDateFormat(DateType.DATE_FORMAT_STR).format(new Date()));
    	Object fromJson = dateType.fromJson(jsonNode);
    	assertTrue(fromJson instanceof Date);
    }
    
    @Test(expected=Error.class)
    public void testFromJsonWithBadValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).textNode("badstring");
    	dateType.fromJson(jsonNode);
    }
    
    @Test(expected=Error.class)
    public void testFromJsonWithIncompatibleValue() {
    	JsonNode jsonNode = JsonNodeFactory.withExactBigDecimals(false).objectNode();
    	dateType.fromJson(jsonNode);
    }

    @Test
    public void testCastNull() {
    	assertNull(dateType.cast(null));
    }
    
    @Test
    public void testCastDate() {
    	assertTrue(dateType.cast(new Date()) instanceof Date);
    }

    @Test
    public void testCastString() {
    	assertTrue(dateType.cast(new SimpleDateFormat(DateType.DATE_FORMAT_STR).format(new Date())) instanceof Date);
    }
    
    @Test(expected=Error.class)
    public void testCastBadString() {
    	assertTrue(dateType.cast("badstring") instanceof Date);
    }
        
    @Test(expected=Error.class)
    public void testCastOther() {
    	Object object = new Object();
    	dateType.cast(object);
    }
    
    @Test
    public void testCompareBothNull() {
    	assertEquals(dateType.compare(null, null), 0);
    }
    
    @Test
    public void testCompareV1Null() {
    	assertEquals(dateType.compare(null, new Object()), -1);
    }
    
    @Test
    public void testCompareV2Null() {
    	assertEquals(dateType.compare(new Object(), null), 1);
    }

    @Test
    public void testCompareEqual() {
    	Date date = new Date();
    	assertEquals(dateType.compare(date, date), 0);
    }
    
    @Test
    public void testCcompareNotEqual() {
    	assertEquals(dateType.compare(new Date(), new Date()), 0);
    }
    
    @Test
    public void testEqualsTrue() {
    	assertTrue(dateType.equals(DateType.TYPE));
    }
    
    @Test
    public void testEqualsFalse() {
    	assertFalse(dateType.equals(Double.MAX_VALUE));
    }
    
    @Test
    public void testHashCode() {
    	assertEquals(dateType.hashCode(), 7);
    }

    @Test
    public void testToString() {
    	assertEquals(dateType.toString(), DateType.NAME);
    }

}
