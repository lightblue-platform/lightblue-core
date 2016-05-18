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
 * The implementation should assign a score to the query plan, and return it in
 * a structure that can be compared using Comparable interface methods.
 *
 * It is up to the implementation to decide whether this will be a stateful or
 * stateless implementation.
 */
public interface QueryPlanScorer {

    /**
     * Returns a new instance for an implementation of QueryPlanData used by
     * this scorer
     */
    QueryPlanData newDataInstance();

    /**
     * Initialize the scorer instance before the iteration of possible query
     * plans
     */
    void reset(QueryPlanChooser c);

    /**
     * Returns a score for the query plan
     */
    Comparable score(QueryPlan p);
}
