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
package com.redhat.lightblue.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Comparison operators
 */
public enum BinaryComparisonOperator {
    _eq("$eq", "="),
    _neq("$neq", "!="),
    _lt("$lt", "<"),
    _gt("$gt", ">"),
    _lte("$lte", "<="),
    _gte("$gte", ">=");

    private final String[] ops;

    private static final Map<String, BinaryComparisonOperator> MAP;

    static {
        MAP = new HashMap<>();
        _eq.init(MAP);
        _neq.init(MAP);
        _lt.init(MAP);
        _gt.init(MAP);
        _lte.init(MAP);
        _gte.init(MAP);
    }

    private void init(Map<String, BinaryComparisonOperator> map) {
        for (String x : ops) {
            map.put(x, this);
        }
    }

    private BinaryComparisonOperator(String... op) {
        this.ops = op;
    }

    /**
     * Applies the operator
     *
     * @param cmp Comparison result of x and y, wheres <0 denotes x<y, >0
     * denotes x>y, and 0 denotes x=y
     *
     * @return Returns the value of x op y based on the comparison result of x
     * and y
     */
    public boolean apply(int cmp) {
        if (cmp < 0) {
            return this == _neq
                    || this == _lt
                    || this == _lte;
        } else if (cmp == 0) {
            return this == _eq
                    || this == _lte
                    || this == _gte;
        } else {
            return this == _neq
                    || this == _gt
                    || this == _gte;
        }
    }

    /**
     * Returns the inverted operation. No change for = and !=, > becomes <, etc.
     */
    public BinaryComparisonOperator invert() {
        if(this==_eq||this==_neq)
            return this;
        else if(this==_lt)
            return _gt;
        else if(this==_gt)
            return _lt;
        else if(this==_lte) 
            return _gte;
        else // _gte
            return _lte;           
    }

    /**
     * Returns the negated operator: for = returns !=, for < returns >=, etc.
     */
    public BinaryComparisonOperator negate() {
        switch(this) {
        case _eq:return _neq;
        case _neq:return _eq;
        case _lt: return _gte;
        case _gt: return _lte;
        case _lte:return _gt;
        }
        // _gte:
        return _lt;
    }

    @Override
    public String toString() {
        return ops[0];
    }

    public static BinaryComparisonOperator fromString(String s) {
        return MAP.get(s);
    }
}
