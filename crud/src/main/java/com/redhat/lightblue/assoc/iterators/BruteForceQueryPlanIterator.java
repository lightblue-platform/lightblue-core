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
package com.redhat.lightblue.assoc.iterators;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.assoc.QueryPlanIterator;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;

/**
 * Iterates over possible query plans by rearranging the query plan graph.
 *
 * The given query plan is taken as an initial state. Every successive
 * call to <code>next</code> will modify the query plan into a unique
 * tree. Once all possible query plans are iterated, <code>next</code>
 * returns false.
 */
public class BruteForceQueryPlanIterator implements QueryPlanIterator, Serializable {

    private static final long serialVersionUID=1l;

    private QueryPlan qp;

    private Edge[] edges;

    private final class Edge {
        private final QueryPlanNode n1;
        private final QueryPlanNode n2;
        private boolean v = false;

        public Edge(QueryPlanNode n1,
                    QueryPlanNode n2) {
            this.n1=n1;
            this.n2=n2;
        }

        public void flip() {
            v=!v;
            qp.flip(n1,n2);
        }
    }

    private void findEdges(List<Edge> l,QueryPlanNode from) {
        QueryPlanNode[] dests=from.getDestinations();
        for(QueryPlanNode to:dests) {
            l.add(new Edge(from,to));
            findEdges(l,to);
        }
    }

    /**
     * Construct a query plan iterator that operates on the given
     * query plan
     */
    @Override
    public void reset(QueryPlan qp) {
        this.qp=qp;
        List<Edge> edgeList=new ArrayList<>(16);
        QueryPlanNode[] sources=qp.getSources();
        for(QueryPlanNode x:sources)
            findEdges(edgeList,x);
        edges=edgeList.toArray(new Edge[edgeList.size()]);
    }

    /**
     * Modifies the query plan into a unique tree.
     *
     * @return If true, the query plan is configured into a unique
     * tree. If false, query plan is now returned back to its original
     * state during iterator construction, and the iteration is
     * expected to stop.
     */
    @Override
    public boolean next() {
        int i;
        for(i=edges.length-1;i>=0;i--) {
            edges[i].flip();
            if(edges[i].v)
                break;
        }
        return i>=0;
    }
    
    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder();
        for(Edge e:edges)
            bld.append(e.v?'0':'1');
        return bld.toString();
    }
}

