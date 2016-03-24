package com.redhat.lightblue.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Marker interface for requests containing a range
 */
public interface WithRange {

    /**
     * Specifies the index in the result set to start returning documents.
     * Meaningful only if sort is given. Starts from 0.
     */
    Long getFrom();

    /**
     * Specifies the last index of the document in the result set to be
     * returned. Meaningful only if sort is given. Starts from 0.
     */
    Long getTo();

    public static void toJson(WithRange range, JsonNodeFactory factory, ObjectNode node) {
        if (range.getFrom() != null) {
            node.set("from", factory.numberNode(range.getFrom()));
            if (range.getTo() != null) {
                node.set("to", factory.numberNode(range.getTo()));
            }
        }
    }

    public static Range fromJson(ObjectNode node) {
        Long from = null;
        Long to = null;
        JsonNode x = node.get("range");
        if (x instanceof ArrayNode && ((ArrayNode) x).size() == 2) {
            from = ((ArrayNode) x).get(0).asLong();
            if (!((ArrayNode) x).get(1).isNull()) {
                to = ((ArrayNode) x).get(1).asLong();
            } else {
                to = null;
            }
        } else {
            x = node.get("from");
            if (x != null) {
                from = x.asLong();
            }
            x = node.get("to");
            if (x != null) {
                to = x.asLong();
            } else {
                x = node.get("maxResults");
                if (x != null) {
                    long l = x.asLong();
                    if (l >= 0) {
                        to = (from == null ? 0 : from) + l - 1;
                    }
                }
            }
        }
        return new Range(from, to);
    }

    static class Range {
        public final Long from;
        public final Long to;

        public Range(Long from, Long to) {
            this.from = from;
            this.to = to;
        }
    }

}
