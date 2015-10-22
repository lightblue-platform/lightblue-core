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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonNodeCursorProfileTest extends AbstractJsonNodeTest {

    protected JsonNode createJsonNode(String postfix) {
        try {
            return loadJsonNode("JsonNodeDocTest-" + postfix + ".json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void profile() throws Exception {
        JsonNode node=createJsonNode("complexarray");
        ArrayNode anode=JsonNodeFactory.instance.arrayNode();
        for(int i=0;i<1000;i++)
            anode.add(node);
        JsonNodeCursor cursor=new JsonNodeCursor(new Path(),anode);
        long l=System.currentTimeMillis();
        while(cursor.next());

        System.out.println(System.currentTimeMillis()-l);
    }
}
