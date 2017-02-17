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
    private final Iterator<T> itr;
    
    public ListDocumentStream(List<T> list) {
        this.documents=list;
        this.itr=documents.iterator();
    }

    /**
     * Return the underlying list
     */
    public List<T> getDocuments() {
        return documents;
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public T next() {
        return itr.next();
    }

    @Override
    public void close() {}
}
