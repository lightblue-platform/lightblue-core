package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferenceTest {

	Reference reference;
	
	public static String ENTITY_FIELD_VALUE = "entityField"; 
	public static String ENTITY_NAME_VALUE = "entityName";
	public static String THIS_FIELD_VALUE = "thisField";
	public static String VERSION_VALUE = "versionValue";
		
	@Before
	public void setUp() throws Exception {
		reference = new Reference();
		reference.setEntityField(ENTITY_FIELD_VALUE);
		reference.setEntityName(ENTITY_NAME_VALUE);
		reference.setThisField(THIS_FIELD_VALUE);
		reference.setVersionValue(VERSION_VALUE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetEntityName() {
		assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE));
	}

	@Test
	public void testSetEntityName() {
		reference.setEntityName(ENTITY_NAME_VALUE+1);
		assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE+1));
	}

	@Test
	public void testGetVersionValue() {
		assertTrue(reference.getVersionValue().equals(VERSION_VALUE));
	}

	@Test
	public void testSetVersionValue() {
		reference.setVersionValue(VERSION_VALUE+1);
		assertTrue(reference.getVersionValue().equals(VERSION_VALUE+1));
	}

	@Test
	public void testGetThisField() {
		assertTrue(reference.getThisField().equals(THIS_FIELD_VALUE));
	}

	@Test
	public void testSetThisField() {
		reference.setThisField(THIS_FIELD_VALUE+1);
		assertTrue(reference.getThisField().equals(THIS_FIELD_VALUE+1));
	}

	@Test
	public void testGetEntityField() {
		assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE));
	}

	@Test
	public void testSetEntityField() {
		reference.setEntityField(ENTITY_FIELD_VALUE+1);
		assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE+1));
	}

}
