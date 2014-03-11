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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Path;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lcestari
 */
public class ArrayAddExpressionTest {
    
    public ArrayAddExpressionTest() {
    }

    /**
     * Test of getField method, of class ArrayAddExpression.
     */
    @Test
    public void testGetField() {
        System.out.println("getField");
        ArrayAddExpression instance = null;
        Path expResult = null;
        Path result = instance.getField();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class ArrayAddExpression.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        ArrayAddExpression instance = null;
        List<RValueExpression> expResult = null;
        List<RValueExpression> result = instance.getValues();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOp method, of class ArrayAddExpression.
     */
    @Test
    public void testGetOp() {
        System.out.println("getOp");
        ArrayAddExpression instance = null;
        UpdateOperator expResult = null;
        UpdateOperator result = instance.getOp();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toJson method, of class ArrayAddExpression.
     */
    @Test
    public void testToJson() {
        System.out.println("toJson");
        ArrayAddExpression instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fromJson method, of class ArrayAddExpression.
     */
    @Test
    public void testFromJson() {
        System.out.println("fromJson");
        ObjectNode node = null;
        ArrayAddExpression expResult = null;
        ArrayAddExpression result = ArrayAddExpression.fromJson(node);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class ArrayAddExpression.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        ArrayAddExpression instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class ArrayAddExpression.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        ArrayAddExpression instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
