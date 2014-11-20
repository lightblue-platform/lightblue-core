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
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author lcestari
 */
public class UnsetExpressionTest {

    /**
     * Test of getFields method, of class UnsetExpression.
     */
    @Test
    public void testGetFields() {
        List<Path> expResult = Arrays.asList(Path.EMPTY);
        UnsetExpression instance = new UnsetExpression(expResult);
        List<Path> result = instance.getFields();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromJson method, of class UnsetExpression.
     */
    @Test
    public void testFromJsonToJson() throws IOException {
        UnsetExpression expResult = null;
        UnsetExpression result = null;
        assertEquals(expResult, result);
        expResult = new UnsetExpression(Arrays.asList(Path.EMPTY));

        result = UnsetExpression.fromJson((ObjectNode) JsonUtils.json("{\"$unset\":[\"\"]}"));
        assertEquals(expResult.toJson().toString(), result.toJson().toString());

        boolean error = false;
        try {
            ObjectNode o = (ObjectNode) JsonUtils.json("{\"$set\":[\"\"]}");
            result = UnsetExpression.fromJson(o);
            fail();
        } catch (Error e) {
            error = true;
            String strError = Error.get(QueryConstants.ERR_INVALID_UNSET_EXPRESSION, JsonUtils.json("{\"$set\":[\"\"]}").toString()).toString();
            assertEquals(strError, e.toString());
        }
        if (!error) {
            fail("Execption expected!");
        }
        error = false;

        try {
            ObjectNode o = (ObjectNode) JsonUtils.json("{\"$set\":[\"\"]}");
            result = UnsetExpression.fromJson(o);
            fail();
        } catch (Error e) {
            error = true;
            String strError = Error.get(QueryConstants.ERR_INVALID_UNSET_EXPRESSION, JsonUtils.json("{\"$set\":[\"\"]}").toString()).toString();
            assertEquals(strError, e.toString());
        }
    }

}
