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
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author lcestari
 */
public class ArrayAddExpressionTest {

    /**
     * Test of getField method, of class ArrayAddExpression.
     */
    @Test
    public void testGetField() {
        ArrayAddExpression instance = new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null);
        Path expResult = Path.EMPTY;
        Path result = instance.getField();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValues method, of class ArrayAddExpression.
     */
    @Test
    public void testGetValues() {
        List<RValueExpression> expResult = new ArrayList<>();
        ArrayAddExpression instance = new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, expResult);
        expResult.add(new RValueExpression(Path.EMPTY));
        List<RValueExpression> result = instance.getValues();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOp method, of class ArrayAddExpression.
     */
    @Test
    public void testGetOp() {
        ArrayAddExpression instance = new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null);
        UpdateOperator expResult = UpdateOperator._set;
        UpdateOperator result = instance.getOp();
        assertEquals(expResult, result);
    }

    /**
     * Test of toJson method, of class ArrayAddExpression.
     */
    @Test
    public void testToJson() throws IOException {
        List<RValueExpression> l = new ArrayList<>();
        ArrayAddExpression instance = new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, l);
        l.add(new RValueExpression(Path.EMPTY));
        JsonNode expResult = JsonUtils.json("{\"$set\":{\"\":{\"$valueof\":\"\"}}}");
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromJson method, of class ArrayAddExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        ObjectNode node = (ObjectNode) JsonUtils.json("{\"$append\":{\"\":{\"$valueof\":\"\"}}}");
        List<RValueExpression> l = new ArrayList<>();
        ArrayAddExpression expResult = new ArrayAddExpression(Path.EMPTY, UpdateOperator._append, l);
        l.add(new RValueExpression(Path.EMPTY));
        ArrayAddExpression result = ArrayAddExpression.fromJson(node);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class ArrayAddExpression.
     */
    @Test
    public void testHashCode() {
        assertEquals(new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null).hashCode(), new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null).hashCode());
    }

    /**
     * Test of equals method, of class ArrayAddExpression.
     */
    @Test
    public void testEquals() throws IOException {
        assertEquals(new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null), new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null));
        ArrayAddExpression instance = new ArrayAddExpression(Path.EMPTY, UpdateOperator._set, null);
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(""));
        assertFalse(instance.equals(ArrayAddExpression.fromJson((ObjectNode) JsonUtils.json("{\"$append\":{\"\":{\"$valueof\":\"\"}}}"))));
        assertFalse(instance.equals(new ArrayAddExpression(Path.EMPTY, UpdateOperator._add, null)));
        assertFalse(instance.equals(new ArrayAddExpression(Path.ANYPATH, UpdateOperator._set, null)));
    }

}
