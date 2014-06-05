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

package com.redhat.lightblue.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lcestari
 */
public class JsonObjectTest {
    
    public JsonObjectTest() {
    }

    /**
     * Test of getFactory method, of class JsonObject.
     */
    @Test
    public void testGetFactory() {
        JsonNodeFactory expResult = JsonNodeFactory.withExactBigDecimals(true);
        JsonNodeFactory result = JsonObject.getFactory();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSourceNode method, of class JsonObject.
     */
    @Test
    public void testGetSourceNode() {
        JsonNode expResult = NullNode.getInstance();
        JsonObject instance = new JsonObjectImpl(expResult);
        JsonNode result = instance.getSourceNode();
        assertEquals(expResult, result);
    }

    /**
     * Test of toJson method, of class JsonObject.
     */
    @Test
    public void testToJson() {
        JsonObject instance = new JsonObjectImpl();
        JsonNode expResult = NullNode.getInstance();
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class JsonObject.
     */
    @Test
    public void testToString() {
        JsonObject instance = new JsonObjectImpl();
        String expResult = "null";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    public class JsonObjectImpl extends JsonObject {

        public JsonObjectImpl(JsonNode node) {
            super(node);
        }

        public JsonObjectImpl() {
            super();
        }
        
        public JsonNode toJson() {
            return getFactory().nullNode();
        }
    }
    
}
