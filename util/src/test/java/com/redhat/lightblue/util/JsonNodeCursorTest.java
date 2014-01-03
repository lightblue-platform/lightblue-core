/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import java.io.IOException;
import org.junit.Before;

/**
 *
 * @author nmalik
 */
public class JsonNodeCursorTest extends AbstractTreeCursorTest<JsonNode> {
    @Before
    @Override
    public void setup() {
        path = new Path("object.text");
        super.setup();
    }

    @Override
    public AbstractTreeCursor<JsonNode> createCursor(Path p) {
        try {
            JsonNode node = AbstractJsonNodeTest.loadJsonNode("JsonNodeCursorTest-general.json");
            return new JsonNodeCursor(path, node);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
