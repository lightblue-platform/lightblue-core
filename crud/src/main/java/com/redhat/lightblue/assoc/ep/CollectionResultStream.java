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
package com.redhat.lightblue.assoc.ep;

/**
 * A result stream based on a collection
 */
public class CollectionResultStream<T> implements ResultStream<T> {

    private Iterator<T> iterator;
    private final Collection<T> collection;

    public CollectionResultStream(Collection<T> collection) {
        this.collection=collection;
        this.iterator=collection.iterator();
    }

    public CollectionResultStream(Iterator<T> itr) {
        this.iterator=itr;
        this.collection=null;
    }

    @Override
    public T next() {
        if(iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    @Override
    public boolean canRewind() {
        return collection!=null;
    }

    @Override
    public void rewind() {
        if(collection!=null)
            iterator=collection.iterator();
        else
            throw new UnsupportedOperationException();
    }
}
