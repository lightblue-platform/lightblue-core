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

import java.util.List;

import com.redhat.lightblue.util.Error;

/**
 * Deletion operation response 
 */
public class CRUDDeleteResponse extends AbstractCRUDResponse {

    private static final long serialVersionUID = 1L;
    
    private int numDeleted;

    /**
     * Default ctor for empty response
     */
    public CRUDDeleteResponse() {
    }

    /**
     * Ctor with error list
     */
    public CRUDDeleteResponse(List<Error> errors) {
        super(errors);
    }

    /**
     * Number of records deleted
     */
    public int getNumDeleted() {
        return numDeleted;
    }

    /**
     * Number of records deleted
     */
    public void setNumDeleted(int n) {
        numDeleted=n;
    }
}
