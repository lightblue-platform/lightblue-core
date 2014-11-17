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
package com.redhat.lightblue.util;

import java.util.Iterator;
import java.util.Collection;

/**
 * An adapter to present a collection as a Iteratable
 */
public class IteratableCollectionAdapter<X> implements Iterable<X> {
    private final Collection<X> coll;

    public IteratableCollectionAdapter(Collection<X> coll) {
        this.coll = coll;
    }

    @Override
    public Iterator<X> iterator() {
        return coll.iterator();
    }
}
