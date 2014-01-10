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
package com.redhat.lightblue.eval;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.types.Type;

import com.redhat.lightblue.crud.FieldValue;

/**
 * Adds to field values
 */
public class FieldAdder extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldAdder.class);

    /**
     * Keeps the field type and correctly casted value to be added to the field
     */
    private static final class TypeAndValue {
        private final Type t;
        private final Object value;

        public TypeAndValue(Type t, Object value) {
            this.t = t;
            this.value = t.cast(value);
        }

        public Type getType() {
            return t;
        }

        public Object getValue() {
            return value;
        }
    }

    private final Map<Path, TypeAndValue> map = new HashMap<Path, TypeAndValue>();
    private final JsonNodeFactory factory;

    /**
     * Ctor
     *
     */
    public FieldAdder(JsonNodeFactory factory, EntityMetadata md, List<FieldValue> values) {
        this.factory = factory;
        for (FieldValue x : values) {
            FieldTreeNode node = md.resolve(x.getField());
            if (node == null) {
                throw new EvaluationError("Unknown field:" + x.getField());
            }
            map.put(x.getField(), new TypeAndValue(node.getType(), x.getValue().getValue()));
        }
    }

    /**
     * Adds values to the fields
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret = false;
        for (Map.Entry<Path, TypeAndValue> x : map.entrySet()) {
            Path p = x.getKey();
            TypeAndValue tvalue = x.getValue();
            LOGGER.debug("Add  {} to {}", tvalue.getValue(), p);
            KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(p);
            while (cursor.hasNext()) {
                JsonNode oldNode = cursor.getCurrentValue();
                Object oldValue = tvalue.getType().fromJson(oldNode);
                Object newValue;
                if (oldValue instanceof BigDecimal) {
                    newValue = ((BigDecimal) oldValue).add((BigDecimal) tvalue.getValue());
                } else if (oldValue instanceof BigInteger) {
                    newValue = ((BigInteger) oldValue).add((BigInteger) tvalue.getValue());
                } else if (oldValue instanceof Double) {
                    newValue = ((Double) oldValue).doubleValue() + ((Double) tvalue.getValue()).doubleValue();
                } else if (oldValue instanceof Long) {
                    newValue = ((Long) oldValue).longValue() + ((Long) tvalue.getValue()).longValue();
                } else {
                    throw new EvaluationError("Unsupported value type:" + oldValue.getClass().getName());
                }
                JsonNode newNode = tvalue.getType().toJson(factory, newValue);
                doc.modify(cursor.getCurrentKey(), newNode, false);
                if (!ret) {
                    ret = !oldNode.equals(newNode);
                }
            }
        }
        return ret;
    }
}
