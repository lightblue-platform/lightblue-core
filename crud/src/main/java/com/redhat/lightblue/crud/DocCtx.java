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
import java.util.ArrayList;
import java.util.Collection;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.DataError;

/**
 * This class represents an input document and all its associated data structures during an operation
 */
public class DocCtx extends JsonDoc {

    private final List<Error> errors = new ArrayList<>();
    private JsonDoc outputDoc = this;
    private Operation operationPerformed;

    public DocCtx(JsonDoc doc) {
        super(doc.getRoot());
    }

    /**
     * Adds an error to this document
     */
    public void addError(Error e) {
        errors.add(e);
    }

    /**
     * Adds errors to this document
     */
    public void addErrors(Collection<Error> l) {
        errors.addAll(l);
    }

    /**
     * Returns the errors associated with this document
     */
    public List<Error> getErrors() {
        return errors;
    }

    /**
     * Returns if the document has any associated errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * The output document
     */
    public JsonDoc getOutputDocument() {
        return outputDoc;
    }

    /**
     * The output document
     */
    public void setOutputDocument(JsonDoc doc) {
        this.outputDoc = doc;
    }

    /**
     * Returns the operation performed on this document
     */
    public Operation getOperationPerformed() {
        return operationPerformed;
    }

    /**
     * Sets the operation performed on this document
     */
    public void setOperationPerformed(Operation op) {
        operationPerformed=op;
    }

    /**
     * If there are errors for this documents, returns a data error. Otherwise, returns null.
     */
    public DataError getDataError() {
        if (!errors.isEmpty()) {
            return new DataError(outputDoc == null ? getRoot() : outputDoc.getRoot(), errors);
        } else {
            return null;
        }
    }
}
