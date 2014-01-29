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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;

public class ModifyDocTest {

    private static JsonNodeFactory factory;
    private static JsonDoc doc;

    @Before
    public void before() {
        factory = JsonNodeFactory.withExactBigDecimals(true);
        doc = new JsonDoc(factory.objectNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void modify_with_empty_path_throws_exception() {
        doc.modify(new Path(""), null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void modify_non_container_parent_throws_exception() {
        doc = new JsonDoc(factory.booleanNode(true));

        doc.modify(new Path("path"), factory.objectNode(), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void modify_with_null_value_throws_exception() {

        doc.modify(new Path("x.y.z.1.w"), null, false);
    }

    @Test
    public void create_basic_number_node_at_root() {
        doc.modify(new Path("field1"), factory.numberNode(1), true);

        Assert.assertEquals(1, doc.get(new Path("field1")).intValue());
    }

    @Test
    public void create_text_node_at_root() {
        doc.modify(new Path("field2"), factory.textNode("blah"), true);

        Assert.assertEquals("blah", doc.get(new Path("field2")).textValue());
    }

    @Test
    public void create_array_node_at_root() {
        doc.modify(new Path("arr"), factory.arrayNode(), true);
        Assert.assertEquals(0, doc.get(new Path("arr")).size());
    }

    @Test
    public void create_array_element_at_root() {
        doc.modify(new Path("arr.0"), factory.numberNode(1), true);

        Assert.assertEquals(1, doc.get(new Path("arr.0")).intValue());
    }

    @Test
    public void create_array_with_non_zero_index_creates_correctly_sized_array() {
        doc.modify(new Path("arr.5"), factory.numberNode(5), true);

        Assert.assertEquals(6, doc.get(new Path("arr")).size());
    }

    @Test
    public void create_array_with_one_index_value_initializes_remaining_indicies_to_null() {
        doc.modify(new Path("arr.2"), factory.numberNode(5), true);

        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.0"))
                .getClass());
        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.1"))
                .getClass());
    }

    @Test
    public void create_nested_number_node() {
        doc.modify(new Path("x.y.z.1.w"), factory.numberNode(1), true);

        Assert.assertEquals(1, doc.get(new Path("x.y.z.1.w")).intValue());
    }

    @Test
    public void create_nested_text_node() {
        doc.modify(new Path("x.y.z.1.w"), factory.textNode("text"), true);

        Assert.assertEquals("text", doc.get(new Path("x.y.z.1.w")).textValue());
    }

    @Test
    public void create_nested_array_node() {
        doc.modify(new Path("arr"), factory.arrayNode(), true);

        Assert.assertEquals(0, doc.get(new Path("arr")).size());
    }

    @Test
    public void create_nested_array_node_element() {
        doc.modify(new Path("x.y.z.1"), factory.textNode("text"), true);

        Assert.assertEquals("text", doc.get(new Path("x.y.z.1")).textValue());
    }

    @Test
    public void create_nested_array_with_index_creates_array_of_correct_size() {
        doc.modify(new Path("test.arr.5"), factory.numberNode(5), true);

        Assert.assertEquals(6, doc.get(new Path("test.arr")).size());
    }

    @Test
    public void create_nested_array_with_one_index_value_initializes_remaining_indicies_to_null() {
        doc.modify(new Path("test.arr.2"), factory.numberNode(5), true);

        Assert.assertEquals(NullNode.class, doc.get(new Path("test.arr.0")).getClass());
        Assert.assertEquals(NullNode.class, doc.get(new Path("test.arr.1")).getClass());
    }

    @Test
    public void modify_basic_number_node_at_root() {
        doc.modify(new Path("x"), factory.numberNode(1), true);

        doc.modify(new Path("x"), factory.numberNode(2), false);

        Assert.assertEquals(2, doc.get(new Path("x")).intValue());
    }

    @Test
    public void modify_text_node_at_root() {
        doc.modify(new Path("x"), factory.textNode("test"), true);

        doc.modify(new Path("x"), factory.textNode("result"), false);

        Assert.assertEquals("result", doc.get(new Path("x")).textValue());
    }

    @Test
    public void modify_nested_number_node() {
        doc.modify(new Path("x.y"), factory.numberNode(1), true);

        doc.modify(new Path("x.y"), factory.numberNode(2), false);

        Assert.assertEquals(2, doc.get(new Path("x.y")).intValue());
    }

    @Test
    public void modify_nested_text_node() {
        doc.modify(new Path("x.y"), factory.textNode("test"), true);

        doc.modify(new Path("x.y"), factory.textNode("result"), false);

        Assert.assertEquals("result", doc.get(new Path("x.y")).textValue());
    }

    @Test
    public void modify_nested_array_node() {
        doc.modify(new Path("x.arr"), factory.textNode("test"), true);

        doc.modify(new Path("x.arr"), factory.textNode("result"), false);

        Assert.assertEquals("result", doc.get(new Path("x.arr")).textValue());
    }

    @Test
    public void modify_nested_array_node_element() {
        doc.modify(new Path("arr.1"), factory.textNode("test"), true);

        doc.modify(new Path("arr.1"), factory.textNode("result"), false);

        Assert.assertEquals("result", doc.get(new Path("arr.1")).textValue());
    }
    
    @Test
    public void remove_basic_number_node_at_root() {
        doc.modify(new Path("x"), factory.numberNode(1), true);

        doc.modify(new Path("x"), null, false);

        Assert.assertNull(doc.get(new Path("x")));
    }

    @Test
    public void remove_text_node_at_root() {
        doc.modify(new Path("x"), factory.textNode("test"), true);

        doc.modify(new Path("x"), null, false);

        Assert.assertNull(doc.get(new Path("x")));
    }

    @Test
    public void remove_array_node_at_root() {
        doc.modify(new Path("arr.0"), factory.textNode("test"), true);

        doc.modify(new Path("arr.0"), null, false);

        Assert.assertNull(doc.get(new Path("arr.1")));
    }

    @Test
    public void remove_nested_number_node() {
        doc.modify(new Path("x.y"), factory.numberNode(1), true);

        doc.modify(new Path("x.y"), null, false);

        Assert.assertNull(doc.get(new Path("x.y")));
    }

    @Test
    public void remove_nested_text_node() {
        doc.modify(new Path("x.y"), factory.textNode("test"), true);

        doc.modify(new Path("x.y"), null, false);

        Assert.assertNull(doc.get(new Path("x.y")));
    }

    @Test
    public void remove_nested_array_node() {
        doc.modify(new Path("x.arr"), factory.arrayNode(), true);

        doc.modify(new Path("x.arr"), null, false);

        Assert.assertNull(doc.get(new Path("x.arr")));
    }

    @Test
    public void remove_nested_array_node_element() {
        doc.modify(new Path("x.arr.0"), factory.textNode("test"), true);

        doc.modify(new Path("x.arr.0"), null, false);

        Assert.assertEquals(0, doc.get(new Path("x.arr")).size());
    }

    @Test
    public void remove_nested_array_node_non_zero_index() {
        doc.modify(new Path("x.arr.0"), factory.textNode("test0"), true);
        doc.modify(new Path("x.arr.1"), factory.textNode("test1"), true);

        doc.modify(new Path("x.arr.0"), null, false);

        Assert.assertEquals(1, doc.get(new Path("x.arr")).size());
    }

}
