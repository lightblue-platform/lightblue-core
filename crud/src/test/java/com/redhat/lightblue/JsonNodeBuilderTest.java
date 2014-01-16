package com.redhat.lightblue;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.query.QueryExpression;

public class JsonNodeBuilderTest {

	JsonNodeBuilder builder;
	
	@Before
	public void setUp() throws Exception {
		builder = new JsonNodeBuilder();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIncludeNullsDefault() {
		assertFalse(builder.includeNulls());
	}

	@Test
	public void testIncludeNullsTrue() {
		builder.includeNulls(true);
		assertTrue(builder.includeNulls());
	}
	
	@Test
	public void testAddString() {
		builder.add("test", "testString");
		assertTrue("testString".equals(builder.build().get("test").asText()));
	}
	
	@Test
	public void testAddStringNull() {
		builder.add("test", (String) null);
		assertFalse("testString".equals(builder.build().asText()));
	}

	@Test
	public void testAddQueryExpression() {
	}
	
	@Test
	public void testAddQueryExpressionNull() {
		builder.add("query", (QueryExpression) null);
		assertNull(builder.build().get("query"));
	}

	@Test
	public void testAddProjection() {
		
	}

	@Test
	public void testAddSort() {
		
	}

	@Test
	public void testAddLong() {
		
	}

	@Test
	public void testAddBoolean() {
		
	}

	@Test
	public void testAddOperationStatus() {
		
	}

	@Test
	public void testAddEntityVersion() {
		
	}

	@Test
	public void testAddClientIdentification() {
		
	}

	@Test
	public void testAddExecutionOptions() {
		
	}

	@Test
	public void testAddSessionInfo() {
		
	}

	@Test
	public void testAddUpdateExpression() {
		
	}

	@Test
	public void testAddListOfT() {
		
	}

	@Test
	public void testBuild() {
		assertTrue(builder.build() instanceof JsonNode);
	}

}
