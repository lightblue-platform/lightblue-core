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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author nmalik
 */
public class ErrorTest {
    @Before
    public void setup() {
        Error.reset();
    }

    /**
     * Test of push method, of class Error.
     */
    @Test
    public void testPush() {
        String contexts[] = new String[]{"1", "2"};
        String errorCode = "code";

        StringBuilder buff = new StringBuilder();

        for (String context : contexts) {
            if (buff.length() > 0) {
                buff.append(Error.DELIMITER);
            }
            Error.push(context);
            buff.append(context);
        }

        Error e = Error.get(errorCode);

        Assert.assertEquals(buff.toString(), e.getContext());
        Assert.assertEquals(errorCode, e.getErrorCode());
    }

    /**
     * Test of pop method, of class Error.
     */
    @Test
    public void testPop() {
        String contexts[] = new String[]{"1", "2"};
        String errorCode = "code";

        for (String context : contexts) {
            Error.push(context);
        }

        Error.pop();

        // testPush takes care of verifying context on pushes.. pop it and test only the first context exists still
        Error e = Error.get(errorCode);
        Assert.assertEquals(contexts[0], e.getContext());
        Assert.assertEquals(errorCode, e.getErrorCode());

        // verify pop to empty works
        Error.pop();

        e = Error.get(errorCode);
        Assert.assertEquals("", e.getContext());
        Assert.assertEquals(errorCode, e.getErrorCode());

        // verify empty pop works
        Error.pop();

        e = Error.get(errorCode);
        Assert.assertEquals("", e.getContext());
        Assert.assertEquals(errorCode, e.getErrorCode());

        // verify reset pop works
        Error.reset();
        Error.pop();

        e = Error.get(errorCode);
        Assert.assertEquals("", e.getContext());
        Assert.assertEquals(errorCode, e.getErrorCode());
    }

    /**
     * Test of get method, of class Error.
     */
    @Test
    public void testGet_3args() {
        String ctx = "context";
        String errorCode = "code";
        String msg = "message";
        Error result = Error.get(ctx, errorCode, msg);

        Assert.assertEquals(ctx, result.getContext());
        Assert.assertEquals(errorCode, result.getErrorCode());
        Assert.assertEquals(msg, result.getMsg());
    }

    /**
     * Test of get method, of class Error.
     */
    @Test
    public void testGet_2args() {
        String errorCode = "code";
        String msg = "message";
        Error result = Error.get(errorCode, msg);

        Assert.assertEquals("", result.getContext());
        Assert.assertEquals(errorCode, result.getErrorCode());
        Assert.assertEquals(msg, result.getMsg());
    }

    /**
     * Test of get method, of class Error.
     */
    @Test
    public void testGet_1arg() {
        String errorCode = "code";
        Error result = Error.get(errorCode);

        Assert.assertEquals("", result.getContext());
        Assert.assertEquals(errorCode, result.getErrorCode());
        Assert.assertNull(result.getMsg());
    }

    /**
     * Test of reset method, of class Error.
     */
    @Test
    public void testReset() {
        String context = "test";
        Error.push(context);

        Error before = Error.get("code");

        Assert.assertEquals(context, before.getContext());

        Error.reset();

        Error after = Error.get("code");

        Assert.assertEquals("", after.getContext());
    }

    /**
     * Test of pushContext method, of class Error.
     */
    @Test
    public void testPushContext() {
        List<String> contexts = new ArrayList<>();
        contexts.add("1");
        contexts.add("2");

        for (String context : contexts) {
            Error.push(context);
        }

        Error result = Error.get("code");

        String pushContext = "3";
        contexts.add(pushContext);
        result.pushContext(pushContext);

        StringBuilder buff = new StringBuilder();

        for (String context : contexts) {
            if (buff.length() > 0) {
                buff.append(Error.DELIMITER);
            }
            buff.append(context);
        }

        Assert.assertEquals(buff.toString(), result.getContext());
    }

    /**
     * Test of popContext method, of class Error.
     */
    @Test
    public void testPopContext() {
        List<String> contexts = new ArrayList<>();
        contexts.add("1");
        contexts.add("2");

        for (String context : contexts) {
            Error.push(context);
        }

        Error result = Error.get("code");

        String pushContext = "3";
        result.pushContext(pushContext);
        result.popContext();

        StringBuilder buff = new StringBuilder();

        for (String context : contexts) {
            if (buff.length() > 0) {
                buff.append(Error.DELIMITER);
            }
            buff.append(context);
        }

        Assert.assertEquals(buff.toString(), result.getContext());
    }

    @Test
    public void toJson_full() throws JSONException {
        Error e = Error.get("a", "b", "c");
        JsonNode node = e.toJson();
        JSONAssert.assertEquals("{objectType:error,context:a,errorCode:b,msg:c}", node.toString(), false);
    }

    @Test
    public void testToString() throws JSONException {
        // alias for toJson, so not going to test all cases
        Error e = Error.get("a", "b", "c");
        String s = e.toString();
        JSONAssert.assertEquals("{objectType:error,context:a,errorCode:b,msg:c}", s, false);
    }

    @Test
    public void toJson_empty() throws Exception {
        Method method = Error.class.getMethod("get", String.class);
        Error e = (Error) method.invoke(null, new Object[]{null});
        JsonNode node = e.toJson();
        JSONAssert.assertEquals("{}", node.toString(), false);
    }

    @Test
    public void fromJson() {
        Error e = Error.get("a", "b", "c");
        JsonNode node = e.toJson();
        // toJson test passes, we can trust this to be valid JsonNode
        Error fromJson = Error.fromJson(node);
        Assert.assertEquals("a", fromJson.getContext());
        Assert.assertEquals("b", fromJson.getErrorCode());
        Assert.assertEquals("c", fromJson.getMsg());
    }
}
