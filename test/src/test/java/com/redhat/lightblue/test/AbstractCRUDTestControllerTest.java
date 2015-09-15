package com.redhat.lightblue.test;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractCRUDTestControllerTest {

    @Test
    public void testEnsureDatasource() throws Exception {
        JsonNode node = loadJsonNode("./metadata/datasource.json");
        AbstractCRUDTestController.ensureDatasource(node, "anotherdatasource");

        assertEquals("anotherdatasource", node.get("entityInfo").get(
                "datastore").get("datasource").asText());
    }

    @Test
    public void testGrantAnyoneAccess() throws Exception {
        JsonNode node = loadJsonNode("./metadata/access.json");
        AbstractCRUDTestController.grantAnyoneAccess(node);

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
