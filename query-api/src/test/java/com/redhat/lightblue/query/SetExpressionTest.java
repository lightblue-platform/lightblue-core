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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lcestari
 */
public class SetExpressionTest {

    @Test
    public void testConstructors() {
        List<FieldAndRValue> list = null;
        FieldAndRValue fav = null;
        SetExpression instance = null;

        instance = new SetExpression(UpdateOperator._set, list);
        assertEquals(UpdateOperator._set, instance.getOp());
        assertEquals(list, instance.getFields());

        instance = new SetExpression(UpdateOperator._add, list);
        assertEquals(UpdateOperator._add, instance.getOp());
        assertEquals(list, instance.getFields());

        boolean error = false;
        try {
            instance = new SetExpression(UpdateOperator._foreach, list);
        } catch (IllegalArgumentException e) {
            error = true;
        }
        if (!error) {
            fail("Expected IllegalArgumentException");
        }

        error = false;
        instance = new SetExpression(UpdateOperator._set, fav);
        assertEquals(UpdateOperator._set, instance.getOp());
        assertEquals(Arrays.asList(fav), instance.getFields());

        instance = new SetExpression(UpdateOperator._add, fav);
        assertEquals(UpdateOperator._add, instance.getOp());
        assertEquals(Arrays.asList(fav), instance.getFields());

        try {
            instance = new SetExpression(UpdateOperator._foreach, fav);
        } catch (IllegalArgumentException e) {
            error = true;
        }
        if (!error) {
            fail("Expected IllegalArgumentException");
        }
    }

    /**
     * Test of toJson method, of class SetExpression.
     */
    @Test
    public void testToJson() {
        List<FieldAndRValue> list = new ArrayList<>();
        FieldAndRValue fav = new FieldAndRValue();
        fav.setField(Path.EMPTY);
        fav.setRValue(new RValueExpression(Path.EMPTY));
        list.add(fav);
        SetExpression instance = null;

        instance = new SetExpression(UpdateOperator._set, list);

        JsonNode result = instance.toJson();
        assertEquals("{\"$set\":{\"\":{\"$valueof\":\"\"}}}", result.toString());
    }

    /**
     * Test of fromJson method, of class SetExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        List<FieldAndRValue> list = new ArrayList<>();
        FieldAndRValue fav = new FieldAndRValue();
        fav.setField(Path.EMPTY);
        fav.setRValue(new RValueExpression(Path.EMPTY));
        list.add(fav);
        SetExpression expResult = new SetExpression(UpdateOperator._set, list);
        SetExpression result = SetExpression.fromJson((ObjectNode) JsonUtils.json("{\"$set\":{\"\":{\"$valueof\":\"\"}}}"));
        assertEquals(expResult, result);
    }

}
