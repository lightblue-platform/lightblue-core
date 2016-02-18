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
 * Provides an iterable interface to a ResultStream object. Every time
 * iterator() is called, the source stream is rewound.
 */
public class IterableAdapter<T> implements ResultStream<T> {

    private final RewindableStream<T> stream;
    
    public IterableAdapter(RewindableStream<T> stream) {
        this.stream=stream;
    }

    @Override
    public Iterator<T> iterator() {
        stream.rewind();
        return new Iterator<T>() {
            private T nextItem=null;
            private boolean finished=false;
            @Override
            public boolean hasNext() {
                if(!finished) {
                    if(nextItem==null) {
                        nextItem=stream.next();
                        if(nextItem==null) {
                            finished=true;
                        }
                    }
                }
                return nextItem!=null;
            }
            
            @Override
            public T next() {
                if(finished) {
                    throw new NoSuchElementException();
                } else {
                    if(nextItem==null) {
                        nextItem=stream.next();
                        if(nextItem==null)
                            finished=true;
                    }
                    if(nextItem==null)
                        throw new NoSuchElementException();
                    T t=nextItem;
                    nextItem=null;
                    return t;
                }
            }
        }
    }
}
