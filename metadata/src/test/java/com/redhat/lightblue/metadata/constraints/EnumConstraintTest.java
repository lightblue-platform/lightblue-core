package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.StringType;

public class EnumConstraintTest {

    EnumConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new EnumConstraint();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetType() {
        assertTrue(constraint.getType().equals(EnumConstraint.ENUM));
    }

    @Test
    public void testIsValidForFieldType() {
        assertTrue(constraint.isValidForFieldType(StringType.TYPE));
    }

    @Test
    public void testGetName() {
        constraint.setName("not null");
        assertNotNull(constraint.getName());
    }

    @Test
    public void testSetName() {
        String name = "3";
        constraint.setName(name);
        assertTrue(name.equals(constraint.getName()));
    }

    @Test
    public void testSetValuesNull() {
        constraint.setName(null);
        assertTrue(null == constraint.getName());
    }
}
