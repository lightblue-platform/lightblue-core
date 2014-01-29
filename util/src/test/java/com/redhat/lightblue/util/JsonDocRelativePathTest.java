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

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class JsonDocRelativePathTest extends AbstractJsonNodeTest {

    JsonNode node;
    JsonDoc doc;
    
    protected JsonNode createJsonNode(String postfix) {
        try {
            return loadJsonNode("JsonNodeDocRelativeTest-" + postfix + ".json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Before
    public void setup() {
        node = createJsonNode("complexarray");
        doc = new JsonDoc(node);
    }
    
    
    @Test
    public void relative_path_with_1_$this_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$this.doublenestedsimple"));
        
        Assert.assertEquals("doublenestedvalue", ((TextNode) result).asText());
    }

    @Test
    public void relative_path_with_2_$this_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$this.$this.doublenestedsimple"));
        
        Assert.assertEquals("doublenestedvalue", ((TextNode) result).asText());
    }
    
    @Test
    public void relative_path_with_3_$this_and_valid_field_resolves_correctly() {        
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$this.$this.$this.doublenestedsimple"));
        
        Assert.assertEquals("doublenestedvalue", ((TextNode) result).asText());
    }

    @Test
    public void relative_path_with_$this_and_invalid_field_returns_null() {        
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$this.notthere"));
        
        Assert.assertNull(result);
    }
    
    @Test
    public void relative_path_with_1_$parent_and_valid_field_resolves_correctly()  {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$parent.simplenested"));
        
        Assert.assertEquals("nestedvalue", ((TextNode) result).textValue());
    }
    
    @Test
    public void relative_path_with_2_$parent_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.doublenested.$parent.$parent.simplenested"));
        
        Assert.assertEquals("nestedvalue", ((TextNode) result).textValue());
    }
    
    @Test
    public void relative_path_with_3_$parent_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.doublenested.triplenested1.$parent.$parent.$parent.simplenested"));
        
        Assert.assertEquals("nestedvalue", ((TextNode) result).textValue());
    }

    @Test
    public void relative_path_with_gibberish_in_the_beginning_and_valid_field_returns_null() {
        JsonNode result = doc.get(new Path("does.not.exist.$parent.$parent.simplenested.doublenested1.doublenestedsimple"));
        
        Assert.assertNull(result);
    }
    
    @Test
    public void relative_path_with_$parent_and_invalid_field_returns_null() {        
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$parent.notthere"));
        
        Assert.assertNull(result);
    }
    
    public void getComplex() {
        JsonNode node = createJsonNode("complex");
        JsonDoc doc = new JsonDoc(node);

        JsonNode result = doc.get(new Path("object1.array1.1.simple2"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value2", ((TextNode) result).textValue());

        result = doc.get(new Path("object2.simple3"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value3", ((TextNode) result).textValue());

        result = doc.get(new Path("object2.array2.0"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ObjectNode);

    }

    
    public void itr() {
        JsonNode node = createJsonNode("complexarray");
        JsonDoc doc = new JsonDoc(node);

        JsonNode x = doc.get(new Path("array1.1.nested1.0"));
        Assert.assertEquals(1, x.asInt());

        KeyValueCursor<Path, JsonNode> c = doc.getAllNodes(new Path("array1.*.deep.0.deeper.*"));
        System.out.println(c);
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(1, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(1, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(2, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(2, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(3, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(3, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(4, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(4, c.getCurrentValue().asInt());
        Assert.assertTrue(!c.hasNext());

        c = doc.getAllNodes(new Path("array1.0.nested1.0"));
        Assert.assertTrue(c.hasNext());
        c.next();
        System.out.println(c.getCurrentKey());
        Assert.assertEquals(1, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(1, c.getCurrentValue().asInt());
        Assert.assertTrue(!c.hasNext());
    }
}
