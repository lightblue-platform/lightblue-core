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
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.eval.QueryEvaluator;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;

/**
 * A result document of a particular entity type. This contains the
 * document, the unique id, the query plan node corresponding to this
 * document, and the references to other documents. During
 * construction, all reference instances are collected, and a
 * DocReference list is constructed using those references. Subsequent
 * stages of execution attach new documents to those references.
 */
public class ResultDoc implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(ResultDoc.class);
        
    private final JsonDoc doc;
    private final DocId id;
    private final QueryPlanNode node;

    private final Map<CompositeMetadata,List<DocReference>> children=new HashMap<>();
    private final Map<CompositeMetadata,ChildDocReference> parents=new HashMap<>();

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

    public List<DocReference> getChildren(QueryPlanNode dest) {
        return getChildren(dest.getMetadata());
    }

    public List<DocReference> getChildren(CompositeMetadata destMd) {
        return children.get(destMd);
    }

    public Map<CompositeMetadata,List<DocReference>> getChildren() {
        return children;
    }

    /**
     * Returns all child references to a given node for all the docs in the list
     */
    public static List<DocReference> getChildren(List<ResultDoc> docs,QueryPlanNode dest) {
        List<DocReference> ret=new ArrayList<>();
        for(ResultDoc x:docs) {
            List<DocReference> l=x.getChildren(dest);
            if(l!=null)
                ret.addAll(l);
        }
        return ret;
    }

    public Map<CompositeMetadata,ChildDocReference> getParentDocs() {
        return parents;
    }

    /**
     * Sets the parent document reference of this document that is coming from parentNode
     */
    public void setParentDoc(QueryPlanNode parentNode,ChildDocReference ref) {
        parents.put(parentNode.getMetadata(),ref);
    }


    /**
     * Iterates through all destination nodes in query plan, finds all
     * references corresponding to those destination nodes, and
     * initializes a reference for each
     */
    private void gatherChildren() {
        CompositeMetadata thisMd=node.getMetadata();
        Set<Path> childPaths=thisMd.getChildPaths();
        if(childPaths!=null) {
            for(Path childEntityPath:childPaths) {
                LOGGER.debug("Gathering children for {}",childEntityPath);
                ResolvedReferenceField ref=thisMd.getChildReference(childEntityPath);
                CompositeMetadata destMd=ref.getReferencedMetadata();
                List<DocReference> refList=new ArrayList<>();
                // Get the resolved reference for this field
                LOGGER.debug("Resolved reference for {}:{}",destMd.getEntityPath(),ref);
                // We cut the last segment, it is "ref"
                Path entityRelativeFieldName=thisMd.getEntityRelativeFieldName(ref);
                Path p=entityRelativeFieldName.prefix(-1);
                LOGGER.debug("Getting instances of {} (reference was {})",p,entityRelativeFieldName);
                // p points to the object containing the reference field
                // It could be empty, if the reference is at the root level
                if(p.isEmpty()) {
                    refList.add(new ChildDocReference(this,entityRelativeFieldName));
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
                children.put(destMd,refList);
            }
        }
        LOGGER.debug("children for {}:{}",id,children);
    }

    /**
     * This is used to associate the documents when a child document
     * is searched before the parent document. When this happens, each
     * reference of the parent document is looked up in child doc list
     * using the edge criteria.
     *
     * @param parentDocs Docs that contain the childDocs
     * @param childDocs Docs that are contained in parent docs
     *
     * childDocs must be all from the same query plan node.
     * Parentdocs must be all from the same query plan node.
     * The query plan nodes for child and parent are different, and linked with edgeClauses
     */
    public static void associateDocs(List<ResultDoc> parentDocs,
                                     List<ResultDoc> childDocs,
                                     List<Conjunct> edgeClauses,
                                     CompositeMetadata root) {
        // We have to find a list of childDocs for every reference in a parent doc
        LOGGER.debug("Associating {} parent docs",parentDocs.size());
        if(!childDocs.isEmpty()&&!parentDocs.isEmpty()) {
            QueryPlanNode childDocsNode=childDocs.get(0).node;
            QueryPlanNode parentDocsNode=parentDocs.get(0).node;
            LOGGER.debug("Initializing query processor with parent:{} child:{}",parentDocsNode,childDocsNode);
            ResolvedFieldBinding.BindResult bindingInfo;
            QueryEvaluator qeval;
            if(edgeClauses!=null&&!edgeClauses.isEmpty()) {
                bindingInfo=ResolvedFieldBinding.bind(edgeClauses,childDocsNode,root);
                qeval=QueryEvaluator.getInstance(bindingInfo.getRelativeQuery(),childDocsNode.getMetadata());
            } else {
                bindingInfo=null;
                qeval=QueryEvaluator.MATCH_ALL_EVALUATOR;
            }
            for(ResultDoc parentDoc:parentDocs) {
                LOGGER.debug("Processing parent doc {}",parentDoc.id);
                List<DocReference> references=parentDoc.getChildren(childDocsNode);
                if(bindingInfo!=null) {
                    for(DocReference reference:references) {
                        if(reference instanceof ChildDocReference) {
                            for(ResolvedFieldBinding binding:bindingInfo.getBindings())
                                binding.refresh((ChildDocReference)reference);
                            for(ResultDoc childDoc:childDocs) {
                                if(qeval.evaluate(childDoc.doc).getResult())
                                    ((ChildDocReference)reference).getChildren().add(childDoc);
                            }
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return "Doc:"+id+" children:"+children;
    }
}
