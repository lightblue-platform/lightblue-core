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

import java.util.Iterator;

/**
 * An iterator that iterates through the elements of a collection of iterators
 *
 * The implementation overrides the nextIterator method to return an
 * iterator. A non-null iterator means the ItrItr iterates through the
 * elements of that iterator, and when it is completed, it calls
 * nextIterator again. Once all the iterators are done, nextIterator
 * returns null, meaning the iteration should stop.
 */
public abstract class ItrItr<T> implements Iterator<T> {

    protected Iterator<T> currentIterator=null;
    
    @Override
    public boolean hasNext() {
        do {
            if(currentIterator==null) {
                currentIterator=nextIterator();
            }
            if(currentIterator!=null) {
                if(currentIterator.hasNext())
                    return true;
                else
                    currentIterator=null;
            } else
                return false;
        } while(true);
    }

    @Override
    public T next() {
        do {
            if(currentIterator==null) {
                currentIterator=nextIterator();
            }
            if(currentIterator!=null) {
                if(currentIterator.hasNext())
                    return currentIterator.next();
                else
                    currentIterator=null;
            } else {
                throw new NoSuchElementException();
            }
        } while(true);
    }

    protected abstract Iterator<T> nextIterator();
}
