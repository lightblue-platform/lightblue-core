package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferenceTypeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void typeTest() {
		ReferenceType referenceType = ReferenceType.TYPE;
		assertNotNull(referenceType);
	}

}
