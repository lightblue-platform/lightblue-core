/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata;

import com.google.gson.Gson;
import com.redhat.lightblue.metadata.Version;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Test various things we'll use Gson for. Documents expected results and ensures behavior doesn't change without
 * notice.
 *
 * @author nmalik
 */
public class GsonTest {

    @Test
    public void gsonStringArray() throws JSONException {
        String[] strings = new String[]{"a", "b", "c"};

        Gson g = new Gson();
        String json = g.toJson(strings);

        JSONAssert.assertEquals("[\"a\",\"b\",\"c\"]", json, false);
    }

    @Test
    public void gsonVersionArray() throws JSONException {
        Version[] versions = new Version[2];

        versions[0] = new Version("0.1.0", null, "initial");
        versions[1] = new Version("1.0.0", new String[]{"0.1.0"}, "release");

        Gson g = new Gson();
        String json = g.toJson(versions);

        JSONAssert.assertEquals("[{\"value\":\"0.1.0\",\"extendsVersions\":[],\"changelog\":\"initial\"},{\"value\":\"1.0.0\",\"extendsVersions\":[\"0.1.0\"],\"changelog\":\"release\"}]", json, false);
    }
}
