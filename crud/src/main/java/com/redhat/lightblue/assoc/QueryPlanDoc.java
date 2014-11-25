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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.JsonDoc;

/**
 * Represents a document retrieved during query plan execution. The
 * document is for a single entity, and is linked to its parent and
 * its child documents.
 */
public class QueryPlanDoc implements Serializable {

    private static final long serialVersionUID=1l;
        
    private final JsonDoc doc;
    private final DocId id;
    private final QueryPlanNode node;

    // Parent documents of this doc. Multiple parents are
    // possible, one parent document for each incoming node
    private final Map<QueryPlanNode,QueryPlanDoc> parents=new HashMap<>();
    
    // Child documents of this doc.
    private final Map<QueryPlanNode,List<QueryPlanDoc>> children=new HashMap<>();

    /**
     * Constructs the document with the given parameters
     *
     * @param doc The document
     * @param id Unique ID of the document
     * @param node The query plan node for which this document is retrieved
     */
    public QueryPlanDoc(JsonDoc doc,
                        DocId id,
                        QueryPlanNode node) {
        this.doc=doc;
        this.id=id;
        this.node=node;
    }

    /**
     * Adds children to this node
     *
     * @param node The query plan node for which the children were retrieved
     * @param list The child documents
     *
     * The implementation modifies the child documents by setting their parents to this
     */
    public void addChildren(QueryPlanNode node,List<QueryPlanDoc> list) {
        List<QueryPlanDoc> clist=children.get(node);
        if(clist==null) {
            children.put(node,list);
        } else {
            clist.addAll(list);
        }
        for(QueryPlanDoc c:list)
            c.parents.put(node,this);
    }
    
    /**
     * Adds a child document to this node
     *
     * @param node The query plan node for which the child document were retrieved
     * @param list The child document
     *
     * The implementation modifies the child document by setting their parents to this
     */
    public void addChild(QueryPlanNode node,QueryPlanDoc doc) {
        List<QueryPlanDoc> clist=children.get(node);
        if(clist==null) {
            children.put(node,clist=new ArrayList<>());
        } 
        clist.add(doc);
        doc.parents.put(node,this);
    }
    
    /**
     * Returns the children of this document for the given query plan node
     */
    public List<QueryPlanDoc> getChildren(QueryPlanNode node) {
        return children.get(node);
    }

    /**
     * Returns the parent of this document for the given query plan node
     */
    public QueryPlanDoc getParent(QueryPlanNode node) {
        return parents.get(node);
    }

    /**
     * Returns all parents of the document
     */
    public Collection<QueryPlanDoc> getParents() {
        return parents.values();
    }

    /**
     * Returns the json document
     */
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

    /**
     * Returns the document id
     */
    public DocId getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
