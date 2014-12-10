/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This file is part of lightblue.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.query;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author lcestari
 */
public class UpdateOperatorTest {

    /**
     * Test of values method, of class UpdateOperator.
     */
    @Test
    public void testValues() {
        UpdateOperator[] expResult = {
            UpdateOperator._set, UpdateOperator._unset, UpdateOperator._add,
            UpdateOperator._append, UpdateOperator._insert,
            UpdateOperator._foreach};
        UpdateOperator[] result = UpdateOperator.values();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class UpdateOperator.
     */
    @Test
    public void testValueOf() {
        String name = "_unset";
        UpdateOperator expResult = UpdateOperator._unset;
        UpdateOperator result = UpdateOperator.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class UpdateOperator.
     */
    @Test
    public void testToString() {
        UpdateOperator instance = UpdateOperator._add;
        String expResult = "$add";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromString method, of class UpdateOperator.
     */
    @Test
    public void testFromString() {
        String s = "$set";
        UpdateOperator expResult = UpdateOperator._set;
        UpdateOperator result = UpdateOperator.fromString(s);
        assertEquals(expResult, result);
    }

}
