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

/**
 * This class represents a document and its related copies, errors, and the
 * operation performed on the document
 *
 * DocCtx provides three views of a document:
 * <ul>
 * <li>DocCtx instance: This is the document on which we operate.</li>
 * <li>originalDoc: This is the copy of the document before any modifications
 * are done on it. This has to be explicitly set.</li>
 * <li>outputDoc: This is the version of the document that is projected to be
 * returned. Initially it points to DocCtx instance, and must be explicitly set
 * to point to something else if projections are applied, or null if document
 * will not appear in the output.</li>
 * </ul>
 */
public class DocCtx extends JsonDoc {

    private final List<Error> errors = new ArrayList<>();
    private JsonDoc outputDoc = this;
    private JsonDoc originalDoc = null;
    private JsonDoc originalOutputDoc = null;
    private CRUDOperation CRUDOperationPerformed;
    private final Map<String, Object> propertyMap = new HashMap<>();

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
     * Returns the copy of the document before any modifications
     */
    public JsonDoc getOriginalDocument() {
        return originalDoc;
    }

    /**
     * Sets the originalDocument to a copy of this
     */
    public void copyOriginalFromThis() {
        originalDoc = copy();
    }

    /**
     * This method is to be called before starting making
     * modifications on the document, so that a copy of the original
     * can be saved. It'll create a copy of the current status of the
     * document if there isn't one set already.
     */
    public void startModifications() {
        if (originalDoc == null || originalDoc == this) {
            copyOriginalFromThis();
        }
        if (originalOutputDoc == null || originalOutputDoc == outputDoc) {
            copyOriginalOutputFromOutput();
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
     * Returns the copy of the output document before any modifications (such as projections)
     */
    public JsonDoc getOriginalOutputDocument() {
        return originalOutputDoc;
    }

    /**
     * Sets the originalOutputDoc to a copy of outputDoc
     */
    public void copyOriginalOutputFromOutput() {
        if(outputDoc != null) {
            originalOutputDoc = outputDoc.copy();
        }
    }

    /**
     * Sets the copy of the output document before any modifications
     */
    public void setOriginalOutputDocument(JsonDoc doc) {
        originalOutputDoc = doc;
    }

}
