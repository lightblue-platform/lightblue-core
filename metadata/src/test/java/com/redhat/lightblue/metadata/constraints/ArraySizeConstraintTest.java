package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.ArrayType;

public class ArraySizeConstraintTest {

	ArraySizeConstraint constraint;
	
	@Before
	public void setUp() throws Exception {
		constraint = new ArraySizeConstraint("StringType");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsValidForFieldType() {
		assertTrue(constraint.isValidForFieldType(ArrayType.TYPE));
	}

}
