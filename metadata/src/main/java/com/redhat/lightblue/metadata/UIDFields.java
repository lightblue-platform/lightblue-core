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
package com.redhat.lightblue.metadata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.JsonNodeCursor;

import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.types.UIDType;

/**
 */
public final class UIDFields {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIDFields.class);

    public static void initializeUIDFields(JsonNodeFactory factory,EntityMetadata md,JsonDoc doc) {
        FieldCursor cursor=md.getFieldCursor();
        while(cursor.next()) {
            FieldTreeNode node=cursor.getCurrentNode();
            // Process all UID fields
            if(node.getType().equals(UIDType.TYPE)) {
                Field field=(Field)node;
                Path p=cursor.getCurrentPath();
                LOGGER.debug("Processing UID field {}",p);
                if(required(field)) {
                    LOGGER.debug("Field {} is required",p);
                    setRequiredField(factory,doc,p,1,p.prefix(1));
                } else {
                    LOGGER.debug("Field {} is not required",p);
                    JsonNodeCursor nodeCursor=doc.cursor(p);
                    while(nodeCursor.next()) {
                        JsonNode valueNode=nodeCursor.getCurrentNode();
                        if(valueNode.isNull()||valueNode.asText().length()==0) {
                            String value=UIDType.newValue();
                            LOGGER.debug("Setting {} to {}",nodeCursor.getCurrentPath(),value);
                            doc.modify(nodeCursor.getCurrentPath(),factory.textNode(value),true);
                        }
                    }
                }
            }
        }
    }

    private static void setRequiredField(JsonNodeFactory factory,
                                         JsonDoc doc,
                                         Path fieldPath,
                                         int startSegment,
                                         Path targetPath) {
        int nSegments=fieldPath.numSegments();
        boolean array=false;
        for(int segment=startSegment;segment<nSegments;segment++) {
            if(fieldPath.head(segment).equals(Path.ANY)) {
                array=true;
                MutablePath arrPath=new MutablePath(fieldPath.head(segment-1));
                JsonNode node=doc.get(arrPath);
                int size=node.size();
                arrPath.push(0);
                for(int i=0;i<size;i++) {
                    setRequiredField(factory,doc,fieldPath,segment+1,arrPath.immutableCopy());
                    arrPath.setLast(i);
                }
                break;
            }
        }
        if(!array) {
            LOGGER.debug("Setting {}",targetPath);
            JsonNode valueNode=doc.get(targetPath);
            if(valueNode==null||valueNode.isNull()||valueNode.asText().length()==0) {
                String value=UIDType.newValue();
                LOGGER.debug("Setting {} to {}",targetPath,value);
                doc.modify(targetPath,factory.textNode(value),true);
            }
        }
    }
                                         
    private static boolean required(Field f) {
        List<FieldConstraint> constraints=f.getConstraints();
        if(constraints!=null)
            for(FieldConstraint c:constraints)
                if(c instanceof RequiredConstraint)
                    return true;
        return false;
    }

    private UIDFields() {
    }
}
