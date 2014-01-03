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

import com.redhat.lightblue.DataError;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

public class CRUDInsertionResponse implements Serializable {

    private static final long serialVersionUID = 1l;

    private List<JsonDoc> documents;
    private List<DataError> dataErrors;
    private List<Error> errors;

    public CRUDInsertionResponse() {
    }

    public CRUDInsertionResponse(List<JsonDoc> docs,
                                 List<DataError> dataErrors,
                                 List<Error> errors) {
        this.documents = docs;
        this.dataErrors = dataErrors;
        this.errors = errors;
    }

    /**
     * Returns the documents projected as requested. Returns null if no projections were given
     */
    public List<JsonDoc> getDocuments() {
        return documents;
    }

    /**
     * Sets the documents list
     */
    public void setDocuments(List<JsonDoc> docs) {
        this.documents = docs;
    }

    /**
     * Returns data errors for each doc
     */
    public List<DataError> getDataErrors() {
        return dataErrors;
    }

    /**
     * Sets the data errors list
     */
    public void setDataErrors(List<DataError> l) {
        dataErrors = l;
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
