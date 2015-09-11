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
package com.redhat.lightblue.assoc;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;

/**
 * A result document of a particular entity type. This contains the
 * document, the unique id, the query plan node corresponding to this
 * document, and the references to other documents. During
 * construction, all reference instances are collected, and a
 * DocReference list si constructed using those references. Subsequent
 * stages of execution attach new documents to those references.
 */
public class ResultDoc implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(ResultDoc.class);
        
    private final JsonDoc doc;
    private final DocId id;
    private final QueryPlanNode node;

    private final Map<QueryPlanNode,List<ChildDocReference>> children=new HashMap<>();
    private final Map<QueryPlanNode,ChildDocReference> parents=new HashMap<>();

    public ResultDoc(JsonDoc doc,
                     DocId id,
                     QueryPlanNode node) {
        this.node=node;
        this.doc=doc;
        this.id=id;
        gatherChildren();
    }

    public DocId getId() {
        return id;
    }

    public JsonDoc getDoc() {
        return doc;
    }

    /**
     * Returns the query plan node for this document
     */
    public QueryPlanNode getQueryPlanNode() {
        return node;
    }

    /**
     * Returns the metadata for this document
     */
    public CompositeMetadata getMetadata() {
        return node.getMetadata();
    }

    public List<ChildDocReference> getChildren(QueryPlanNode dest) {
        return children.get(dest);
    }

    public Map<QueryPlanNode,List<ChildDocReference>> getChildren() {
        return children;
    }

    /**
     * Returns all child references to a given node for all the docs in the list
     */
    public static List<ChildDocReference> getChildren(List<ResultDoc> docs,QueryPlanNode dest) {
        List<ChildDocReference> ret=new ArrayList<>();
        for(ResultDoc x:docs) {
            List<ChildDocReference> l=x.getChildren(dest);
            if(l!=null)
                ret.addAll(l);
        }
        return ret;
    }

    public Map<QueryPlanNode,ChildDocReference> getParentDocs() {
        return parents;
    }

    /**
     * Sets the parent document reference of this document that is coming from parentNode
     */
    public void setParentDoc(QueryPlanNode parentNode,ChildDocReference ref) {
        parents.put(parentNode,ref);
    }


    /**
     * Iterates through all destination nodes in query plan, finds all
     * references corresponding to those destination nodes, and
     * initializes a reference for each
     */
    private void gatherChildren() {
        QueryPlanNode[] destinations=node.getDestinations();
        CompositeMetadata md=node.getMetadata();
        if(destinations.length>0) {
            for(QueryPlanNode destNode:destinations) {
                LOGGER.debug("Gathering children for {}",destNode);
                CompositeMetadata destMd=destNode.getMetadata();
                List<ChildDocReference> refList=new ArrayList<>();
                // Find the entity reference for destMd. It can be a child of this entity, or the parent
                if(destMd==node.getMetadata().getParent()) {
                    // This happens when entity graph is A->B, but retrieval graph is B->A
                    // This works only if A -> B link is not an array
                    if(md.getEntityPath().nAnys()>0)
                        throw new IllegalArgumentException("Unsupported association");

                    refList.add(new ChildDocReference(this,Path.EMPTY));
                    children.put(destNode,refList);
                } else {
                    // Get the resolved reference for this field
                    ResolvedReferenceField ref=md.getChildReference(destMd.getEntityPath());
                    LOGGER.debug("Resolved reference for {}:{}",destMd.getEntityPath(),ref);
                    // We cut the last segment, it is "ref"
                    Path entityRelativeFieldName=md.getEntityRelativeFieldName(ref);
                    Path field=entityRelativeFieldName.prefix(-1);
                    LOGGER.debug("Getting instances of {} (reference was {})",field,entityRelativeFieldName);
                    Path p=entityRelativeFieldName.prefix(-1);
                    // p points to the object containing the reference field
                    // It could be empty, if the reference is at the root level
                    if(p.isEmpty()) {
                        refList.add(new ChildDocReference(this,new Path(p,new Path(ref.getName()))));
                    } else {
                        LOGGER.debug("Iterating {} in {}",p,doc);
                        KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(p);
                        while(cursor.hasNext()) {
                            cursor.next();
                            JsonNode nodeWithRef=cursor.getCurrentValue();
                            LOGGER.debug("Checking field {}:{}",cursor.getCurrentKey(),nodeWithRef);
                            if(nodeWithRef instanceof ArrayNode) {
                                int size=((ArrayNode)nodeWithRef).size();
                                MutablePath elem=cursor.getCurrentKey().mutableCopy();
                                int ix=elem.numSegments();
                                elem.push(0);
                                elem.push(ref.getName());
                                for(int i=0;i<size;i++) {
                                    elem.set(ix,i);
                                    refList.add(new ChildDocReference(this,elem.immutableCopy()));
                                }
                            } else {
                                refList.add(new ChildDocReference(this,new Path(cursor.getCurrentKey(),new Path(ref.getName()))));
                            }
                        }
                    }
                    children.put(destNode,refList);
                }
            }
        }
        LOGGER.debug("children for {}:{}",id,children);
    }

    public String toString() {
        return "Doc:"+id+" children:"+children;
    }
}
