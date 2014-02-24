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

import java.io.Serializable;

import java.util.List;

import com.redhat.lightblue.util.JsonDoc;

/**
 * CRUD layer response to find() request. Contains the found documents, total resultset size, and errors.
 */
public class CRUDFindResponse implements Serializable {

    private static final long serialVersionUID=1l;

    private List<JsonDoc> results;
    private long size;

    /**
     * Returns the result set
     */
    public List<JsonDoc> getResults() {
        return results;
    }

    /**
     * Sets the result set
     */
    public void setResults(List<JsonDoc> results) {
        this.results = results;
    }

    /**
     * Number of documents mathcing the query
     */
    public long getSize() {
        return size;
    }

    /**
     * Number of documents mathcing the query
     */
    public void setSize(long l) {
        size = l;
    }
}
