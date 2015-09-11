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
import java.util.Map;
import java.util.HashMap;

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

    private final ConnMx mx;

    private final List<Conjunct> unassignedClauses=new ArrayList<>();
    private final Map<Integer,QueryPlanData> edgeData=new HashMap<>();

    private final QueryPlanScorer qdf;

    private class QueryPlanNodeImpl extends QueryPlanNode {

        private final int nodeIndex;
        private String name;

        public QueryPlanNodeImpl(CompositeMetadata md,QueryPlanData data,int index) {
            super(md,data);
            this.nodeIndex=index;
        }

        public QueryPlanNodeImpl(QueryPlanNodeImpl source) {
            super(source);
            nodeIndex=source.nodeIndex;
        }

        public QueryPlanNode[] getSources() {
            return map(mx.getSources(nodeIndex));
        }

        public QueryPlanNode[] getDestinations() {
            return map(mx.getDestinations(nodeIndex));
        }

        public String getName() {
            if(name==null) {
                name = md.getName() + '_' + nodeIndex;
            }
            return name;
        }

        public String toString() {
            return getName();
        }
    }

    private static final class Edge {
        private final int from;
        private final int to;

        public Edge(int from,int to) {
            this.from=from;
            this.to=to;
        }

        @Override
        public String toString() {
            return from+"->"+to;
        }
    }


    private Integer getEdgeId(int ix1,int ix2) {
        if(ix1<ix2)
            return (ix1*nodes.length)+ix2;
        else
            return (ix2*nodes.length)+ix1;
    }

    private QueryPlanData getEdgeData(int ix1,int ix2) {
        return edgeData.get(getEdgeId(ix1,ix2));
    }

    private void setEdgeData(int ix1,int ix2,QueryPlanData data) {
        edgeData.put(getEdgeId(ix1,ix2),data);
    }


    /**
     * Constructs a query plan from the composite metadata by
     * recursively descending through the associated entities, and
     * creating a node for every entity.
     */
    public QueryPlan(CompositeMetadata root,
                     QueryPlanScorer qdf) {
        this(root,qdf,null);
    }

    /**
     * Constructs a query plan from the composite metadata by
     * recursively descending through the associated entities as
     * deterimed by the filter, and creating a node for every entity.
     *
     * @param root The root composite metadata
     * @param qdf The scorer
     * @param filter A set of composite metadata objects containing
     * only those entities that should be included in the plan. If
     * null, all entities will be included.
     */
    public QueryPlan(CompositeMetadata root,
                     QueryPlanScorer qdf,
                     Set<CompositeMetadata> filter) {
        this.qdf=qdf;
        LOGGER.debug("Constructing query plan for {}",root.getName());
        List<CompositeMetadata> md=new ArrayList<>(16);
        List<Edge> edges=new ArrayList<>(16);
        traverseInit(md,root,edges,filter);
        LOGGER.debug("edges:{}",edges);
        nodes=new QueryPlanNodeImpl[filter==null?md.size():filter.size()];
        int i=0;
        for(CompositeMetadata m:md) {
            nodes[i]=new QueryPlanNodeImpl(m,qdf.newDataInstance(),i);
            i++;
        }
        mx=new ConnMx(nodes.length);
        for(Edge x:edges)
            mx.connect(x.from,x.to);
        LOGGER.debug("constructed plan:{}",this);
    }

    /**
     * Returns the size (number of nodes) of the query plan
     */
    public int getSize() {
        return nodes.length;
    }

    private void traverseInit(List<CompositeMetadata> md,
                              CompositeMetadata root,
                              List<Edge> edges,
                              Set<CompositeMetadata> filter) {
        LOGGER.debug("Traverse {}",root.getName());
        int from=md.size();
        if(filter==null||filter.contains(root)) {
            md.add(root);
            Set<Path> children=root.getChildPaths();
            LOGGER.debug("Children:{}",children);
            for(Path p:children) {
                CompositeMetadata child=root.getChildMetadata(p);
                if(filter==null||filter.contains(child)) {
                    edges.add(new Edge(from,md.size()));
                    traverseInit(md,child,edges,filter);
                }
            }
        }
    }

    /**
     * Copy constructor.
     *
     * @param source
     */
    private QueryPlan(QueryPlan source) {
        qdf=source.qdf;
        mx=new ConnMx(source.mx);
        nodes=new QueryPlanNodeImpl[source.nodes.length];
        for(int i=0;i<nodes.length;i++)
            nodes[i]=new QueryPlanNodeImpl(source.nodes[i]);
        for(Map.Entry<Integer,QueryPlanData> entry:source.edgeData.entrySet()) {
            QueryPlanData data;
            if(entry.getValue()!=null) {
                data=entry.getValue().newInstance();
                data.copyFrom(entry.getValue());
            } else
                data=null;
            edgeData.put(entry.getKey(),data);
        }
        unassignedClauses.addAll(source.unassignedClauses);
    }

    /**
     * Creates a new instance of QueryPlanData
     */
    public QueryPlanData newData() {
        return qdf.newDataInstance();
    }

    /**
     * Returns an array of source nodes, nodes with no incoming
     * edges. This can never return null, or an empty array. Worst
     * case, it will return the root entity.
     */
    public QueryPlanNode[] getSources() {
        return map(mx.getSources());
    }

    /**
     * Returns the list containing clauses that cannot be associated
     * with a node or an edge (i.e. clauses refer to more than two
     * nodes).
     */
    public List<Conjunct> getUnassignedClauses() {
        return unassignedClauses;
    }

    /**
     * Returns the list of conjuncts associated with the undirected edge between the two nodes
     */
    public QueryPlanData getEdgeData(QueryPlanNode x,
                                     QueryPlanNode y) {
        if(isOwned(x)&&isOwned(y)) 
            return getEdgeData(((QueryPlanNodeImpl)x).nodeIndex,
                               ((QueryPlanNodeImpl)y).nodeIndex);
        else
            throw new IllegalArgumentException();
    }

    /**
     * Sets the list of conjuncts associated with the undirected edge between the two nodes
     */
    public void setEdgeData(QueryPlanNode x,
                            QueryPlanNode y,
                            QueryPlanData d) {
        if(isOwned(x)&&isOwned(y))
            setEdgeData( ((QueryPlanNodeImpl)x).nodeIndex,
                         ((QueryPlanNodeImpl)y).nodeIndex,
                         d);
        else
            throw new IllegalArgumentException();
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
            mx.flip( ((QueryPlanNodeImpl)x).nodeIndex,((QueryPlanNodeImpl)y).nodeIndex);
        } else
            throw new IllegalArgumentException();
    }
    
    /**
     * Connects two nodes
     */
    public void connect(QueryPlanNode from,
                        QueryPlanNode to) {
        if(isOwned(from)&&isOwned(to)) {
            mx.connect( ((QueryPlanNodeImpl)from).nodeIndex,
                        ((QueryPlanNodeImpl)to).nodeIndex);
        } else
            throw new IllegalArgumentException();
    }

    /**
     * Returns all nodes
     */
    public QueryPlanNode[] getAllNodes() {
        return nodes;
    }

    /**
     * Returns if there exists a directed edge between the nodes,
     * directed from <code>from</code> to <code>to</code>
     */
    public boolean isDirectedConnected(QueryPlanNode from,
                                       QueryPlanNode to) {
        if(isOwned(from)&&isOwned(to)) {
            return mx.isDirectedConnected( ((QueryPlanNodeImpl)from).nodeIndex, ((QueryPlanNodeImpl)to).nodeIndex );
        }
        return false;
    }

    /**
     * Returns if there exists an edge between the two nodes, pointing either way
     */
    public boolean isUndirectedConnected(QueryPlanNode from,
                                         QueryPlanNode to) {
        if(isOwned(from)&&isOwned(to)) {
            return mx.isUndirectedConnected( ((QueryPlanNodeImpl)from).nodeIndex,((QueryPlanNodeImpl)to).nodeIndex);
        }
        return false;
    }

    public QueryPlanNode[] getBreadthFirstNodeOrdering() {
        QueryPlanNode[] ret=new QueryPlanNode[nodes.length];
        int k=0;
        for(QueryPlanNode x:getSources())
            ret[k++]=x;
        while(k<ret.length) {
            for(int i=k-1;i>=0;i--) {
                QueryPlanNode[] dests=ret[i].getDestinations();
                for(QueryPlanNode x:dests)
                    k=addBreadthFirstNode(ret,k,x);
            }
        }
        return ret;
    }

    private int addBreadthFirstNode(QueryPlanNode[] arr,int n,QueryPlanNode node) {
        // Already in array?
        for(int i=0;i<n;i++)
            if(arr[i]==node)
                return n;
        // Make sure all sources are in the array
        QueryPlanNode[] sources=node.getSources();
        for(QueryPlanNode x:sources)
            n=addBreadthFirstNode(arr,n,x);
        // Add it to the array
        arr[n++]=node;
        return n;
    }

    public String mxToString() {
        return mx.toString();
    }

    /**
     * Returns the query plan node corresponding to the given
     * composite metadata instance.
     *
     * This checks object identity to find the node containing the
     * composite metadata
     */
    public QueryPlanNode getNode(CompositeMetadata md) {
        for(QueryPlanNode n:nodes)
            if(n.md==md)
                return n;
        return null;
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

    @Override
    public String toString() {
        return mxToString()+"\n"+treeToString();
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
    
    private QueryPlanNode[] map(int[] nodeIx) {
        QueryPlanNode[] ret=new QueryPlanNode[nodeIx.length];
        for(int i=0;i<nodeIx.length;i++)
            ret[i]=nodes[nodeIx[i]];
        return ret;
    }
    
}
