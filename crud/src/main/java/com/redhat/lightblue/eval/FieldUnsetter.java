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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.metadata.EntityMetadata;

/**
 * Unsets (removes) some fields
 */
public class FieldUnsetter extends Updater {

    private static final Logger logger = LoggerFactory.getLogger(FieldUnsetter.class);

    private final List<Path> fields;

    /**
     * Ctor
     *
     * @param md Entity metadata
     * @param fields List of fields to unset
     */
    public FieldUnsetter(List<Path> fields) {
        this.fields=fields;
    }

   /**
     * Removes fields from the document
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret=false;
        for(Path p:fields) {
            logger.debug("Remove {}",p);
            KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(p);
            while(cursor.hasNext()) {
                JsonNode oldValue=doc.modify(cursor.getCurrentKey(),null,false);
                if(oldValue!=null)
                    ret=true;
            }
        }
        return ret;
    }
}
