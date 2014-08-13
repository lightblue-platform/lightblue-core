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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.StringTokenizer;

/**
 * Error object. Maintains an error code, message, and context of the error. The
 * context works as a stack of context information that can be passed to the
 * client as an indicator of where the error happened.
 *
 * The error object also provides static APIs that keep the execution context
 * for the current thread.
 */
public final class Error extends RuntimeException {
    private static final Logger LOGGER = LoggerFactory.getLogger(Error.class);

    private static final long serialVersionUID = 1L;

    private static final JsonNodeFactory FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private static final ThreadLocal<ArrayDeque<String>> THREAD_CONTEXT = new ThreadLocal< ArrayDeque<String>>() {
        @Override
        protected ArrayDeque<String> initialValue() {
            return new ArrayDeque<>();
        }
    };

    public static final char DELIMITER = '/';

    private final ArrayDeque<String> context;
    private final String errorCode;
    private final String msg;

    /**
     * Pushes the given context information to the current thread stack
     */
    public static void push(String context) {
        if (null == context) {
            context = "null";
        }
        LOGGER.debug("push: {}", context);
        THREAD_CONTEXT.get().addLast(context);
    }

    /**
     * Pops the context information from current thread stack
     */
    public static void pop() {
        ArrayDeque<String> c = THREAD_CONTEXT.get();
        if (!c.isEmpty()) {
            String context = c.removeLast();
            LOGGER.debug("pop: {}", context);
        }
        if (c.isEmpty()) {
            reset();
        }
    }

    /**
     * Constructs a new error object by pushing the given context on top of the
     * current context
     */
    public static Error get(String ctx, String errorCode, String msg) {
        push(ctx);
        try {
            Error x = new Error(THREAD_CONTEXT.get(), errorCode, msg);
            return x;
        } finally {
            pop();
        }
    }

    /**
     * Helper that gets a new Error with msg set to the stack trace of the given Throwable.
     */
    public static Error get(String ctx, String errorCode, Throwable msg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        msg.printStackTrace(pw);
        return get(ctx, errorCode, sw.toString());
    }

    /**
     * Constructs a new error object using the current context
     */
    public static Error get(String errorCode, String msg) {
        return new Error(THREAD_CONTEXT.get(), errorCode, msg);
    }

    /**
     * Helper that gets a new Error with msg set to the stack trace of the given Throwable.
     */
    public static Error get(String errorCode, Throwable msg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        msg.printStackTrace(pw);
        return get(errorCode, sw.toString());
    }

    /**
     * Constructs a new error object using the current context
     */
    public static Error get(String errorCode) {
        return new Error(THREAD_CONTEXT.get(), errorCode, null);
    }

    /**
     * Resets the stack thread context
     */
    public static void reset() {
        LOGGER.debug("reset");
        THREAD_CONTEXT.remove();
    }

    private Error(String errorCode, String msg) {
        this.context = new ArrayDeque<>();
        this.errorCode = errorCode;
        this.msg = msg;
    }

    private Error(ArrayDeque<String> context, String errorCode, String msg) {
        this.context = (ArrayDeque<String>) context.clone();
        this.errorCode = errorCode;
        this.msg = msg;
    }

    public void pushContext(String context) {
        this.context.addLast(context);
    }

    public void popContext() {
        this.context.removeLast();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public String getContext() {
        StringBuilder s = new StringBuilder();
        if (!context.isEmpty()) {
            boolean first = true;
            for (String x : context) {
                if (first) {
                    first = false;
                } else {
                    s.append(DELIMITER);
                }
                s.append(x);
            }
        }
        return s.toString();
    }

    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) FACTORY.objectNode();
        node.put("object_type", FACTORY.textNode("error"));
        if (!context.isEmpty()) {
            node.put("context", FACTORY.textNode(getContext()));
        }
        if (errorCode != null) {
            node.put("errorCode", FACTORY.textNode(errorCode));
        }
        if (msg != null) {
            node.put("msg", FACTORY.textNode(msg));
        }
        return node;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    public static Error fromJson(JsonNode node) {
        if (node instanceof ObjectNode) {
            String e = null;
            String m = null;

            JsonNode x;

            x = ((ObjectNode) node).get("errorCode");
            if (x != null) {
                e = x.asText();
            }
            x = ((ObjectNode) node).get("msg");
            if (x != null) {
                m = x.asText();
            }

            Error ret = new Error(e, m);

            x = ((ObjectNode) node).get("context");
            if (x != null) {
                StringTokenizer tok = new StringTokenizer(x.asText(), "/");
                while (tok.hasMoreTokens()) {
                    ret.pushContext(tok.nextToken());
                }
            }

            return ret;
        }
        return null;
    }
}
