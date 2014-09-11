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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.Path;

/**
 * Represents a query plan. A query plan is constructed from composite
 * metadata. Starting from the root entity, a QueryPlanNode is created
 * in the query plan for each entity in the composite metadata. Query
 * plan is initially constructed using the same tree structure as the
 * metadata, but query plan offers <code>flip</code> operation to
 * change the direction of an edge in the tree. During query plan
 * determination, many query plans are evaluated, and an efficient one
 * is selected, so this implementation of QueryPlan is optimized for
 * quickly evaluating <code>flip<code> operations, and quickly
 * creating a clone of the query plan.
 *
 * The internal representation is a connection matrix between the
 * nodes. These are the internal fields:
 * <ul>
 * <li>nodes: Query plan nodes in an array. Each node contains a 
 *  nodeIndex such that nodes[i].nodeIndex=i </li>
 * <li>connMx: Connection matrix. If connMx[from][to]=true, then there 
 * is an edge from nodes[from] to nodes[to]</li>
 * <li>fromN: fromN[i]=number of edges emanating from nodes[i]</li>
 * <li>toN: toN[i]=number of edges entering into nodes[i]</li>
 * </ul>
 */
public class QueryPlan implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryPlan.class);
    
    private final QueryPlanNodeImpl[] nodes;

    /**
     * Connection matrix. connMx[i][j] is true if there is an edge from i to j
     */
    private final boolean[][] connMx;
    private final int[] fromN;
    private final int[] toN;

    private class QueryPlanNodeImpl extends QueryPlanNode {

        private final int nodeIndex;
        private String name;

        public QueryPlanNodeImpl(CompositeMetadata md,int index) {
            super(md);
            this.nodeIndex=index;
        }

        public QueryPlanNodeImpl(QueryPlanNodeImpl source) {
            super(source);
            nodeIndex=source.nodeIndex;
        }

        public QueryPlanNode[] getSources() {
            QueryPlanNode[] sources=new QueryPlanNode[toN[nodeIndex]];
            if(sources.length>0) {
                int k=0;
                for(int i=0;i<connMx.length;i++) {
                    if(connMx[i][nodeIndex]) {
                        sources[k++]=nodes[i];
                    }
                }
            }
            return sources;
        }

        public QueryPlanNode[] getDestinations() {
            QueryPlanNode[] dests=new QueryPlanNode[fromN[nodeIndex]];
            if(dests.length>0) {
                int k=0;
                for(int i=0;i<connMx.length;i++) {
                    if(connMx[nodeIndex][i]) {
                        dests[k++]=nodes[i];
                    }
                }
            }
            return dests;
        }

        public String getName() {
            if(name==null) {
                StringBuilder bld=new StringBuilder();
                bld.append(getMetadata().getName()).append('_').append(nodeIndex);
                name=bld.toString();
            }
            return name;
        }
    }

    private static final class Edge {
        private final int from;
        private final int to;

        public Edge(int from,int to) {
            this.from=from;
            this.to=to;
        }
    }


    /**
     * Constructs a query plan from the composite metadata by
     * recursively descending through the associated entities, and
     * creating a node for every entity.
     */
    public QueryPlan(CompositeMetadata root) {
        LOGGER.debug("Constructing query plan for {}",root.getName());
        List<CompositeMetadata> md=new ArrayList<>(16);
        List<Edge> edges=new ArrayList<>(16);
        traverseInit(md,root,edges);
        nodes=new QueryPlanNodeImpl[md.size()];
        int i=0;
        for(CompositeMetadata m:md) {
            nodes[i]=new QueryPlanNodeImpl(m,i);
            i++;
        }
        connMx=new boolean[nodes.length][];
        for(i=0;i<connMx.length;i++)
            connMx[i]=new boolean[nodes.length];
        fromN=new int[nodes.length];
        toN=new int[nodes.length];
        for(Edge x:edges)
            connect(x.from,x.to);
    }

    /**
     * Returns the size (number of nodes) of the query plan
     */
    public int getSize() {
        return nodes.length;
    }

    private void traverseInit(List<CompositeMetadata> md,CompositeMetadata root,List<Edge> edges) {
        LOGGER.debug("Traverse {}",root.getName());
        int from=md.size();
        md.add(root);
        Set<Path> children=root.getChildNames();
        LOGGER.debug("Children:{}",children);
        for(Path p:children) {
            CompositeMetadata child=root.getChild(p);
            edges.add(new Edge(from,md.size()));
            traverseInit(md,child,edges);
        }
    }


    private QueryPlan(QueryPlan source) {
        connMx=source.connMx.clone();
        fromN=source.fromN.clone();
        toN=source.toN.clone();
        nodes=new QueryPlanNodeImpl[source.nodes.length];
        for(int i=0;i<nodes.length;i++)
            nodes[i]=new QueryPlanNodeImpl(source.nodes[i]);
    }

    private QueryPlan(QueryPlanNodeImpl[] nodes) {
        this.nodes=nodes;
        connMx=new boolean[nodes.length][];
        for(int i=0;i<nodes.length;i++)
            connMx[i]=new boolean[nodes.length];
        fromN=new int[nodes.length];
        toN=new int[nodes.length];
    }

    /**
     * Returns an array of source nodes, nodes with no incoming
     * edges. This can never return null, or an empty array. Worst
     * case, it will return the root entity.
     */
    public QueryPlanNode[] getSources() {
        // Source nodes are those with no incoming edges.
        int n=0;
        for(int x=0;x<toN.length;x++)
            if(toN[x]==0)
                n++;
        QueryPlanNode[] sources=new QueryPlanNode[n];
        int k=0;
        for(int x=0;x<toN.length;x++)
            if(toN[x]==0)
                sources[k++]=nodes[x];
        return sources;
    }

    /**
     * Returns a deep copy of the query plan. 
     */
    public QueryPlan deepCopy() {
        return new QueryPlan(this);
    }

    /**
     * Flips the direction of a node.
     */
    public void flip(QueryPlanNode x,
                     QueryPlanNode y) {
        if(isOwned(x)&&isOwned(y)) {
            int ix1=((QueryPlanNodeImpl)x).nodeIndex;
            int ix2=((QueryPlanNodeImpl)y).nodeIndex;
            if(connMx[ix1][ix2])
                flip(ix1,ix2);
            else
                flip(ix2,ix1);
        } else
            throw new IllegalArgumentException();
    }
    
    /**
     * Connects two nodes
     */
    public void connect(QueryPlanNode from,
                        QueryPlanNode to) {
        if(isOwned(from)&&isOwned(to)) {
            connect((QueryPlanNodeImpl)from,
                    (QueryPlanNodeImpl)to);
        } else
            throw new IllegalArgumentException();
    }

    public String mxToString() {
        StringBuilder bld=new StringBuilder(128);
        for(QueryPlanNodeImpl n:nodes) {
            bld.append(n.getName()).append(" ");
        }
        bld.append('\n');
        for(int i=0;i<connMx.length;i++) {
            for(int j=0;j<connMx.length;j++) {
                bld.append(connMx[i][j]?'1':'0').append(' ');
            }
            bld.append(':').append(fromN[i]).append('\n');
        }
        for(int i=0;i<toN.length;i++) {
            bld.append(toN[i]).append(' ');
        }
        return bld.toString();
    }

    public String treeToString() {
        return treeToString(new StringBuilder(128)).toString();
    }

    public StringBuilder treeToString(StringBuilder bld) {
        if(nodes.length==1)
            bld.append(nodes[0].getName());
        else {
            for(QueryPlanNode node:getSources())
                treeToString(node,bld);
        }
        return bld;
    }

    private void connect(QueryPlanNodeImpl from,
                         QueryPlanNodeImpl to) {
        connect(from.nodeIndex,to.nodeIndex);
    }

    private void treeToString(QueryPlanNode start, StringBuilder bld) {
        for(QueryPlanNode node:start.getDestinations()) {
            bld.append(start.getName()).append(" -> ").
                append(node.getName()).append('\n');
            treeToString(node,bld);
        }
    }


    private boolean isOwned(QueryPlanNode node) {
        for(QueryPlanNodeImpl x:nodes)
            if(node==x)
                return true;
        return false;
    }
    
    private void connect(int from,int to) {
        if(!connMx[from][to]) {
            connMx[from][to]=true;
            fromN[from]++;
            toN[to]++;
        }
    }
    
    private void flip(int from,int to) {
        if(connMx[from][to]) {
            connMx[from][to]=false;
            connMx[to][from]=true;
            fromN[from]--;
            fromN[to]++;
            toN[to]--;
            toN[from]++;
        }
    }
    
}
