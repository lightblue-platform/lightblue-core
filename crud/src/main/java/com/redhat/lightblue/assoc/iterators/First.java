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

import com.redhat.lightblue.assoc.QueryPlanIterator;
import com.redhat.lightblue.assoc.QueryPlan;

/**
 * Only iterates the first query plan
 */
public class First implements QueryPlanIterator, Serializable {

    private static final long serialVersionUID=1l;

    private QueryPlan qp;

    /**
     * Construct a query plan iterator that operates on the given
     * query plan
     */
    @Override
    public void reset(QueryPlan qp) {
        this.qp=qp;
    }

    @Override
    public boolean next() {
        return false;
    }
}

