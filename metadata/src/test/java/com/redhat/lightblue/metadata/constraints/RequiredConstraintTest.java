package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.metadata.types.Type;

public class RequiredConstraintTest {

    RequiredConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new RequiredConstraint();
        constraint.setValue(true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetType() {
        assertTrue(constraint.getType().equals(RequiredConstraint.REQUIRED));
    }

    @Test
    public void testIsValidForFieldType() {
        assertTrue(constraint.isValidForFieldType(StringType.TYPE));
    }

    @Test
    public void testGetValue() {
        assertTrue(constraint.getValue());
    }

    @Test
    public void testSetValue() {
        constraint.setValue(false);
        assertFalse(constraint.getValue());
    }

}
