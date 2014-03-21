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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import com.redhat.lightblue.util.Error;
import java.util.Arrays;

/**
 *
 * @author lcestari
 */
public class PrimitiveUpdateExpressionTest {

    public PrimitiveUpdateExpressionTest() {
    }

    /**
     * Test of fromJson method, of class PrimitiveUpdateExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        List<FieldAndRValue> list = new ArrayList<>();
        FieldAndRValue fav = new FieldAndRValue();
        fav.setField(Path.EMPTY);
        fav.setRValue(new RValueExpression(Path.EMPTY));
        list.add(fav);
        SetExpression instance = null;

        PrimitiveUpdateExpression expResult = new SetExpression(UpdateOperator._set, list);
        PrimitiveUpdateExpression result = PrimitiveUpdateExpression.fromJson((ObjectNode) JsonUtils.json("{\"$set\":{\"\":{\"$valueof\":\"\"}}}"));
        assertEquals(expResult, result);

        expResult = new SetExpression(UpdateOperator._add, list);
        result = PrimitiveUpdateExpression.fromJson((ObjectNode) JsonUtils.json("{\"$add\":{\"\":{\"$valueof\":\"\"}}}"));
        assertEquals(expResult, result);

        expResult = new UnsetExpression(Arrays.asList(Path.EMPTY));
        result = PrimitiveUpdateExpression.fromJson((ObjectNode) JsonUtils.json("{\"$unset\":[\"\"]}"));
        assertEquals(expResult, result);

        boolean error = false;
        try {

            result = PrimitiveUpdateExpression.fromJson((ObjectNode) JsonUtils.json("{\"$append\":{\"\":{\"$valueof\":\"\"}}}"));
            fail();
        } catch (Error e) {
            error = true;
            String expError = Error.get(
                    QueryConstants.ERR_INVALID_UPDATE_EXPRESSION,
                    JsonUtils.json("{\"$append\":{\"\":{\"$valueof\":\"\"}}}").toString())
                    .toString();
            assertEquals(expError, e.toString());
        }
        assertTrue("Execption expected!", error);
    }
    /*
     if (node.has(UpdateOperator._add.toString()) || node.has(UpdateOperator._set.toString())) {
     return SetExpression.fromJson(node);
     } else if (node.has(UpdateOperator._unset.toString())) {
     return UnsetExpression.fromJson(node);
     } else {
     throw Error.get(QueryConstants.ERR_INVALID_UPDATE_EXPRESSION, node.toString());
     }
     */
}
