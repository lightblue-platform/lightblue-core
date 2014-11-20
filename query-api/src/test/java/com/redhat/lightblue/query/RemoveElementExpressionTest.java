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
import com.redhat.lightblue.util.JsonUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author lcestari
 */
public class RemoveElementExpressionTest {

    /**
     * Test of toJson method, of class RemoveElementExpression.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testToJson() throws IOException {
        RemoveElementExpression instance = new RemoveElementExpression();
        JsonNode expResult = JsonUtils.json("\"$remove\"");
        JsonNode result = instance.toJson();
        assertEquals(expResult, result);
    }

    @Test
    public void testEqualsHashCode() {
        RemoveElementExpression instance = new RemoveElementExpression();
        assertEquals(new RemoveElementExpression().hashCode(), instance.hashCode());
        assertEquals(new RemoveElementExpression(), instance);
    }
}
