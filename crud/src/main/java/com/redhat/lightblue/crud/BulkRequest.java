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
package com.redhat.lightblue.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Request;

/**
 * Contains a list of requests. Each request has a sequence number that can be
 * used to match the corresponding response.
 * <pre>
 *   {
 *     "requests": [
 *         {
 *             "seq":0,
 *             "op": "FIND",
 *             "request": { request }
 *         }
 *     ]
 *   }
 * </pre>
 */
public class BulkRequest extends AbstractBulkJsonObject<Request> {

    @Override
    public JsonNode toJson() {
        ObjectNode rootJson = getFactory().objectNode();
        ArrayNode requestsJson = getFactory().arrayNode();
        rootJson.set("requests", requestsJson);
        int seq = 0;
        for (Request request : getEntries()) {
            ObjectNode requestJson = (ObjectNode) request.toJson();
            requestJson.put("seq", seq++);
            requestsJson.add(requestJson);
        }
        rootJson.put("ordered", ordered);
        return rootJson;
    }

    public static BulkRequest fromJson(ObjectNode node) {
        BulkRequest req = new BulkRequest();
        req.parseRequests((ArrayNode) node.get("requests"));
        if (node.get("ordered") != null) {
            req.ordered = node.get("ordered").asBoolean();
        }
        return req;
    }

    protected Request parseEntry(ObjectNode node) {
        JsonNode opNode = node.get("op");
        if (opNode != null) {
            Request req;
            String opstr = opNode.asText();
            JsonNode val = node.get("request");
            if (val instanceof ObjectNode) {
                if (opstr.equalsIgnoreCase(CRUDOperation.FIND.toString())) {
                    req = FindRequest.fromJson((ObjectNode) val);
                } else if (opstr.equalsIgnoreCase(CRUDOperation.INSERT.toString())) {
                    req = InsertionRequest.fromJson((ObjectNode) val);
                } else if (opstr.equalsIgnoreCase(CRUDOperation.SAVE.toString())) {
                    req = SaveRequest.fromJson((ObjectNode) val);
                } else if (opstr.equalsIgnoreCase(CRUDOperation.UPDATE.toString())) {
                    req = UpdateRequest.fromJson((ObjectNode) val);
                } else if (opstr.equalsIgnoreCase(CRUDOperation.DELETE.toString())) {
                    req = DeleteRequest.fromJson((ObjectNode) val);
                } else {
                    throw new IllegalArgumentException(opstr);
                }
                return req;
            } else {
                throw new IllegalArgumentException(opstr);
            }
        } else {
            throw new IllegalArgumentException("op");
        }
    }

    private static class SReq {
        private final int seq;
        private final Request entry;

        public SReq(int seq, Request entry) {
            this.seq = seq;
            this.entry = entry;
        }
    }

    /**
     * Parses the bulk object from the given array node
     */
    protected void parseRequests(ArrayNode entriesArray) {
        entries.clear();
        // Fill the entries into an array list, assuming for most
        // cases the array list will be ordered by the sequence
        // number. If there are out-of-sequence entries, then sort it
        List<SReq> list = new ArrayList<>(entriesArray.size());
        int lastSeq = 0;
        boolean first = true;
        boolean ooo = false;
        for (Iterator<JsonNode> itr = entriesArray.elements(); itr.hasNext();) {
            JsonNode x = itr.next();
            JsonNode val = x.get("seq");
            if (val != null) {
                int seq = val.asInt();
                if (first) {
                    lastSeq = seq;
                    first = false;
                } else if (seq < lastSeq) {
                    ooo = true;
                } else {
                    lastSeq = seq;
                }
                Request entry = parseEntry((ObjectNode) x);
                list.add(new SReq(seq, entry));
            }
        }
        if (ooo) {
            Collections.sort(list, new Comparator<SReq>() {
                @Override
                public int compare(SReq r1, SReq r2) {
                    return r1.seq - r2.seq;
                }
            });
        }
        for (SReq r : list) {
            entries.add(r.entry);
        }
    }
}
