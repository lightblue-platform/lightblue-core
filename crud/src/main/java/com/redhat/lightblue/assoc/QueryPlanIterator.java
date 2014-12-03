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

/**
 * Iterates over possible query plans by rearranging the query plan graph.
 *
 * The given query plan is taken as an initial state. Every successive
 * call to <code>next</code> will modify the query plan into a unique
 * tree. Once all possible query plans are iterated, <code>next</code>
 * returns false.
 */
public interface QueryPlanIterator {

    /**
     * Resets the query plan iterator with the given copy of the query
     * plan. The query iterator must assume that this given query plan
     * is in its initial state, and should iterate through different
     * query plans, modifying the given copy.
     */
    void reset(QueryPlan p);

    /**
     * Modifies the query plan into a unique tree.
     *
     * @return If true, tne query plan is configured into a unique
     * tree. If false, query plan is now returned back to its original
     * state during iterator construction, and the iteration is
     * expected to stop.
     */
    public boolean next();
}

