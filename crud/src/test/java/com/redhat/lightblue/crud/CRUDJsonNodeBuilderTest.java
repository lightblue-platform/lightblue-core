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
package com.redhat.lightblue.crud;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.query.QueryExpression;

public class CRUDJsonNodeBuilderTest {

    CRUDJsonNodeBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new CRUDJsonNodeBuilder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddQueryExpression() {
    }

    @Test
    public void testAddQueryExpressionNull() {
        builder.add("query", (QueryExpression) null);
        assertNull(builder.build().get("query"));
    }

    @Test
    public void testAddProjection() {

    }

    @Test
    public void testAddSort() {

    }

    @Test
    public void testAddUpdateExpression() {

    }

    @Test
    public void testBuild() {
        assertTrue(builder.build() instanceof JsonNode);
    }

}
