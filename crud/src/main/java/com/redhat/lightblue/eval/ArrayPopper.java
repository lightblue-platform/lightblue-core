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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.crud.ArrayPopExpression;

/**
 * Removes the first of the last element from an array
 */
public class ArrayPopper extends Updater {

    private static final Logger logger = LoggerFactory.getLogger(ArrayPopper.class);

    private final Path field;
    private final boolean first;

    public ArrayPopper(ArrayPopExpression expr) {
        this.field = expr.getField();
        this.first = expr.isFirst();
    }

    /**
     * Removes the first or the last element from an array
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret = false;
        logger.debug("Pop {} first={}", field, first);
        KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(field);
        while (cursor.hasNext()) {
            JsonNode node = cursor.getCurrentValue();
            if (node instanceof ArrayNode) {
                int size = ((ArrayNode) node).size();
                if (size > 0) {
                    int n = first ? 0 : size - 1;
                    ((ArrayNode) node).remove(n);
                    ret = true;
                }
            } else {
                logger.warn("Expected array node for {}, got {}", cursor.getCurrentKey(), node.getClass().getName());
            }
        }
        return ret;
    }
}
