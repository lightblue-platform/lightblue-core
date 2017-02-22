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
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Consumer;

/**
 * A document stream than can be rewound
 */
public interface RewindableDocumentStream<T> extends DocumentStream<T> {
    
    /**
     * returns a new stream that starts the same resultset from the beginning. Only works if canRewind() is true
     */
    RewindableDocumentStream<T> rewind();

    static class RewindableDocumentStreamMapper<S,D> extends DocumentStream.DocumentStreamMapper<S,D>
        implements RewindableDocumentStream<D> {
        RewindableDocumentStreamMapper(RewindableDocumentStream<S> source,Function<S,D> map) {
            super(source,map);
        }
        @Override
        public RewindableDocumentStream<D> rewind() {
            return new RewindableDocumentStreamMapper<S,D>(((RewindableDocumentStream<S>)source).rewind(),map);
        }
    }

}
