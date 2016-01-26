package com.redhat.lightblue.test;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractCRUDTestControllerTest {

    @Test
    public void testStripHooks_All() throws Exception {
        JsonNode node = loadJsonNode("./metadata/hooks.json");
        LightblueTestHarness.stripHooks(node,
                new HashSet<String>(Arrays.asList(
                        LightblueTestHarness.REMOVE_ALL_HOOKS)));

        assertNull(node.get("entityInfo").get("hooks"));
    }

    @Test
    public void testStripHooks_Selective() throws Exception {
        JsonNode node = loadJsonNode("./metadata/hooks.json");
        LightblueTestHarness.stripHooks(node,
                new HashSet<String>(Arrays.asList("someHook")));

        JsonNode hooksNode = node.get("entityInfo").get("hooks");
        assertNotNull(hooksNode);
        assertEquals(2, hooksNode.size());
    }

    @Test
    public void testEnsureDatasource() throws Exception {
        JsonNode node = loadJsonNode("./metadata/datasource.json");
        LightblueTestHarness.ensureDatasource(node, "anotherdatasource");

        assertEquals("anotherdatasource", node.get("entityInfo").get(
                "datastore").get("datasource").asText());
    }

    @Test
    public void testGrantAnyoneAccess() throws Exception {
        JsonNode node = loadJsonNode("./metadata/access.json");
        LightblueTestHarness.grantAnyoneAccess(node);

        assertEquals("anyone",
                node.get("schema").get("access").get("insert").get(0).textValue());
        assertEquals("anyone",
                node.get("schema").get("fields").get("anArray").get("access").get("insert").get(0).textValue());
        assertEquals("anyone",
                node.get("schema").get("fields").get("anArray").get("items").get("fields").get("name").get("access").get("insert").get(0).textValue());
        assertEquals("anyone",
                node.get("schema").get("fields").get("someField").get("access").get("insert").get(0).textValue());
    }

}
