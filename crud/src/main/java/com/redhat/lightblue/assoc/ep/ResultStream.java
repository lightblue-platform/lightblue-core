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
 * Streams the result documents to the next stage in the pipeline
 */
public interface ResultStream<T> {

    public static final ResultStream<T> EMPTY=new ResultStream<T>() {
            @Override public T next() {return null;}
            @Override public boolean canRewind() {return true;}
            @Override public void rewind() {}
        };
    
    /**
     * Returns the next element in the result. If finished, returns null.
     */
    T next();

    /**
     * Returns if the stream can rewind
     */
    public default boolean canRewind() {return false;}

    /**
     * Rewind the stream. Throws an exception if stream cannot be rewound
     */
    public default void rewind() { throw new UnsupportedOperationException();}
}
