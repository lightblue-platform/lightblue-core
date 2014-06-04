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

package com.redhat.lightblue.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author lcestari
 */
@Ignore
public class JsonUtilsTest {
    @Test
    public void testGetObjectMapper() {
        System.out.println("getObjectMapper");
        ObjectMapper expResult = null;
        ObjectMapper result = JsonUtils.getObjectMapper();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testJson() throws Exception {
        System.out.println("json");
        String s = "";
        JsonNode expResult = null;
        JsonNode result = JsonUtils.json(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testPrettyPrint_JsonNode() {
        System.out.println("prettyPrint");
        JsonNode node = null;
        String expResult = "";
        String result = JsonUtils.prettyPrint(node);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testPrettyPrint_StringBuilder_JsonNode() {
        System.out.println("prettyPrint");
        StringBuilder bld = null;
        JsonNode node = null;
        JsonUtils.prettyPrint(bld, node);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    public void testJsonWithNoInput() throws Exception {
        JsonUtils.json((String)null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    public void testJsonWithInvalidJSONInput() throws Exception {
        JsonUtils.json("a");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
