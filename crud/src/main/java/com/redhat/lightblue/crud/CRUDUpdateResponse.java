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

/**
 * CRUD layer update response
 */
public class CRUDUpdateResponse implements Serializable {

    private static final long serialVersionUID = 1l;

    private int numUpdated;
    private int numFailed;
    private int numMatched;

    /**
     * Returns the number of updated docs
     */
    public int getNumUpdated() {
        return numUpdated;
    }

    /**
     * Sets the number of updated docs
     */
    public void setNumUpdated(int n) {
        numUpdated = n;
    }

    /**
     * Returns the number of failed updated
     */
    public int getNumFailed() {
        return numFailed;
    }

    /**
     * Sets the number of failed docs
     */
    public void setNumFailed(int n) {
        numFailed = n;
    }

    /**
     * Number of documents matched the update criteria
     */
    public int getNumMatched() {
        return numMatched;
    }

    /**
     * Number of documents matched the update criteria
     */
    public void setNumMatched(int n) {
        numMatched = n;
    }
}
