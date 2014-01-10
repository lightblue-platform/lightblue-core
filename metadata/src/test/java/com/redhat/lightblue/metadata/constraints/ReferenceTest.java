package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.*;

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
	public void getEntityNameTest() {
		assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE));
	}

	@Test
	public void setEntityNameTest() {
		reference.setEntityName(ENTITY_NAME_VALUE+1);
		assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE+1));
	}

	@Test
	public void getVersionValueTest() {
		assertTrue(reference.getVersionValue().equals(VERSION_VALUE));
	}

	@Test
	public void setVersionValueTest() {
		reference.setVersionValue(VERSION_VALUE+1);
		assertTrue(reference.getVersionValue().equals(VERSION_VALUE+1));
	}

	@Test
	public void getThisFieldTest() {
		assertTrue(reference.getThisField().equals(THIS_FIELD_VALUE));
	}

	@Test
	public void setThisFieldTest() {
		reference.setThisField(THIS_FIELD_VALUE+1);
		assertTrue(reference.getThisField().equals(THIS_FIELD_VALUE+1));
	}

	@Test
	public void getEntityFieldTest() {
		assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE));
	}

	@Test
	public void setEntityFieldTest() {
		reference.setEntityField(ENTITY_FIELD_VALUE+1);
		assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE+1));
	}

}
