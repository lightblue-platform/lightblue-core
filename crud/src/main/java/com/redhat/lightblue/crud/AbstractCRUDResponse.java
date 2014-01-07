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

import com.redhat.lightblue.util.Error;

/**
 * Base class for CRUD responses. Contains the error list.
 */
public abstract class AbstractCRUDResponse implements Serializable {

    private static final long serialVersionUID = 1l;

    private List<Error> errors;

    public AbstractCRUDResponse() {
    }

    public AbstractCRUDResponse(List<Error> errors) {
        this.errors = errors;
    }

    /**
     * Returns errors that are not related to data
     */
    public List<Error> getErrors() {
        return errors;
    }

    /**
     * Sets the errors list
     */
    public void setErrors(List<Error> l) {
        errors = l;
    }
}
