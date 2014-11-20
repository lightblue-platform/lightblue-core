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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class SortParseTest {
    final String doc1 = "{\"field\":\"$asc\"}";
    final String doc2 = "[ {\"field\":\"$asc\"},{\"field2\":\"$desc\"},{\"field3.x\":\"$desc\"} ]";

    @Test
    public void basicSortParseTest() throws Exception {
        Sort s = Sort.fromJson(JsonUtils.json(doc1));
        Assert.assertTrue(s instanceof SortKey);
        SortKey k = (SortKey) s;
        Assert.assertEquals("field", k.getField().toString());
        Assert.assertTrue(!k.isDesc());
    }

    @Test
    public void compositeSortParseTest() throws Exception {
        Sort s = Sort.fromJson(JsonUtils.json(doc2));
        Assert.assertTrue(s instanceof CompositeSortKey);
        CompositeSortKey k = (CompositeSortKey) s;
        Assert.assertEquals(3, k.getKeys().size());
        Assert.assertEquals("field", k.getKeys().get(0).getField().toString());
        Assert.assertTrue(!k.getKeys().get(0).isDesc());
        Assert.assertEquals("field2", k.getKeys().get(1).getField().toString());
        Assert.assertTrue(k.getKeys().get(1).isDesc());
        Assert.assertEquals("field3.x", k.getKeys().get(2).getField().toString());
        Assert.assertTrue(k.getKeys().get(2).isDesc());
    }

    @Test
    public void sortConversionTest() throws Exception {
        JSONAssert.assertEquals(doc1, Sort.fromJson(JsonUtils.json(doc1)).toString(), false);
        JSONAssert.assertEquals(doc2, Sort.fromJson(JsonUtils.json(doc2)).toString(), false);
    }
}
