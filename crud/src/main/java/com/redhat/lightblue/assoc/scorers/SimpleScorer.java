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

import com.redhat.lightblue.assoc.QueryPlanScorer;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanChooser;

/**
 * Simple scorer that always returns MAX
 */
public class SimpleScorer implements QueryPlanScorer, Serializable {

    private static final long serialVersionUID=1l;

    private static final class MaxScore implements Comparable {
        @Override
        public int compareTo(Object value) {
            return (value instanceof MaxScore)?0:1;
        }

        @Override
        public boolean equals(Object x) {
            return compareTo(x)==0;
        }
    }

    public static final Comparable MAX=new MaxScore();


    @Override
    public QueryPlanData newDataInstance() {
        return new QueryPlanData();
    }

    @Override
    public Comparable score(QueryPlan qp) {
        return MAX;
    }

    @Override
    public void reset(QueryPlanChooser c) {
    }

}
