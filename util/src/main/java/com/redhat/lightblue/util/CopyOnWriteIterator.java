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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator extension that copies the underlying list when it is modified
 * through this iterator. The original list remains unchanged, and the new list
 * can be obtained from the iterator instance.
 */
public class CopyOnWriteIterator<T> implements Iterator<T> {

    private final List<T> list;
    private List<T> copiedList = null;
    private int ix;
    private final Iterator<T> readItr;
    private boolean deleted=false;

    /**
     * Constructs an iterator for the given list
     */
    public CopyOnWriteIterator(List<T> list) {
        this.list = list;
        this.readItr = list.iterator();
        ix = -1;
    }

    @Override
    public boolean hasNext() {
        return readItr.hasNext();
    }

    @Override
    public T next() {
        // If pointer is invalid, don't skip. The element under
        // pointer is deleted, so it is already pointing to the next
        // one
        if(!deleted)
            ix++;
        deleted=false;
        return readItr.next();
    }

    /**
     * Creates a copy of the underlying list, and removes the element from the
     * new copy. Original list remains unchanged.
     */
    @Override
    public void remove() {
        // Removing an element makes the current pointer invalid.
        if(deleted)
            throw new NoSuchElementException();
        copy();
        copiedList.remove(ix);
        deleted=true;
    }

    /**
     * Creates a copy of the underlying list, and sets the element that was just
     * read in the new copy. Original list remains unchanged. Note that this
     * modifies the current element, that is, if next() is called once, set(x)
     * sets 0th element to x not the 1st element
     */
    public void set(T object) {
        if(deleted)
            throw new NoSuchElementException();
        copy();
        copiedList.set(ix, object);
    }

    /**
     * Returns if the original list has been copied.
     */
    public boolean isCopied() {
        return copiedList != null;
    }

    /**
     * Returns the copied list instance. If the list was not copied (i.e. was
     * not modified), returns null.
     */
    public List<T> getCopiedList() {
        return copiedList;
    }

    private void copy() {
        if (copiedList == null) {
            copiedList = new ArrayList<T>(list);
        }
    }
}
