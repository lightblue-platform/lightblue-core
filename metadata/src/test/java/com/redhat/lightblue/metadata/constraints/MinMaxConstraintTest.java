package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.BigDecimalType;
import com.redhat.lightblue.metadata.types.BigIntegerType;
import com.redhat.lightblue.metadata.types.DoubleType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;

public class MinMaxConstraintTest {

    MinMaxConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new MinMaxConstraint(IntegerType.TYPE.toString());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetType() {
        assertTrue(constraint.getType().equals(IntegerType.TYPE.toString()));
    }

    @Test
    public void testIsValidForFieldTypeInteger() {
        assertTrue(constraint.isValidForFieldType(IntegerType.TYPE));
    }

    @Test
    public void testIsValidForFieldTypeDouble() {
        assertTrue(constraint.isValidForFieldType(DoubleType.TYPE));
    }

    @Test
    public void testIsValidForFieldTypeBigDecimal() {
        assertTrue(constraint.isValidForFieldType(BigDecimalType.TYPE));
    }

    @Test
    public void testIisValidForFieldTypeBigInteger() {
        assertTrue(constraint.isValidForFieldType(BigIntegerType.TYPE));
    }

    @Test
    public void testIsNotValidForFieldType() {
        assertFalse(constraint.isValidForFieldType(StringType.TYPE));

    }

    @Test
    public void testGetValue() {
        constraint.setValue(1);
        assertEquals(constraint.getValue(), 1);
    }

    @Test
    public void testSetValue() {
        constraint.setValue(0);
        assertEquals(constraint.getValue(), 0);
    }

}
