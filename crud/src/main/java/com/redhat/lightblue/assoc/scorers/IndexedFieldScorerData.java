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

import java.util.Set;
import java.util.Map;

import com.redhat.lightblue.metadata.Index;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.assoc.QueryPlanData;

public class IndexedFieldScorerData extends QueryPlanData {

    private Set<Path> indexableFields;
    private Map<Index,Set<Path>> indexMap;

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
    public String toString() {
        return super.toString()+" indexableFields:"+indexableFields+" indexMap:"+indexMap;
    }
}
