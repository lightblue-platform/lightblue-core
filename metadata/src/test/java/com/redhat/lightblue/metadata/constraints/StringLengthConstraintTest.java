package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.StringType;

public class StringLengthConstraintTest {

    StringLengthConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new StringLengthConstraint(StringLengthConstraint.MAXLENGTH);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsValidForFieldType() {
        assertTrue(constraint.isValidForFieldType(StringType.TYPE));
    }

}
