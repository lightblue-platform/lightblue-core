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
import java.util.List;

import com.redhat.lightblue.metadata.Index;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.assoc.QueryPlanData;

public class IndexedFieldScorerData extends QueryPlanData {

    private Index idIndex;
    private List<Path> idFields;
    private Set<Path> indexableFields;
    private Map<Index, Set<Path>> indexMap;
    private boolean rootNode;
    private boolean identitySearch;

    private transient CostAndSize cs = null;

    /**
     * Somewhat arbitrary numbers to estimate the cost involved in retrieving
     * data. A real DB engine would use DB statistics.
     *
     * Cost Size IdentitySearch 1 1 Indexed search 2 5 Nonindexed search 10000 5
     * No criteria 10 10000
     */
    /**
     * Cost of running a query on a indexes id field
     */
    private static final BigInteger COST_ID = new BigInteger("1");

    /**
     * Cost of running a query on an indexed field
     */
    private static final BigInteger COST_INDEXED = new BigInteger("2");

    /**
     * Cost of running a query without an indexed field
     */
    private static final BigInteger COST_UNINDEXED = new BigInteger("10000");

    /*
     * Cost associated with not having a query
     */
    private static final BigInteger COST_NOQ = new BigInteger("10");

    /**
     * Expected size of a result set with identity
     */
    private static final BigInteger SZ_ID = new BigInteger("1");

    /**
     * Expected size of a result set with query
     */
    private static final BigInteger SZ_IX = new BigInteger("5");

    /**
     * Expected size of a result set without a query. We assume the caller is
     * not stupid enough to attempt to retrieve all data of a 1M record DB
     */
    private static final BigInteger SZ_NOQ = new BigInteger("10000");

    public BigInteger estimateNodeCostPerQ() {
        return estimateCost(identitySearch, hasQueries(), hasUsefulIndexes());
    }

    public BigInteger estimateNodeResultSetSizePerQ() {
        if (identitySearch) {
            return SZ_ID;
        } else if (hasQueries()) {
            return SZ_IX;
        } else {
            return SZ_NOQ;
        }
    }

    public static BigInteger estimateCost(boolean identitySearch, boolean hasQueries, boolean hasUsefulIndexes) {
        if (identitySearch) {
            return COST_ID;
        } else if (hasQueries) {
            if (hasUsefulIndexes) {
                return COST_INDEXED;
            } else {
                return COST_UNINDEXED;
            }
        } else {
            return COST_NOQ;
        }
    }

    public CostAndSize getCostAndSize() {
        if (cs == null) {
            cs = new CostAndSize(estimateNodeCostPerQ(), estimateNodeResultSetSizePerQ());
        }
        return cs;
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
        rootNode = v;
    }

    public boolean hasUsefulIndexes() {
        return !indexMap.isEmpty();
    }

    public boolean hasQueries() {
        return !getConjuncts().isEmpty();
    }

    /**
     * Set of fields that are collected from the queries associated to this node
     * that can be used for indexed access. These are the entity relative names
     * of the paths
     */
    public Set<Path> getIndexableFields() {
        return indexableFields;
    }

    /**
     * Set of fields that are collected from the queries associated to this node
     * that can be used for indexed access. These are the entity relative names
     * of the paths
     */
    public void setIndexableFields(Set<Path> f) {
        indexableFields = f;
    }

    /**
     * Index info that shows which collections of fields can be searched by
     * which index
     */
    public Map<Index, Set<Path>> getIndexMap() {
        return indexMap;
    }

    /**
     * Index info that shows which collections of fields can be searched by
     * which index
     */
    public void setIndexMap(Map<Index, Set<Path>> map) {
        indexMap = map;
    }

    /**
     * The index for the entity id
     */
    public Index getIdIndex() {
        return idIndex;
    }

    /**
     * The index for the entity id
     */
    public void setIdIndex(Index i) {
        idIndex = i;
    }

    /**
     * The identities of the entity
     */
    public List<Path> getIdFields() {
        return idFields;
    }

    /**
     * The identities of the entity
     */
    public void setIdFields(List<Path> list) {
        idFields = list;
    }

    /**
     * If there is an identity search on this entity
     */
    public boolean isIdentitySearch() {
        return identitySearch;
    }

    /**
     * If there is an identity search on this entity
     */
    public void setIdentitySearch(boolean b) {
        identitySearch = b;
    }

    @Override
    public QueryPlanData newInstance() {
        return new IndexedFieldScorerData();
    }

    @Override
    public void copyFrom(QueryPlanData source) {
        super.copyFrom(source);
        if (source instanceof IndexedFieldScorerData) {
            IndexedFieldScorerData s = (IndexedFieldScorerData) source;
            idIndex = s.idIndex;
            idFields = s.idFields;
            indexableFields = s.indexableFields;
            indexMap = s.indexMap;
            rootNode = s.rootNode;
            identitySearch = s.identitySearch;
            cs = s.cs;
        }
    }

    @Override
    public String toString() {
        return super.toString() + " indexableFields:" + indexableFields + " indexMap:" + indexMap;
    }
}
