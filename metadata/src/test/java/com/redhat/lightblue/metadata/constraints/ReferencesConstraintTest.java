package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferencesConstraintTest {

	ReferencesConstraint constraint;
	ArrayList<Reference> references;
	
	@Before
	public void setUp() throws Exception {
		constraint = new ReferencesConstraint();
		Reference reference = new Reference();
		reference.setEntityField(ReferenceTest.ENTITY_FIELD_VALUE);
		reference.setEntityName(ReferenceTest.ENTITY_NAME_VALUE);
		reference.setThisField(ReferenceTest.THIS_FIELD_VALUE);
		reference.setVersionValue(ReferenceTest.VERSION_VALUE);
		references = new ArrayList<Reference>();
		references.add(reference);
		constraint.setReferences(references);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void getTypeTest() {
		assertTrue(constraint.getType().equals(ReferencesConstraint.REFERENCES));
	}

	@Test
	public void testGetReferencesTest() {
		assertTrue(constraint.getReferences().equals(references));
	}

	@Test
	public void testSetReferences() {
		Reference reference = new Reference();
		reference.setEntityField(ReferenceTest.ENTITY_FIELD_VALUE+1);
		reference.setEntityName(ReferenceTest.ENTITY_NAME_VALUE+1);
		reference.setThisField(ReferenceTest.THIS_FIELD_VALUE+1);
		reference.setVersionValue(ReferenceTest.VERSION_VALUE+1);
		references = new ArrayList<Reference>();
		references.add(reference);
		constraint.setReferences(references);
		assertTrue(constraint.getReferences().equals(references));
	}

	@Test
	public void testSetReferencesNull() {
		constraint.setReferences(null);
		references.clear();
		assertTrue(constraint.getReferences().equals(references));
	}
}
