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

import java.math.BigInteger;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.metadata.Index;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.assoc.QueryPlanData;

public class IndexedFieldScorerData extends QueryPlanData {

    private Set<Path> indexableFields;
    private Map<Index,Set<Path>> indexMap;
    private boolean rootNode;

    /**
     * Somewhat arbitrary numbers to estimate the cost involved in
     * retrieving data. A real DB engine would use DB statistics.
     */

    /**
     * Cost of running a query on an indexed field
     */
    private static final BigInteger COST_INDEXED=new BigInteger("2");

    /**
     * Cost of running a query without an indexed field
     */
    private static final BigInteger COST_UNINDEXED=new BigInteger("20");

    /**
     * Cost of evaluating a query once root is retrieved
     */
    private static final BigInteger COST_POST_ROOT_W_QUERY_FACTOR=new BigInteger("2");

    /*
     * Cost associated with not having a query
     */
    private static final BigInteger COST_NOQ=new BigInteger("1");

    /**
     * Expected size of a result set with a query
     */
    private static final BigInteger SZ_Q=new BigInteger("10");

    /**
     * Expected size of a result set without a query. We assume the
     * caller is not stupid enough to attempt to retrieve all data of
     * a 1M record DB
     */
    private static final BigInteger SZ_NOQ=new BigInteger("100");

    public BigInteger estimatedCost() {
        if(hasQueries()) {
            if(hasUsefulIndexes()) {
                return COST_INDEXED ;
            } else {
                return COST_UNINDEXED;
            }
        } else
            return COST_NOQ;
    }

    /**
     * If there is a query after root, then we have to manually filter records, which is costly
     */
    public BigInteger estimatedRootDescendantCost(BigInteger resultSetSize) {
        if(hasQueries())
            return COST_POST_ROOT_W_QUERY_FACTOR.multiply(resultSetSize);
        else
            return BigInteger.ONE;
    }

    public BigInteger estimatedResultSize() {
        if(hasQueries())
            return SZ_Q;
        else
            return SZ_NOQ;
    }

    /**
     * True if this is the root node
     */
    public boolean isRootNode() {
        return rootNode;
    }

    /**
     * True if this is the root node
     */
    public void setRootNode(boolean v) {
        rootNode=v;
    }

    public boolean hasUsefulIndexes() {
        return !indexMap.isEmpty();
    }

    public boolean hasQueries() {
        return !getConjuncts().isEmpty();
    }

    /**
     * Set of fields that are collected from the queries associated to
     * this node that can be used for indexed access. These are the
     * entity relative names of the paths
     */
    public Set<Path> getIndexableFields() {
        return indexableFields;
    }

    /**
     * Set of fields that are collected from the queries associated to
     * this node that can be used for indexed access. These are the
     * entity relative names of the paths
     */
    public void setIndexableFields(Set<Path> f) {
        indexableFields=f;
    }

    /**
     * Index info that shows which collections of fields can be
     * searched by which index
     */
    public Map<Index,Set<Path>> getIndexMap() {
        return indexMap;
    }

    /**
     * Index info that shows which collections of fields can be
     * searched by which index
     */
    public void setIndexMap(Map<Index,Set<Path>> map) {
        indexMap=map;
    }

    @Override
    public QueryPlanData newInstance() {
        return new IndexedFieldScorerData();
    }

    @Override
    public void copyFrom(QueryPlanData source) {
        super.copyFrom(source);
        if (source instanceof IndexedFieldScorerData) {
            if (((IndexedFieldScorerData) source).indexableFields != null)
                indexableFields = new HashSet<>(((IndexedFieldScorerData) source).indexableFields);
            if (((IndexedFieldScorerData) source).indexMap != null)
                indexMap = new HashMap<>(((IndexedFieldScorerData) source).indexMap);
        }
    }

    @Override
    public String toString() {
        return super.toString()+" indexableFields:"+indexableFields+" indexMap:"+indexMap;
    }
}
