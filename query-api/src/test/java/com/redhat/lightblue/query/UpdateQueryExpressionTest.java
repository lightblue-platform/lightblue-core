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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.JsonUtils;

/**
 *
 * @author lcestari
 */
public class UpdateQueryExpressionTest {
    
    public UpdateQueryExpressionTest() {
    }

    /**
     * Test of fromJson method, of class UpdateQueryExpression.
     */
    @Test
    public void testFromJson() throws IOException {
        JsonNode node = null;
        QueryExpression expResult = null;
        QueryExpression result = null;
        
        node = JsonUtils.json("\"$all\"");
        
        result = UpdateQueryExpression.fromJson(node);
        expResult = new AllMatchExpression();
        assertEquals(expResult, result);
        
        result = UpdateQueryExpression.fromJson(JsonUtils.json("{\"field\":\"field6.nf1\",\"regex\":\"Nvalue.*\"}"));
        assertNotEquals(expResult, result);
        
    }
    
}
