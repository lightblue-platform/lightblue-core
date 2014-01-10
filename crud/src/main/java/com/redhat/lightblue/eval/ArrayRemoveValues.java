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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.crud.ArrayRemoveValuesExpression;

import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.types.Type;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

/**
 * Removes values from an array
 */
public class ArrayRemoveValues extends Updater {

    private static final Logger logger = LoggerFactory.getLogger(ArrayRemoveValues.class);

    private final Path field;
    private final Set<Object> values;
    private final Type type;

    /**
     * Ctor
     *
     * Saves the values to be removed by type-casting them to the appropriate type
     */
    public ArrayRemoveValues(EntityMetadata md, ArrayRemoveValuesExpression expr) {
        this.field = expr.getField();
        FieldTreeNode node = md.resolve(field);
        if (node instanceof ArrayField) {
            type = ((ArrayField) node).getElement().getType();
            List<Value> valueList = expr.getValues();
            values = new HashSet(valueList.size());
            for (Value x : valueList) {
                values.add(type.cast(x.getValue()));
            }
        } else {
            throw new EvaluationError("Expected array field:" + field);
        }
    }

    /**
     * Removes the elements that match the values
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret = false;
        logger.debug("Remove values {} from {} ", values, field);
        KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(field);
        while (cursor.hasNext()) {
            JsonNode node = cursor.getCurrentValue();
            if (node instanceof ArrayNode) {
                List<Integer> deleteList = new ArrayList<>();
                int index = 0;
                for (Iterator<JsonNode> itr = ((ArrayNode) node).elements(); itr.hasNext();) {
                    JsonNode element = itr.next();
                    if (element != null) {
                        Object obj = type.fromJson(element);
                        if (obj != null) {
                            if (values.contains(obj)) {
                                deleteList.add(index);
                            }
                        }
                    }
                    index++;
                }
                logger.debug("Removing {} from {}", deleteList, field);
                for (int i = deleteList.size() - 1; i >= 0; i--) {
                    ((ArrayNode) node).remove(deleteList.get(i));
                }
            } else {
                logger.warn("Expected array node for {}, got {}", cursor.getCurrentKey(), node.getClass().getName());
            }
        }
        return ret;
    }
}
