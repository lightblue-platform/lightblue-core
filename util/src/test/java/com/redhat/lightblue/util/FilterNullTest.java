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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import org.skyscreamer.jsonassert.JSONAssert;

public class FilterNullTest {

    private static String esc(String s) {
        return s.replaceAll("\'", "\"");
    }

    private static JsonNode json(String s) throws Exception {
        return JsonUtils.json(esc(s));
    }

    @Test
    public void basicRemoval() throws Exception {
        JsonNode node = json("{'x':'a','y':null}");
        JsonDoc.filterNulls(node);
        JSONAssert.assertEquals(esc("{'x':'a'}"), node.toString(), false);
    }

    @Test
    public void nestedRemoval() throws Exception {
        JsonNode node = json("{'x':'a','y':[null,{'z':null,'a':'a','b':['a','b',null]}]}");
        JsonDoc.filterNulls(node);
        JSONAssert.assertEquals(esc("{'x':'a','y':[null, {'a':'a','b':['a','b', null]}]}"), node.toString(), false);
    }
}
