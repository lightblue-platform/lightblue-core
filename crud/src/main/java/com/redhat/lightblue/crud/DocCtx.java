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
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.ResultMetadata;

/**
 * This class represents a document and its related copies, errors, and the
 * operation performed on the document
 *
 * DocCtx provides four views of a document:
 * <ul>
 * <li>DocCtx instance: This is the document on which we operate.</li>
 * <li>originalDoc: This is the copy of the document before any modifications
 * are done on it. This has to be explicitly set.</li>
 * <li>outputDoc: This is the version of the document that is projected to be
 * returned. Initially it points to DocCtx instance, and must be explicitly set
 * to point to something else if projections are applied, or null if document
 * will not appear in the output.</li>
 * <li>updatedDoc: This is the copy of the unprojected updated document . This
 * has to be explicitly set (expected on the update & save operations). </li>
 * <li>resultMetadata: Result specific metadata</li>
 * </ul>
 */
public class DocCtx extends JsonDoc {

    private final List<Error> errors = new ArrayList<>();
    private JsonDoc originalDoc = null;
    private JsonDoc outputDoc = this;
    private JsonDoc updatedDoc = null;
    private CRUDOperation CRUDOperationPerformed;
    private final Map<String, Object> propertyMap = new HashMap<>();
    private ResultMetadata resultMetadata;

    public DocCtx(JsonDoc doc) {
        super(doc.getRoot());
    }

    public DocCtx(JsonDoc doc,ResultMetadata rmd) {
        super(doc.getRoot());
        this.resultMetadata=rmd;
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
     * Returns the projected output document
     */
    public JsonDoc getOutputDocument() {
        return outputDoc;
    }

    /**
     * Set the projected output document
     */
    public void setOutputDocument(JsonDoc doc) {
        this.outputDoc = doc;
    }

    /**
     * Returns the copy of the document before any modifications
     */
    public JsonDoc getOriginalDocument() {
        return originalDoc;
    }

    /**
     * Sets the original document to a copy of this object reference
     */
    public void copyOriginalFromThis() {
        originalDoc = copy();
    }

    /**
     * This method is to be called before starting making modifications on the
     * document, so that a copy of the original can be saved. It'll create a
     * copy of the current status of the document if there isn't one set
     * already. If the output document has already set, it will also make a copy
     * to the updated document field
     */
    public void startModifications() {
        if (originalDoc == null || originalDoc == this) {
            copyOriginalFromThis();
        }
        if (updatedDoc == null || updatedDoc == outputDoc) {
            copyUpdatedDocFromOutputDoc();
        }
    }

    /**
     * Sets the copy of the document before any modifications
     */
    public void setOriginalDocument(JsonDoc doc) {
        originalDoc = doc;
    }

    /**
     * Returns the operation performed on this document
     */
    public CRUDOperation getCRUDOperationPerformed() {
        return CRUDOperationPerformed;
    }

    /**
     * Sets the operation performed on this document
     */
    public void setCRUDOperationPerformed(CRUDOperation op) {
        CRUDOperationPerformed = op;
    }

    /**
     * If there are errors for this documents, returns a data error. Otherwise,
     * returns null.
     */
    public DataError getDataError() {
        if (!errors.isEmpty()) {
            return new DataError(outputDoc == null ? getRoot() : outputDoc.getRoot(), errors);
        } else {
            return null;
        }
    }

    /**
     * Properties for the document context
     */
    public Object getProperty(String name) {
        return propertyMap.get(name);
    }

    /**
     * Properties for the document context
     */
    public void setProperty(String name, Object value) {
        propertyMap.put(name, value);
    }

    /**
     * Returns the copy of the unprojected output document. To access the
     * projected output document, see getOutputDocument() method
     */
    public JsonDoc getUpdatedDocument() {
        return updatedDoc;
    }

    /**
     * Sets the unprojected output document to a copy of output document
     */
    public void copyUpdatedDocFromOutputDoc() {
        if (outputDoc != null) {
            updatedDoc = outputDoc.copy();
        }
    }

    /**
     * Sets a JsonDoc which must contain the unprojected updated document
     */
    public void setUpdatedDocument(JsonDoc doc) {
        updatedDoc = doc;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }

    public void setResultMetadata(ResultMetadata d) {
        resultMetadata=d;
    }
}
