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

import com.google.common.base.Joiner;
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

    @Test
    public void stringArrayBenchmark() throws JSONException {
        int count = 1000;
        String[] strings = new String[]{"a", "b", "c"};

        {
            long start = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                Gson g = new Gson();
                String json = g.toJson(strings);
                JSONAssert.assertEquals("[\"a\",\"b\",\"c\"]", json, false);
            }

            long end = System.currentTimeMillis();

            System.out.println("BENCHMARK (stringArrayBenchmark) | Gson = " + (end - start));
        }

        {
            long start = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                StringBuilder buff = new StringBuilder("[\"");
                buff.append(Joiner.on("\",\"").join(strings));
                buff.append("\"]");
                String json = buff.toString();
                JSONAssert.assertEquals("[\"a\",\"b\",\"c\"]", json, false);
            }

            long end = System.currentTimeMillis();

            System.out.println("BENCHMARK (stringArrayBenchmark) | Joiner+Builder = " + (end - start));
        }

        {
            long start = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                String json = String.format("[\"%s\"]", Joiner.on("\",\"").join(strings));
                JSONAssert.assertEquals("[\"a\",\"b\",\"c\"]", json, false);
            }

            long end = System.currentTimeMillis();

            System.out.println("BENCHMARK (stringArrayBenchmark) | Joiner+format = " + (end - start));
        }

        {
            long start = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                StringBuilder buff = new StringBuilder("[\"");
                for (int x = 0; x < strings.length; x++) {
                    buff.append(strings[x]);
                    if (x + 1 < strings.length) {
                        buff.append("\",\"");
                    }
                }
                buff.append("\"]");
                String json = buff.toString();
                JSONAssert.assertEquals("[\"a\",\"b\",\"c\"]", json, false);
            }

            long end = System.currentTimeMillis();

            System.out.println("BENCHMARK (stringArrayBenchmark) | StringBuilder = " + (end - start));
        }
    }
}
