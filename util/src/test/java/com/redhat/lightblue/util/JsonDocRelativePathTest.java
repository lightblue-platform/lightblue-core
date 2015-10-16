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
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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
    public void relative_path_with_1_$parent_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$parent.simplenested"));

        Assert.assertEquals("nestedvalue", ((TextNode) result).textValue());
    }

    @Test
    public void relative_path_with_2_$parent_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.doublenestedsimple.$parent.$parent.simplenested"));

        Assert.assertEquals("nestedvalue", ((TextNode) result).textValue());
    }

    @Test
    public void relative_path_with_3_$parent_and_valid_field_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.triplenested1.$parent.$parent.$parent.simple"));

        Assert.assertEquals("value", ((TextNode) result).textValue());
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

    @Test
    public void relative_path_with_2_non_successive_$parent_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$parent.doublenested2.triplenested2.$parent.doublenestedsimple2"));

        Assert.assertEquals("doublenestedvalue2", ((TextNode) result).textValue());
    }

    @Test
    public void relative_path_with_2_non_successive_$this_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.$this.doublenested1.$this.doublenestedsimple"));

        Assert.assertEquals("doublenestedvalue", ((TextNode) result).asText());
    }

    @Test
    public void relative_path_with_parent_and_this_resolves_correctly() {
        JsonNode result = doc.get(new Path("object.nested1.doublenested1.$parent.doublenested2.triplenested2.$this.triplenestedsimple2"));

        Assert.assertEquals("triplenestedvalue2", ((TextNode) result).textValue());
    }

}
