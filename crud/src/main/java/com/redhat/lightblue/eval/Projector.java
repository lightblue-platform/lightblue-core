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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonNodeCursor;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.ArrayMatchingElementsProjection;
import com.redhat.lightblue.query.ArrayRangeProjection;
import com.redhat.lightblue.query.ArrayQueryMatchProjection;

/**
 * This class evaluates a Projection.
 *
 *
 */
public abstract class Projector {

    private static final Logger logger=LoggerFactory.getLogger(Projector.class);

    private final FieldTreeNode rootMdNode;
    private final Path rootMdPath;

    protected Projector(Path ctxPath,FieldTreeNode ctx) {
        this.rootMdNode=ctx;
        this.rootMdPath=ctxPath;
    }

    /**
     * Returns the nested projector for this path *only if*
     * <code>project</code> returns true. May return null, which means
     * to continue using this projector.
     */
    public abstract Projector getNestedProjector();

    /**
     * Returns true, false, or null if the result cannot be determined.
     *
     * @param p The absolute field path
     * @param ctx Query evaluation context
     */
    public abstract Boolean project(Path p,QueryEvaluationContext ctx);

    /**
     * Builds a projector using the given projection and entity metadata
     */
    public static Projector getInstance(Projection projection,EntityMetadata md) {
        return getInstance(projection,Path.EMPTY,md.getFieldTreeRoot());
    }

    /**
     * Builds a (potentially nested) projection based on the given
     * projection, and the location in the metadata field tree.
     */
    public static Projector getInstance(Projection projection,Path ctxPath,FieldTreeNode ctx) {
        if(projection instanceof FieldProjection) {
            return new FieldProjector((FieldProjection)projection,ctxPath,ctx);
        } else if(projection instanceof ProjectionList) {
            return new ListProjector((ProjectionList)projection,ctxPath,ctx);
        } else if(projection instanceof ArrayMatchingElementsProjection)  {
            return new ArrayMatchingElementsProjector((ArrayMatchingElementsProjection)projection,ctxPath,ctx);
        } else if(projection instanceof ArrayRangeProjection)  {
            return new ArrayRangeProjector((ArrayRangeProjection)projection,ctxPath,ctx);
        } else  {
            return new ArrayQueryProjector((ArrayQueryMatchProjection)projection,ctxPath,ctx);
        }
    }

    /**
     * Projects a document
     */
    public JsonDoc project(JsonDoc doc,
                           JsonNodeFactory factory,
                           QueryEvaluationContext ctx) {
        JsonNodeCursor cursor=doc.cursor();
        cursor.firstChild();
        ObjectNode root=projectObject(this,
                                      factory,
                                      rootMdNode,
                                      rootMdPath,
                                      cursor,
                                      ctx==null?new QueryEvaluationContext(doc.getRoot()):ctx);
        return new JsonDoc(root);
    }

    private ObjectNode projectObject(Projector projector,
                                     JsonNodeFactory factory,
                                     FieldTreeNode mdContext,
                                     Path contextPath,
                                     JsonNodeCursor cursor,
                                     QueryEvaluationContext ctx) {
        ObjectNode ret=factory.objectNode();
        do {
            Path fieldPath=cursor.getCurrentPath();
            // The context path *is* a prefix of the field path 
            Path contextRelativePath=contextPath.isEmpty()?fieldPath:fieldPath.suffix(-contextPath.numSegments());
            JsonNode fieldNode=cursor.getCurrentNode();
            logger.debug("projectObject context={} fieldPath={} contextRelativePath={}",contextPath,fieldPath,contextRelativePath);
            FieldTreeNode fieldMd=mdContext.resolve(contextRelativePath);
            if(fieldMd!=null) {
                logger.debug("Projecting {} in context {}",contextRelativePath,contextPath);
                Boolean result=projector.project(fieldPath,ctx);
                if(result!=null) {
                    if(result) {
                        logger.debug("Projection includes {}",fieldPath);
                        if(fieldMd instanceof ObjectField) {
                            if(fieldNode instanceof ObjectNode) {
                                if(cursor.firstChild()) {
                                    ObjectNode newNode=projectObject(projector,factory,mdContext,contextPath,cursor,ctx);
                                    ret.set(fieldPath.tail(0),newNode);
                                    cursor.parent();
                                } else {
                                    ret.set(fieldPath.tail(0),factory.objectNode());
                                }
                            } else {
                                logger.warn("Expecting object node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                            }
                        } else if(fieldMd instanceof SimpleField) {
                            if(fieldNode.isValueNode()) {
                                ret.set(fieldPath.tail(0),fieldNode);
                            } else {
                                logger.warn("Expecting value node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                            }
                        } else if(fieldMd instanceof ArrayField) {
                            if(fieldNode instanceof ArrayNode) {
                                ArrayNode newNode=factory.arrayNode();
                                ret.set(fieldPath.tail(0),newNode);
                                if(cursor.firstChild()) {
                                    do {
                                        JsonNode node=projectArrayElement(projector,
                                                                          factory,
                                                                          ((ArrayField)fieldMd).getElement(),
                                                                          fieldPath,
                                                                          cursor,
                                                                          ctx);
                                        if(node!=null) {
                                            newNode.add(node);
                                        }
                                    } while(cursor.nextSibling());
                                    cursor.parent();
                                }
                            } else {
                                logger.warn("Expecting array node, found {} for {}",fieldNode.getClass().getName(),fieldPath);
                            }
                        }
                    } else {
                        logger.debug("Projection excludes {}",fieldPath);
                    }
                } else {
                    logger.debug("No projection match for {}",fieldPath);
                }
            } else {
                logger.warn("Unknown field {}",fieldPath);
            }
        } while(cursor.nextSibling());
        return ret;
    }

    private JsonNode projectArrayElement(Projector projector,
                                         JsonNodeFactory factory,
                                         ArrayElement mdContext,
                                         Path contextPath,
                                         JsonNodeCursor cursor,
                                         QueryEvaluationContext ctx) {
        Path elemPath=cursor.getCurrentPath();
        logger.debug("Project array element {}  context {}",elemPath,contextPath);
        Boolean result=projector.project(elemPath,ctx);
        if(result!=null) {
            if(result) {
                Projector nestedProjector=projector.getNestedProjector();
                if(nestedProjector==null) {
                    nestedProjector=projector;
                }
                logger.debug("Projection includes {}",elemPath);
                if(mdContext instanceof SimpleArrayElement) {            
                    return cursor.getCurrentNode();
                } else {
                    if(cursor.firstChild()) {
                        // Object array element
                        JsonNode ret=projectObject(nestedProjector,factory,mdContext,elemPath,cursor,ctx);
                        cursor.parent();
                        return ret;
                    } else {
                        return factory.objectNode();
                    }
                }
            } else {
                logger.debug("Projection excludes {}",elemPath);
            }
        } else {
            logger.debug("No projection match for {}",elemPath);
        }
        return null;
    }
}
