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
package com.redhat.lightblue.assoc.scorers;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Indexes;
import com.redhat.lightblue.metadata.Index;

import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanScorer;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanChooser;
import com.redhat.lightblue.assoc.ResolvedFieldInfo;

import com.redhat.lightblue.util.Path;

public class IndexedFieldScorer implements QueryPlanScorer, Serializable {

    private static final long serialVersionUID=1l;

    private CompositeMetadata cmd;

    private static class Score implements Comparable {
        // Sorted list of indexed node distances
        private final List<Integer> indexed=new ArrayList<>();
        // Sorted list of unindexed but queried node distances
        private final List<Integer> unindexed=new ArrayList<>();

        public void addIndexedNodeDistance(int dist) {
            indexed.add(dist);
        }

        public  void addUnindexedNodeDistance(int dist) {
            unindexed.add(dist);
        }

        public int numDistances() {
            return indexed.size()+unindexed.size();
        }

        @Override
        public int compareTo(Object t) {
            if(t instanceof Score) {
                // If this has closer indexes than t, this is smaller
                int ret=compare(indexed,((Score)t).indexed);
                // Same index set, or no indexes. Look at unindexed queries
                if(ret==0)
                    ret=compare(unindexed,((Score)t).unindexed);
                return ret;
            } else
                throw new IllegalArgumentException("Expecting a score, got "+t);
        }

        private int compare(List<Integer> l1,List<Integer> l2) {
            int n1=l1.size();
            int n2=l2.size();
            int min=n1>n2?n1:n2;
            for(int i=0;i<min;i++) {
                int val1=l1.get(i);
                int val2=l2.get(i);
                if(val1<val2)
                    return -1;
                else if(val1>val2)
                    return 1;
            }
            if(n1>n2)
                return 1;
            else if(n1<n2)
                return -1;
            else
                return 0;
        }
    } 

    private static final class MaxScore extends Score {
        @Override
        public int compareTo(Object value) {
            return (value instanceof MaxScore)?0:1;
        }
    }

    public static final Comparable MAX=new MaxScore();


    @Override
    public QueryPlanData newDataInstance() {
        return new IndexedFieldScorerData();
    }

    @Override
    public Comparable score(QueryPlan qp) {
        Score score=new Score();
        // Scoring is done by the distance of index using queries to
        // the source nodes If there are no index using queries, then
        // distance of other node queries are used If those don't
        // exist, returns QueryPlanScorer.MAX
        QueryPlanNode[] nodes=qp.getSources();
        for(QueryPlanNode node:nodes) {
            addNode(score,node,0);
        }
        return score.numDistances()==0?MAX:score;
    }

    private void addNode(Score score,QueryPlanNode node,int distance) {
        IndexedFieldScorerData data=(IndexedFieldScorerData)node.getData();
        if(data.getIndexMap().isEmpty()) {
            // No useful indexes in this node
            // Any queries?
            if(!data.getConjuncts().isEmpty()) {
                score.addUnindexedNodeDistance(distance);
            }
        } else {
            score.addIndexedNodeDistance(distance);
        }
        QueryPlanNode[] nodes=node.getDestinations();
        for(QueryPlanNode x:nodes)
            addNode(score,x,distance+1);
    }

    @Override
    public void reset(QueryPlanChooser c) {
        cmd=c.getMetadata();
        // Conjuncts associated with nodes will not move from one node to another
        // So we can measure the cost associated with them from the start
        for(QueryPlanNode node:c.getQueryPlan().getAllNodes()) {
            IndexedFieldScorerData data=(IndexedFieldScorerData)node.getData();
            Set<Path> indexableFields=new HashSet<>();
            Indexes indexes=null;
            for(Conjunct cj:data.getConjuncts()) {
                List<ResolvedFieldInfo> cjFields=cj.getFieldInfo();
                // If conjunct has one field, index can be used to retrieve it
                if(cjFields.size()==1) {
                    indexableFields.add(cjFields.get(0).getEntityRelativeFieldName());
                    indexes=cjFields.get(0).getFieldEntityMetadata().getEntityInfo().getIndexes();
                }
            }
            data.setIndexableFields(indexableFields);
            if(indexes!=null) 
                data.setIndexMap(indexes.getUsefulIndexes(indexableFields));
            else
                data.setIndexMap(new HashMap<Index,Set<Path>>());
        }
    }

}
