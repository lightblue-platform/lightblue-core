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

import com.redhat.lightblue.DataError;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

/**
 * CRUD layer insertion response
 */
public class CRUDInsertionResponse extends AbstractCRUDUpdateResponse {

	private static final long serialVersionUID = 1L;
	
    /**
     * Default ctor
     */
    public CRUDInsertionResponse() {
    }

    /**
     * Constructs a response object with the given values
     */
    public CRUDInsertionResponse(List<JsonDoc> docs,
                                 List<DataError> dataErrors,
                                 List<Error> errors) {
        super(docs, dataErrors, errors);
    }
}
