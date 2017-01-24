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
package com.redhat.lightblue.mindex;

import java.util.Iterator;

import com.redhat.lightblue.util.Tuples;

public abstract class LookupSpec {
    public Key buildKey() {
        throw new IllegalStateException();
    }
    
    public abstract boolean matches(Key key);
    
    /**
     * If returns true, the lookup needs an index scan. Value
     * lookups don't need a scan. Prefix and ranges do.
     */
    protected abstract boolean needsScan();
    
    /**
     * If returns true, the lookup spec is multi-valued.
     * multiValues() means the lookup spec requires more than one
     * definite values (e.g. an $in expression).  A range or
     * prefix is not multiValued, only a value lookup spec can be
     * multi-valued.
     */
    protected abstract boolean multiValued();
    
    /**
     * This is used to iterate multiple values of a lookup
     * spec. Each element of the iteration will be used to build a
     * LookupSpec that is single-valued. Pass the tuples to next()
     * to get the next lookup spec.
     *
     * This call sets up the passed tuples with multi-valued
     * specs. Returns true if tuples are setup. Returns false if
     * the spec is not multi-valued
     */
    protected abstract boolean iterate(Tuples<Object> tuples);
    
    /**
     * Returns the next single-valued lookup spec based on the tuple
     */
    protected abstract LookupSpec next(Iterator<Object> tuple);
}
