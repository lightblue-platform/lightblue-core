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
import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author lcestari
 */
public class RValueExpressionTest {

    public RValueExpressionTest() {
    }

    /**
     * Test of getValue method, of class RValueExpression.
     */
    @Test
    public void testContructors() {

        //public RValueExpression(Value value) {
        //public RValueExpression(Path p) {
        //public RValueExpression() {
        //public RValueExpression(RValueType type) {
        RValueExpression instance = new RValueExpression();
        Value expResult = null;
        Value result = instance.getValue();
        assertEquals(expResult, result);

        //Path expResult = null;
        //Path result = instance.getPath();
        assertEquals(expResult, result);

        
        //RValueExpression.RValueType expResult = null;
        //RValueExpression.RValueType result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toJson method, of class RValueExpression.
     */
    @Test
    @Ignore
    public void testToJson() {
        System.out.println("toJson");
        RValueExpression instance = new RValueExpression();
        JsonNode expResult = null;
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fromJson method, of class RValueExpression.
     */
    @Test
    @Ignore
    public void testFromJson() {
        System.out.println("fromJson");
        JsonNode node = null;
        RValueExpression expResult = null;
        RValueExpression result = RValueExpression.fromJson(node);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
