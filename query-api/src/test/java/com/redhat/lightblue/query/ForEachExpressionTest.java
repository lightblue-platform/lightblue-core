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
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author lcestari
 */
@Ignore
public class ForEachExpressionTest {
    
    /**
     * Test of getField method, of class ForEachExpression.
     */
    @Test
    public void testGetField() {
        System.out.println("getField");
        ForEachExpression instance = null;
        Path expResult = null;
        Path result = instance.getField();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getQuery method, of class ForEachExpression.
     */
    @Test
    public void testGetQuery() {
        System.out.println("getQuery");
        ForEachExpression instance = null;
        QueryExpression expResult = null;
        QueryExpression result = instance.getQuery();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUpdate method, of class ForEachExpression.
     */
    @Test
    public void testGetUpdate() {
        System.out.println("getUpdate");
        ForEachExpression instance = null;
        UpdateExpression expResult = null;
        UpdateExpression result = instance.getUpdate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toJson method, of class ForEachExpression.
     */
    @Test
    public void testToJson() {
        System.out.println("toJson");
        ForEachExpression instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fromJson method, of class ForEachExpression.
     */
    @Test
    public void testFromJson() {
        System.out.println("fromJson");
        ObjectNode node = null;
        ForEachExpression expResult = null;
        ForEachExpression result = ForEachExpression.fromJson(node);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
