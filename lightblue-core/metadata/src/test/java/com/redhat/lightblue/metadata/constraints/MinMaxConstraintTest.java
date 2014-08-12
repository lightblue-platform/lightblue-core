/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
