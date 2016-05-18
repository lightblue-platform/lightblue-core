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

/**
 * Instances of this class is used to associate additional data to query plan
 * nodes and edges. The actual implementation of QueryPlanData is determined by
 * the caller based on the iterator and scorer used. It is expected that
 * particular iterator and scorers will need different types of data associated
 * with nodes to process the query plan.
 *
 * Subclasses should override the newInstance and copyFrom methods
 */
public class QueryPlanData implements Serializable {

    private static final long serialVersionUID = 1l;

    private List<Conjunct> conjuncts = new ArrayList<>();

    /**
     * The query clauses associated with this node/edge
     */
    public List<Conjunct> getConjuncts() {
        return conjuncts;
    }

    /**
     * The query clauses associated with this node/edge
     */
    public void setConjuncts(List<Conjunct> l) {
        conjuncts = l;
    }

    /**
     * Copies contents of <code>source</code> into this. Subclasses should
     * override this
     */
    public void copyFrom(QueryPlanData source) {
        if (source.conjuncts != null) {
            conjuncts = new ArrayList<>(source.conjuncts);
        } else {
            conjuncts = null;
        }
    }

    /**
     * Creates a new instance. Subclasses should override this
     */
    public QueryPlanData newInstance() {
        return new QueryPlanData();
    }

    @Override
    public String toString() {
        return "Conjuncts:" + conjuncts;
    }
}
