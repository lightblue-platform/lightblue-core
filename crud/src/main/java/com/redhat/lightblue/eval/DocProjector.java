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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.ObjectField;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonNodeCursor;

public class DocProjector {

    private static final Logger logger=LoggerFactory.getLogger(DocProjector.class);

    private final JsonNodeFactory factory;

    public DocProjector(JsonNodeFactory factory) {
        this.factory=factory;
    }

    public JsonDoc project(JsonDoc doc,
                           Projector p,
                           EntityMetadata md,
                           QueryEvaluationContext ctx) {
        JsonNodeCursor cursor=doc.cursor();
        cursor.firstChild();
        ObjectNode root=projectObject(p,md.getFieldTreeRoot(),Path.EMPTY,cursor,ctx);
        return new JsonDoc(root);
    }

    private ObjectNode projectObject(Projector projector,
                                     FieldTreeNode mdContext,
                                     Path contextPath,
                                     JsonNodeCursor cursor,
                                     QueryEvaluationContext ctx) {
        ObjectNode ret=factory.objectNode();
        do {
            Path fieldPath=cursor.getCurrentPath();
            // The context path *is* a prefix of the field path 
            Path contextRelativePath=fieldPath.suffix(-contextPath.numSegments());
            JsonNode fieldNode=cursor.getCurrentNode();
            FieldTreeNode fieldMd=mdContext.resolve(contextRelativePath);
            logger.debug("projectObject context={} fieldPath={} contextRelativePath={}",contextPath,fieldPath,contextRelativePath);
            if(fieldMd!=null) {
                logger.debug("Projecting {} in context {}",contextRelativePath,contextPath);
                Boolean result=projector.project(fieldPath,ctx);
                if(result!=null) {
                    if(result) {
                        logger.debug("Projection includes {}",fieldPath);
                        if(fieldMd instanceof ObjectField) {
                            if(fieldNode instanceof ObjectNode) {
                                if(cursor.firstChild()) {
                                    ObjectNode newNode=projectObject(projector,mdContext,contextPath,cursor,ctx);
                                    ret.set(fieldPath.tail(0),newNode);
                                    cursor.parent();
                                } else
                                    ret.set(fieldPath.tail(0),factory.objectNode());
                            } else
                                logger.warn("Expecting object node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                        } else if(fieldMd instanceof SimpleField) {
                            if(fieldNode.isValueNode()) {
                                ret.set(fieldPath.tail(0),fieldNode);
                            } else
                                logger.warn("Expecting value node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                        } else if(fieldMd instanceof ArrayField) {
                            if(fieldNode instanceof ArrayNode) {
                                ArrayNode newNode=factory.arrayNode();
                                ret.set(fieldPath.tail(0),newNode);
                                if(cursor.firstChild()) {
                                    do {
                                        JsonNode node=projectArrayElement(projector.getNestedProjector()==null?projector:
                                                                          projector.getNestedProjector(),
                                                                          ((ArrayField)fieldMd).getElement(),
                                                                          fieldPath,
                                                                          cursor,
                                                                          ctx);
                                        if(node!=null)
                                            newNode.add(node);
                                    } while(cursor.nextSibling());
                                    cursor.parent();
                                }
                            } else
                                logger.warn("Expecting array node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                        }
                    } else
                        logger.debug("Projection excludes {}",fieldPath);
                } else
                    logger.debug("No projection match for {}",fieldPath);
            } else
                logger.warn("Unknown field {}",fieldPath);
        } while(cursor.nextSibling());
        return ret;
    }

    private JsonNode projectArrayElement(Projector projector,
                                         ArrayElement mdContext,
                                         Path contextPath,
                                         JsonNodeCursor cursor,
                                         QueryEvaluationContext ctx) {
        Path elemPath=cursor.getCurrentPath();
        Path contextRelativeElemPath=elemPath.suffix(-contextPath.numSegments());
        logger.debug("Project array element {} contextRelative {} context {}",elemPath,contextRelativeElemPath,contextPath);
        Boolean result=projector.project(contextRelativeElemPath,ctx);
        if(result!=null)
            if(result) {
                logger.debug("Projection includes {}",elemPath);
                if(mdContext instanceof SimpleArrayElement) {            
                    return cursor.getCurrentNode();
                } else {
                    // Object array element
                    return projectObject(projector,mdContext,contextPath,cursor,ctx);
                }
            } else
                logger.debug("Projection excludes {}",elemPath);
        else
            logger.debug("No projection match for {}",elemPath);
        return null;
    }
}
