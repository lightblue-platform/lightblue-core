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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.types.Type;

import com.redhat.lightblue.crud.ArrayPushExpression;

/**
 * Adds values to an array
 */
public class ArrayPusher extends Updater {

    private static final Logger logger = LoggerFactory.getLogger(ArrayPusher.class);

    private final Path field;
    private final List<Object> values;
    private final Type type;
    private final JsonNodeFactory factory;

    public ArrayPusher(JsonNodeFactory factory,EntityMetadata md,ArrayPushExpression expr) {
        this.field=expr.getField();
        List<Value> valueList=expr.getValues();
        this.values=new ArrayList<Object>(valueList.size());
        this.factory=factory;
        FieldTreeNode node=md.resolve(field);
        if(node instanceof ArrayField) {
            type=((ArrayField)node).getElement().getType();
            for(Value x:valueList)
                values.add(type.cast(x.getValue()));
        } else
            throw new EvaluationError("Not an arrayfield:"+expr);
    }

   /**
     * Removes the first or the last element from an array
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret=false;
        logger.debug("Push to {} ",field);
        KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(field);
        while(cursor.hasNext()) {
            JsonNode node=cursor.getCurrentValue();
            if(node instanceof ArrayNode) {
                for(Object x:values)
                    ((ArrayNode)node).add(type.toJson(factory,x));
            } else
                logger.warn("Expected array node for {}, got {}",cursor.getCurrentKey(),node.getClass().getName());
        }
        return ret;
    }
}
