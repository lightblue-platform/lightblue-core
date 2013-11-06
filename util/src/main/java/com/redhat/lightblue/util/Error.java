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

import java.util.ArrayDeque;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Error object. Maintains an error code, message, and context of the
 * error. The context works as a stack of context information that can
 * be passed to the client as an indicator of where the error
 * happened.
 *
 * The error object also provides static APIs that keep the execution
 * context for the current thread.
 */
public class Error extends RuntimeException {

    private static final JsonNodeFactory factory=
        JsonNodeFactory.withExactBigDecimals(true);

    private static final ThreadLocal<ArrayDeque<String> > threadContext=
        new ThreadLocal < ArrayDeque<String> > () {
        @Override protected ArrayDeque<String> initialValue() {
            return new ArrayDeque<String>();
        }
    };
    
    private ArrayDeque<String> context;
    private String errorCode;
    private String msg;

    /**
     * Pushes the given context information to the current thread
     * stack
     */
    public static void push(String context) {
        threadContext.get().push(context);
    }

    /**
     * Pops the context information from current thread stack
     */
    public static void pop() {
        ArrayDeque<String> c=threadContext.get();
        if(!c.isEmpty())
            c.pop();
        if(c.isEmpty())
            threadContext.remove();
    }

    /**
     * Constructs a new error object using the current context
     */
    public static Error get(String errorCode,String msg) {
        return new Error(threadContext.get(),errorCode,msg);
    }

    /**
     * Constructs a new error object using the current context
     */
    public static Error get(String errorCode) {
        return new Error(threadContext.get(),errorCode,null);
    }

    /**
     * Resets the stack thread context
     */
    public static void reset() {
        threadContext.remove();
    }

    public Error() {
        context=new ArrayDeque<String>();
    }

    public Error(String errorCode,String msg) {
        this();
        this.errorCode=errorCode;
        this.msg=msg;
    }

    private Error(ArrayDeque<String> context,String errorCode,String msg) {
        this.context=context;
        this.errorCode=errorCode;
        this.msg=msg;
    }

    public void pushContext(String context) {
        this.context.push(context);
    }

    public void setErrorCode(String code) {
        errorCode=code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setMsg(String msg) {
        this.msg=msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getContext() {
        StringBuilder s=new StringBuilder();
        if(!context.isEmpty()) {
            boolean first=true;
            for(String x:context) {
                if(first)
                    first=false;
                else
                    s.append('/');
                s.append(x);
            }
        }
        return s.toString();
    }

    public JsonNode toJson() {
        ObjectNode node=(ObjectNode)factory.objectNode().
            put("object_type",factory.textNode("error"));
        if(!context.isEmpty())
            node.put("context",factory.textNode(getContext()));
        if(errorCode!=null)
            node.put("errorCode",factory.textNode(errorCode));
        if(msg!=null)
            node.put("msg",factory.textNode(msg));
        return node;
    }

    public String toString() {
        return toJson().toString();
    }

}
