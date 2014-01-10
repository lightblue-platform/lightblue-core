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

import com.redhat.lightblue.crud.FieldValue;

/**
 * Sets some fields to new values
 */
public class FieldSetter extends Updater {

    private static final Logger logger = LoggerFactory.getLogger(FieldSetter.class);

    private final Map<Path,JsonNode> map=new HashMap<Path,JsonNode>();

    /**
     * Ctor
     *
     * @param md Entity metadata
     * @param values Field-value pairs to update
     *
     * This constructor interprets the values based on the type information in metadata
     */
    public FieldSetter(JsonNodeFactory factory,EntityMetadata md,List<FieldValue> values) {
        for(FieldValue x:values) {
            FieldTreeNode node=md.resolve(x.getField());
            if(node==null)
                throw new EvaluationError("Unknown field:"+x.getField());
            map.put(x.getField(),node.getType().toJson(factory,x.getValue().getValue()));
        }
    }

   /**
     * Updates the fields in the document with the new values.
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret=false;
        for(Map.Entry<Path,JsonNode> x:map.entrySet()) {
            Path p=x.getKey();
            JsonNode value=x.getValue();
            logger.debug("Set {} = {}",p,value);
            KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(p);
            while(cursor.hasNext()) {
                JsonNode oldValue=doc.modify(cursor.getCurrentKey(),value,false);
                if(!ret) {
                    if(value!=null) {
                        if(!oldValue.equals(value)) {
                            ret=true;
                        } else {
                            ret=false;
                        }
                    } else {
                        ret=true;
                    }
                }
            }
        }
        return ret;
    }
}
