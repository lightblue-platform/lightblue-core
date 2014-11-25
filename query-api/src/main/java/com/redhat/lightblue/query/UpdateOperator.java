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
 * Update operators
 */
public enum UpdateOperator {
    _set("$set"),
    _unset("$unset"),
    _add("$add"),
    _append("$append"),
    _insert("$insert"),
    _foreach("$foreach");

    private final String name;

    private static final Map<String, UpdateOperator> MAP;

    static {
        MAP = new HashMap<>();
        _set.init(MAP);
        _unset.init(MAP);
        _add.init(MAP);
        _append.init(MAP);
        _insert.init(MAP);
        _foreach.init(MAP);
    }

    private UpdateOperator(String x) {
        this.name = x;
    }

    private void init(Map<String, UpdateOperator> map) {
        map.put(name, this);
    }

    public String toString() {
        return name;
    }

    public static UpdateOperator fromString(String s) {
        return MAP.get(s);
    }
}
