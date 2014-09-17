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

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author lcestari
 */
public class ForEachExpressionTest {

    /**
     * Test of getField method, of class ForEachExpression.
     */
    @Test
    public void testGetField() {
        ForEachExpression instance = new ForEachExpression(Path.EMPTY, null, null);
        Path expResult = Path.EMPTY;
        Path result = instance.getField();
        assertEquals(expResult, result);
    }

    /**
     * Test of getQuery method, of class ForEachExpression.
     */
    @Test
    public void testGetQuery() {
        QueryExpression expResult = new QueryExpression() {
            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            @Override
            protected QueryExpression bind(Path ctx,
                                           List<FieldBinding> bindingResult,
                                           Set<Path> bindRequest) {
                return this;
            }
            @Override
            protected void getQueryFields(List<FieldInfo> fields,Path ctx) {}
       };
        ForEachExpression instance = new ForEachExpression(Path.EMPTY, expResult, null);
        QueryExpression result = instance.getQuery();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUpdate method, of class ForEachExpression.
     */
    @Test
    public void testGetUpdate() {
        UpdateExpression expResult = new UpdateExpression() {
            @Override
            public JsonNode toJson() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        ForEachExpression instance = new ForEachExpression(Path.EMPTY, null, expResult);
        UpdateExpression result = instance.getUpdate();
        assertEquals(expResult, result);
    }

    /**
     * Test of toJson method, of class ForEachExpression.
     */
    @Test
    @Ignore
    public void testToJson() {
        ForEachExpression instance = null;
        JsonNode expResult = null;
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromJson method, of class ForEachExpression.
     */
    @Test
    @Ignore
    public void testFromJson() {
        ObjectNode node = null;
        ForEachExpression expResult = null;
        ForEachExpression result = ForEachExpression.fromJson(node);
        assertEquals(expResult, result);
    }

    @Test
    public void testHashCode() {
        assertEquals(new ForEachExpression(Path.EMPTY, null, null).hashCode(), new ForEachExpression(Path.EMPTY, null, null).hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(new ForEachExpression(Path.EMPTY, null, null), new ForEachExpression(Path.EMPTY, null, null));
        ForEachExpression instance = new ForEachExpression(Path.EMPTY, null, null);
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(""));
        assertFalse(instance.equals(new ForEachExpression(Path.ANYPATH, null, null)));
    }
}
