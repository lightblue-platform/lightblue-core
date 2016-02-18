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
 * Abstract base class for result streams with an initialization
 * block. First call to next() invokes firstItem(), and subsequent
 * calls to next() calls the nextItem().
 */
public abstract class AbstractResultStream<T> implements ResultStream<T> {

    private boolean first=true;
    private boolean finished=false;

    /**
     * Perform initializations and return the first item
     *
     * Default implementations calls nextItem()
     */
    protected T firstItem() {
        return nextItem();
    }

    /**
     * Returns the next item. It is guaranteed that firstItem() is called before this one
     */
    protected abstract T nextItem();

    /**
     * Will the next call be the first call
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Is iteration finished?
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Sets finished flag
     */
    protected boolean finished() {
        finished=true;
    }
    
    @Override 
    public T next() {
        T ret;
        if(!finished) {
            if(first) {
                ret=firstItem();
            } else {
                ret=nextItem();
            }
            if(ret==null) {
                finished=true;
            }
        } else {
            ret=null;
        }
        return ret;
    }
}


