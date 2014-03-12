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
