package com.redhat.lightblue.query;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.skyscreamer.jsonassert.JSONAssert;

import com.redhat.lightblue.util.JsonUtils;

public class SortParseTest {
    final String doc1="{\"field\":\"$asc\"}";
    final String doc2="[ {\"field\":\"$asc\"},{\"field2\":\"$desc\"},{\"field3.x\":\"$desc\"} ]";

    @Test
    public void basicSortParseTest() throws Exception {
        Sort s=Sort.fromJson(JsonUtils.json(doc1));
        Assert.assertTrue(s instanceof SortKey);
        SortKey k=(SortKey)s;
        Assert.assertEquals("field",k.getField().toString());
        Assert.assertTrue(!k.isDesc());
    }

    @Test
    public void compositeSortParseTest() throws Exception {
        Sort s=Sort.fromJson(JsonUtils.json(doc2));
        Assert.assertTrue(s instanceof CompositeSortKey);
        CompositeSortKey k=(CompositeSortKey)s;
        Assert.assertEquals(3,k.getKeys().size());
        Assert.assertEquals("field",k.getKeys().get(0).getField().toString());
        Assert.assertTrue(!k.getKeys().get(0).isDesc());
        Assert.assertEquals("field2",k.getKeys().get(1).getField().toString());
        Assert.assertTrue(k.getKeys().get(1).isDesc());
        Assert.assertEquals("field3.x",k.getKeys().get(2).getField().toString());
        Assert.assertTrue(k.getKeys().get(2).isDesc());
    }

    @Test
    public void sortConversionTest() throws Exception {
        JSONAssert.assertEquals(doc1,Sort.fromJson(JsonUtils.json(doc1)).toString(),false);
        JSONAssert.assertEquals(doc2,Sort.fromJson(JsonUtils.json(doc2)).toString(),false);
    }
}
