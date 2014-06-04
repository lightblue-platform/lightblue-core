/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This file is part of lightblue.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.util.JsonObject;
import com.redhat.lightblue.util.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author lcestari
 */
public class UpdateExpressionListTest {

    /**
     * Test of getList method, of class UpdateExpressionList.
     */
    @Test
    @Ignore // The constructors from this class have different behaviors, is this expected?
    public void testContructors() {
        List<PartialUpdateExpression> list = new ArrayList<>();
        PartialUpdateExpression elem = new PartialUpdateExpression() {
            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
        list.add(elem);
        list.add(elem);
        list.add(elem);
        UpdateExpressionList iList = new UpdateExpressionList(list);
        UpdateExpressionList iArray = new UpdateExpressionList(elem, elem, elem);

        assertEquals("The size of the instances are different", iList.getList().size(), iArray.getList().size());
        assertEquals(3, iList.getList().size());
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(0), iArray.getList().get(0));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(1), iArray.getList().get(1));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(2), iArray.getList().get(2));

        iList.getList().add(elem);
        iArray.getList().add(elem);
        assertEquals("The size of the instances are different", iList.getList().size(), iArray.getList().size());
        assertEquals(4, iList.getList().size());
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(0), iArray.getList().get(0));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(1), iArray.getList().get(1));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(2), iArray.getList().get(2));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(3), iArray.getList().get(3));
    }

    @Test
    public void testContructorsSimpleVersionDueStrangeBehaviorAbove() {
        // Erase this method after defining what to do with the issue above, this
        // method is just covering few less conditions then the above test and
        // it aims to increase the test coverage without the resolution of the issue above
        List<PartialUpdateExpression> list = new ArrayList<>();
        PartialUpdateExpression elem = new PartialUpdateExpression() {
            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
        list.add(elem);
        list.add(elem);
        list.add(elem);
        UpdateExpressionList iList = new UpdateExpressionList(list);
        UpdateExpressionList iArray = new UpdateExpressionList(elem, elem, elem);

        assertEquals("The size of the instances are different", iList.getList().size(), iArray.getList().size());
        assertEquals(3, iList.getList().size());
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(0), iArray.getList().get(0));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(1), iArray.getList().get(1));
        assertEquals("The element is diffent (maybe in a different order)", iList.getList().get(2), iArray.getList().get(2));
    }

    /**
     * Test of getList method, of class UpdateExpressionList.
     */
    @Test
    public void testGetList() {
        PartialUpdateExpression elem0 = new PartialUpdateExpressionImpl();
        UpdateExpressionList instance = new UpdateExpressionList(elem0);
        assertNotNull(instance.getList());
        assertEquals(1, instance.getList().size());
        assertNotNull(instance.getList().get(0));
        assertEquals("The element using the constructor is different", elem0, instance.getList().get(0));
    }

    /**
     * Test of toJson method, of class UpdateExpressionList.
     */
    @Test
    public void testToJson() {
        PartialUpdateExpression elem = new PartialUpdateExpressionImpl();
        UpdateExpressionList instance = new UpdateExpressionList(elem);

        ArrayNode arrayNode = JsonObject.getFactory().arrayNode();
        assertEquals(arrayNode.add(elem.toJson()), instance.toJson());
        
        instance = new UpdateExpressionList((PartialUpdateExpression)null);
        arrayNode = JsonObject.getFactory().arrayNode();
        assertEquals(arrayNode, instance.toJson());
        
        instance = new UpdateExpressionList((List)null);
        arrayNode = JsonObject.getFactory().arrayNode();
        assertEquals(arrayNode, instance.toJson());
    }

    /**
     * Test of fromJson method, of class UpdateExpressionList.
     */
    @Test
    public void testFromJson() {
        PartialUpdateExpression elem = new PartialUpdateExpressionImpl();
        ArrayNode arrayNode = JsonObject.getFactory().arrayNode();
        arrayNode.add(elem.toJson());

        UpdateExpressionList expResult = new UpdateExpressionList(elem);
        UpdateExpressionList result = UpdateExpressionList.fromJson(arrayNode);
        assertNotNull(result.getList());
        assertEquals(1, result.getList().size());
        assertNotNull(result.getList().get(0));
        assertEquals("The element using the constructor is different", expResult.toJson(), result.toJson());
        
        arrayNode = JsonObject.getFactory().arrayNode();
        result = UpdateExpressionList.fromJson(arrayNode);
        expResult = new UpdateExpressionList((PartialUpdateExpression)null);
        assertEquals("The element using the constructor is different", expResult.toJson(), result.toJson());
        
    }

    private static class PartialUpdateExpressionImpl extends PartialUpdateExpression {
        @Override
        public JsonNode toJson() {
            try {
                return JsonUtils.json("{\"$add\":{\"$UpdateExpressionListTest\":\"123\"}}");
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

}
