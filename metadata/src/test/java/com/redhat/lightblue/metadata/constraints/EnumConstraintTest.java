package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

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
        assertTrue(constraint.getType().equals(EnumConstraint.TYPE));
    }

    @Test
    public void testIsValidForFieldType() {
        assertTrue(constraint.isValidForFieldType(StringType.TYPE));
    }

    @Test
    public void testGetValues() {
        assertNotNull(constraint.getValues());
    }

    @Test
    public void testSetValues() {
        Collection<String> values = new HashSet<String>();
        values.add("1");
        values.add("2");
        values.add("3");
        constraint.setValues(values);
        assertTrue(constraint.getValues().equals(values));
    }

    @Test
    public void testSetValuesNull() {
        Collection<String> values = new HashSet<String>();
        constraint.setValues(null);
        assertTrue(constraint.getValues().equals(values));
    }

}
