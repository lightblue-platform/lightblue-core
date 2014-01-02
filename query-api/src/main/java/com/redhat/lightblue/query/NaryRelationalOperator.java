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

/**
 * N-ary relational operator definitions
 */
public enum NaryRelationalOperator {
    _in("$in"), _not_in("$nin", "$not_in");

    private final String[] ops;

    private NaryRelationalOperator(String... op) {
        this.ops = op;
    }

    /**
     * Applies the operator
     *
     * @param valueExists Whether the value of the field exists in the value list in a query of the form { field op
     * [value1,value2,...] }
     *
     * @return The value when the operator is applied to the predicate <code>valueExists</code>
     */
    public boolean apply(boolean valueExists) {
        return this == _in ? valueExists : !valueExists;
    }

    public String toString() {
        return ops[0];
    }

    private boolean has(String s) {
        for (String x : ops) {
            if (x.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static NaryRelationalOperator fromString(String s) {
        if (_in.has(s)) {
            return _in;
        } else if (_not_in.has(s)) {
            return _not_in;
        } else {
            return null;
        }
    }
}
