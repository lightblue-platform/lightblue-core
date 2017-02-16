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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Default trivial implementation of document stream using a list
 */
public class ListDocumentStream<T> implements DocumentStream<T> {

    private final List<T> documents;
    
    public ListDocumentStream(T item) {
    	this.documents=new ArrayList<T>(1);
    	this.documents.add(item);
    }
    
    public ListDocumentStream(List<T> list) {
        this.documents=list;
    }

    @Override
    public Iterator<T> getDocuments() {
        return documents.iterator();
    }
}
