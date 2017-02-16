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
package com.redhat.lightblue.crud;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Maps a DocumentStream<S> to a DocumentStream<T>
 */
public class MapDocumentStream<S,D> implements DocumentStream<D> {

    private final DocumentStream<S> source;
    private final Function<S,D> map;

    private static class MapIterator<SRC,DST> implements Iterator<DST> {
        private final Iterator<SRC> itr;
        private Function<SRC,DST> map;
        MapIterator(Iterator<SRC> itr,Function<SRC,DST> map) {
            this.itr=itr;
            this.map=map;
        }

        @Override
        public boolean hasNext() {return itr.hasNext();}
        @Override
        public DST next() {return map.apply(itr.next());}
    }

    public MapDocumentStream(DocumentStream<S> source,Function<S,D> map) {
        this.source=source;
        this.map=map;
    }

    @Override
    public Iterator<D> getDocuments() {
        return new MapIterator<S,D>(source.getDocuments(),map);
    }
}
