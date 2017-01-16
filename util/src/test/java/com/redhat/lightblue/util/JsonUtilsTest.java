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

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author lcestari
 */
public class JsonUtilsTest {

    static JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    @Ignore
    public void testGetObjectMapper() {
        ObjectMapper expResult = null;
        ObjectMapper result = JsonUtils.getObjectMapper();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testJson() throws Exception {
        String s = "";
        JsonNode expResult = null;
        JsonNode result = JsonUtils.json(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testJson_system_properties() throws Exception {
        String jvmVersion = System.getProperty("java.vm.version");
        String s = "{\"test\":\"${java.vm.version}\"}";
        JsonNode result = JsonUtils.json(s, true);
        Assert.assertNotNull(result);
        Assert.assertEquals(jvmVersion, result.get("test").asText());
    }

    @Test
    public void testJson_system_properties_no_match() throws Exception {
        String s = "{\"test\":\"${something.that.does.not.exist}\"}";
        JsonNode result = JsonUtils.json(s, true);
        Assert.assertNotNull(result);
        Assert.assertEquals("${something.that.does.not.exist}", result.get("test").asText());
    }

    @Test
    public void testPrettyPrint_nullNode() {
        ObjectNode node = nodeFactory.objectNode();
        node.set("value", nodeFactory.objectNode());
        Assert.assertEquals("{\n"
                + "\"value\":{}\n"
                + "}", JsonUtils.prettyPrint(node));
    }

    @Test
    @Ignore
    public void testPrettyPrint_JsonNode() {
        JsonNode node = null;
        String expResult = "";
        String result = JsonUtils.prettyPrint(node);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testPrettyPrint_StringBuilder_JsonNode() {
        StringBuilder bld = null;
        JsonNode node = null;
        JsonUtils.prettyPrint(bld, node);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testJsonWithNoInput() throws Exception {
        JsonUtils.json((String) null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    @Ignore
    public void testJsonWithInvalidJSONInput() throws Exception {
        JsonUtils.json("a");
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testFromJson() throws Exception {
        JsonNode node=JsonUtils.json(getClass().getResourceAsStream("/JsonNodeDocTest-complex.json"));
        Map obj=(Map)JsonUtils.fromJson(node);
        Assert.assertTrue(obj.get("object1") instanceof Map);
        Assert.assertTrue(obj.get("object2") instanceof Map);
        Assert.assertEquals("value3",((Map)obj.get("object2")).get("simple3"));
        Assert.assertEquals(2,((List) ((Map)obj.get("object1")).get("array1")).size());
        Assert.assertEquals("value1",((Map)((List)((Map)obj.get("object1")).get("array1")).get(0)).get("simple1"));
    }
    
    @Test
    public void testToson() throws Exception {
        HashMap m=new HashMap();
        m.put("v1","value1");
        HashMap n;
        m.put("o",n=new HashMap());
        List a=new ArrayList();
        n.put("a",a);
        a.add(new Integer(1));
        JsonNode node=JsonUtils.toJson(m);
        Assert.assertEquals("value1",node.get("v1").asText());
        Assert.assertEquals(1,node.get("o").get("a").get(0).asInt());
    }
}
