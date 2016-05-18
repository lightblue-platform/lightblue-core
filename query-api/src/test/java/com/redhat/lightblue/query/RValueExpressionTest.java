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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author lcestari
 */
public class RValueExpressionTest {

    /**
     * Test of getValue method, of class RValueExpression.
     */
    @Test
    public void testContructors() {
        //public RValueExpression() {
        RValueExpression instance = new RValueExpression(new Value(JsonNodeFactory.instance.objectNode()));
        Value expResult = new Value(JsonNodeFactory.instance.objectNode());
        Value result = instance.getValue();
        assertEquals(expResult, result);

        Path pExpResult = null;
        Path pResult = instance.getPath();
        assertEquals(pExpResult, pResult);

        RValueExpression.RValueType rExpResult = RValueExpression.RValueType._value;
        RValueExpression.RValueType rResult = instance.getType();
        assertEquals(rExpResult, rResult);

        //public RValueExpression(Value value) {
        instance = new RValueExpression(new Value(""));
        expResult = new Value("");
        result = instance.getValue();
        assertEquals(expResult, result);

        pExpResult = null;
        pResult = instance.getPath();
        assertEquals(pExpResult, pResult);

        rExpResult = RValueExpression.RValueType._value;
        rResult = instance.getType();
        assertEquals(rExpResult, rResult);

        //public RValueExpression(Path p) {
        instance = new RValueExpression(Path.ANYPATH);
        expResult = null;
        result = instance.getValue();
        assertEquals(expResult, result);

        pExpResult = Path.ANYPATH;
        pResult = instance.getPath();
        assertEquals(pExpResult, pResult);

        rExpResult = RValueExpression.RValueType._dereference;
        rResult = instance.getType();
        assertEquals(rExpResult, rResult);

        //public RValueExpression(RValueType type) {
        instance = RValueExpression.NULL;
        expResult = null;
        result = instance.getValue();
        assertEquals(expResult, result);

        pExpResult = null;
        pResult = instance.getPath();
        assertEquals(pExpResult, pResult);

        rExpResult = RValueExpression.RValueType._null;
        rResult = instance.getType();
        assertEquals(rExpResult, rResult);
    }

    /**
     * Test of toJson method, of class RValueExpression.
     */
    @Test
    public void testToJson() throws IOException {
        RValueExpression instance = new RValueExpression(new Value("test"));
        JsonNode expResult = JsonUtils.json("\"test\"");
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromJson method, of class RValueExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        RValueExpression expResult = new RValueExpression(Path.ANYPATH);
        RValueExpression result = RValueExpression.fromJson(JsonUtils.json("{\"$valueof\":\"*\"}"));
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        assertEquals(new RValueExpression(new Value(JsonNodeFactory.instance.objectNode())).hashCode(),
                new RValueExpression(new Value(JsonNodeFactory.instance.objectNode())).hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(new RValueExpression(new Value(JsonNodeFactory.instance.objectNode())),
                new RValueExpression(new Value(JsonNodeFactory.instance.objectNode())));
        RValueExpression instance = new RValueExpression(new Value(JsonNodeFactory.instance.objectNode()));
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(""));
        assertFalse(instance.equals(new RValueExpression(Path.ANYPATH)));
        assertFalse(instance.equals(new RValueExpression(new Value(this))));
    }

}
