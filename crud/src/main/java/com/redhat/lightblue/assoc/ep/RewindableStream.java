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
 * Wraps another stream, and provides rewinding functionality by
 * caching the results. If the wrapped stream is already rewindable,
 * it is a pass-thru
 */
public class RewindableStream<T> implements interface ResultStream<T> {

    private final List<T> list=new ArrayList<>();
    private final ResultStream<T> sourceStream;
    private int at;
    private boolean caching;

    public RewindableStream(ResultStream<T> source) {
        this.sourceStream=source;
        at=0;
        caching=!sourceStream.canRewind();
    }

    @Override
    public T next() {
        T nextObject;
        if(caching) {
            nextObject=sourceStream.next();
            if(nextObject!=null) {
                list.add(nextObject);
            } else {
                caching=false;
            }
        } else {
            if(sourceStream.canRewind()) {
                nextObject=sourceStream.next();
            } else {
                if(list.size()>at) {
                    nextObject=list.get(at++);
                } else {
                    nextObject=null;
                }
            }
        }
        return nextObject;
    }

    @Override
    public boolean canRewind() {
        return true;
    }

    @Override
    public void rewind() {
        if(sourceStream.canRewind)
            sourceStream.rewind();
        else {
            caching=false;
            at=0;
        }
    }
}
