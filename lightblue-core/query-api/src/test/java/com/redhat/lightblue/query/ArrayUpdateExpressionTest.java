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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonUtils;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import com.redhat.lightblue.util.Error;

/**
 *
 * @author lcestari
 */
public class ArrayUpdateExpressionTest {

    public ArrayUpdateExpressionTest() {
    }

    /**
     * Test of fromJson method, of class ArrayUpdateExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        ObjectNode node = (ObjectNode) JsonUtils.json("{ \"$append\" : { \"path\" : \"rvalue_expression\" } } ");
        ArrayUpdateExpression expResult = ArrayAddExpression.fromJson(node);
        ArrayUpdateExpression result = ArrayUpdateExpression.fromJson(node);
        assertEquals(expResult, result);

        node = (ObjectNode) JsonUtils.json("{ \"$insert\" : { \"path\" : \"$all\"} } ");
        expResult = ArrayAddExpression.fromJson(node);
        result = ArrayUpdateExpression.fromJson(node);
        assertEquals(expResult, result);

        node = (ObjectNode) JsonUtils.json("{ \"$foreach\" : { \"path\" : \"$all\" ,\"$update\" : \"$remove\"} } ");
        expResult = ForEachExpression.fromJson(node);
        result = ArrayUpdateExpression.fromJson(node);
        assertEquals(expResult, result);
        boolean error = false;
        try {
            node = (ObjectNode) JsonUtils.json("{ \"$missing\" : { \"path\" : { \"path\" : \"rvalue_expression\" } }}");
            result = ArrayUpdateExpression.fromJson(node);
        } catch (Error e) {
            error = true;
            assertEquals(Error.get(QueryConstants.ERR_INVALID_ARRAY_UPDATE_EXPRESSION, node.toString()).toString(), e.toString());
        }
        assertTrue("It didn't thrown an expection as it was expected", error);
    }

}
