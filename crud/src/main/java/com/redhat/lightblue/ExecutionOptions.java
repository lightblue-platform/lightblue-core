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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.JsonObject;

/**
 * Execution options setting time limit and whether the execution is to be completed asynchronously
 */
public class ExecutionOptions extends JsonObject {

    private long timeLimit;
    private long asynchronous;

    /**
     * The time limit within which the call should complete. The call stops execution once the limit is exceeded, and
     * partial results are returned
     */
    public long getTimeLimit() {
        return timeLimit;
    }

    /**
     * The time limit within which the call should complete. The call stops execution once the limit is exceeded, and
     * partial results are returned
     */
    public void setTimeLimit(long l) {
        timeLimit = l;
    }

    /**
     * If execution lasts longer than this timeout, continue asynchronously
     */
    public long getAsynchronous() {
        return asynchronous;
    }

    /**
     * If execution lasts longer than this timeout, continue asynchronously
     */
    public void setAsynchronous(long b) {
        asynchronous = b;
    }

    /**
     * Returns a json representation of this
     */
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode();
        node.put("timeLimit", timeLimit);
        node.put("asynchronous", asynchronous);
        return node;
    }

    /**
     * Parses execution options from a json object. Unrecognized elements are ignored.
     */
    public static ExecutionOptions fromJson(ObjectNode node) {
        ExecutionOptions ret = new ExecutionOptions();
        JsonNode x = node.get("timeLimit");
        if (x != null) {
            ret.timeLimit = x.asLong();
        }
        x = node.get("asynchronous");
        if (x != null) {
            ret.asynchronous = x.asLong();
        }
        return ret;
    }
}
