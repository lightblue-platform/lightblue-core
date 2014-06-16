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

import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lcestari
 */
public class FieldAndRValueTest {

    public FieldAndRValueTest() {
    }

    /**
     * Test of getField method, of class FieldAndRValue.
     */
    @Test
    public void testGetField() {
        FieldAndRValue instance = new FieldAndRValue();
        Path expResult = null;
        Path result = instance.getField();
        assertEquals(expResult, result);

        expResult = new Path("Test");
        instance = new FieldAndRValue(expResult, null);
        result = instance.getField();
        assertEquals(expResult, result);
    }

    /**
     * Test of setField method, of class FieldAndRValue.
     */
    @Test
    public void testSetField() {
        Path expResult = null;
        FieldAndRValue instance = new FieldAndRValue();
        instance.setField(expResult);
        Path result = instance.getField();
        assertEquals(expResult, result);

        expResult = new Path("Test");
        instance.setField(expResult);
        result = instance.getField();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRValue method, of class FieldAndRValue.
     */
    @Test
    public void testGetRValue() {
        FieldAndRValue instance = new FieldAndRValue();
        RValueExpression expResult = null;
        RValueExpression result = instance.getRValue();
        assertEquals(expResult, result);

        expResult = new RValueExpression(RValueExpression.RValueType._value);
        instance = new FieldAndRValue(null, expResult);
        result = instance.getRValue();
        assertEquals(expResult, result);

    }

    /**
     * Test of setRValue method, of class FieldAndRValue.
     */
    @Test
    public void testSetRValue() {
        RValueExpression expResult = null;
        FieldAndRValue instance = new FieldAndRValue();
        instance.setRValue(expResult);
        RValueExpression result = instance.getRValue();
        assertEquals(expResult, result);

        expResult = new RValueExpression(RValueExpression.RValueType._emptyObject);
        instance.setRValue(expResult);
        result = instance.getRValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FieldAndRValue.
     */
    @Test
    public void testToString() {
        FieldAndRValue instance = new FieldAndRValue();
        String expResult = "null:null";
        String result = instance.toString();
        assertEquals(expResult, result);

        Path p = new Path("ToString");
        RValueExpression rve = new RValueExpression(p);
        instance = new FieldAndRValue(p, rve);
        expResult = "ToString:{\"$valueof\":\"ToString\"}";
        result = instance.toString();
        assertEquals(expResult, result);
    }

}
