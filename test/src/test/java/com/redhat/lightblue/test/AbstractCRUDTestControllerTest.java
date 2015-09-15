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

}
