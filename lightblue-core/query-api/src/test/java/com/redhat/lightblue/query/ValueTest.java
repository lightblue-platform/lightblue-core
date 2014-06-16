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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

/**
 *
 * @author lcestari
 */
public class ValueTest {

    /**
     * Test of toJson method, of class Value.
     */
    @Test
    public void testToJson() throws IOException {
        Value instance = new Value(10L);
        JsonNode expResult = new LongNode(10);
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);

        instance = new Value(10.0D);
        expResult = new DoubleNode(10.0D);
        result = instance.toJson();
        assertEquals(expResult, result);

        instance = new Value(3.0F);
        expResult = new FloatNode(3.0F);
        result = instance.toJson();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromJson method, of class Value.
     */
    @Test
    public void testFromJson() throws IOException {
        Value expResult = new Value(10L);
        JsonNode longNode = new LongNode(10);
        Value result = Value.fromJson(longNode);
        assertEquals(expResult, result);

        boolean error = false;
        try {
            Value.fromJson(JsonUtils.json("[\"test\"]"));
        } catch (Error e) {
            error = true;
            String exString = Error.get(QueryConstants.ERR_INVALID_VALUE, "[\"test\"]").toString();
            assertEquals(exString, e.toString());
        }
        if (!error) {
            fail("Expection was not thrown");
        }
    }

    /**
     * Test of hashCode method, of class Value.
     */
    @Test
    public void testHashCode() {
        Value instance = new Value("");
        int expResult = new Value("").hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Value.
     */
    @Test
    public void testEquals() {
        assertEquals(new Value(""), new Value(""));
        Value instance = new Value("");
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(""));
        assertFalse(instance.equals(new Value("diff")));
    }

}
