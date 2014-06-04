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

import java.util.Arrays;
import java.util.List;

/**
 * N-ary logical operators: and, or
 */
public enum NaryLogicalOperator {
    _and("$and", "$all"), _or("$or", "$any");

    private final List<String> ops;

    private NaryLogicalOperator(String... ops) {
        if (ops != null && ops.length > 0) {
            this.ops = Arrays.asList(ops);
        } else {
            throw new RuntimeException(QueryConstants.ERR_OPERATOR_LIST_NULL_OR_EMPTY);
        }
    }

    @Override
    public String toString() {
        return ops.get(0);
    }

    public static NaryLogicalOperator fromString(String s) {
        if (_and.ops.contains(s)) {
            return _and;
        } else if (_or.ops.contains(s)) {
            return _or;
        } else {
            return null;
        }
    }
}
