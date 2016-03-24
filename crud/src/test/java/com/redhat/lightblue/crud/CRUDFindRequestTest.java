/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

/**
 * Created just to verify range. Should be enhanced to check other attributes on
 * the class.
 *
 * @author nmalik
 */
public class CRUDFindRequestTest extends AbstractJsonNodeTest {

    @Test
    public void test_range() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(0, req.getFrom().longValue());
        Assert.assertEquals(10, req.getTo().longValue());
    }

    @Test
    public void test_fromTo() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-from-to.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(100, req.getFrom().longValue());
        Assert.assertEquals(200, req.getTo().longValue());
    }

    @Test
    public void test_from() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-from.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(100, req.getFrom().longValue());
        Assert.assertNull(req.getTo());
    }

    @Test
    public void test_maxResults() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-max.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(99, req.getTo().longValue());
        Assert.assertNull(req.getFrom());
    }

    @Test
    public void test_to() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-to.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertNull(req.getFrom());
        Assert.assertEquals(200, req.getTo().longValue());
    }

    @Test
    public void test_zero_maxResults() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-0-max.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(0, req.getTo().longValue());
        Assert.assertNull(req.getFrom());
    }
    
    @Test
    public void test_one_maxResults() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-1-max.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(1, req.getTo().longValue());
        Assert.assertNull(req.getFrom());
    }

    @Test
    public void test_zero_to() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-0-to.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertNull(req.getFrom());
        Assert.assertEquals(0, req.getTo().longValue());
    }

    @Test
    public void test_null_to() throws IOException {
        JsonNode node = loadJsonNode("crud/find/schema-test-find-simple-null-to.json");
        CRUDFindRequest req = new CRUDFindRequest();
        req.fromJson((ObjectNode) node);

        Assert.assertEquals(0, req.getFrom().longValue());
        Assert.assertNull(req.getTo());
    }
}
