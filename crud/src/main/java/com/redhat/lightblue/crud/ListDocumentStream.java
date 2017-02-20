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
import java.util.List;

/**
 * Default trivial implementation of document stream that uses a list to hold its elements
 */
public class ListDocumentStream<T> implements DocumentStream<T> {

    private final List<T> documents;
    private Iterator<T> itr;
    
    public ListDocumentStream(List<T> list) {
        this.documents=list;
    }

    @Override
    public boolean canRewind() {
        return true;
    }

    @Override
    public DocumentStream<T> rewind() {
        return new ListDocumentStream<T>(documents);
    }

    @Override
    public boolean hasNext() {
        // Lazy initialization, don't get the iterator until the last
        // moment. This is to protect against concurrent modification
        // exceptions if the list is modified between the creation of
        // the iterator and the actual iteration
        if(itr==null)
            itr=documents.iterator();
        return itr.hasNext();
    }

    @Override
    public T next() {
        if(itr==null)
            itr=documents.iterator();
        return itr.next();
    }

    @Override
    public void close() {}
}
