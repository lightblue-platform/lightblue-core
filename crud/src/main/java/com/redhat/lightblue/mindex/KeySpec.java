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

import java.util.Set;

import com.redhat.lightblue.util.JsonDoc;

/**
 * A key specification, the public interface. This can be a simple or a array key specification
 */
public interface KeySpec {
    /**
     * Compare two keys
     */
    int compareKeys(Key k1,Key k2);
    
    /**
     * Extract key values from the doc, store in dest. Return dest. Allocate dest if dest is null
     */
    Set<Key> extract(JsonDoc doc,Set<Key> dest);
}

