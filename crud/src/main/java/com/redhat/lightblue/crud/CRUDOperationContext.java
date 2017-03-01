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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.ExecutionOptions;
import com.redhat.lightblue.ResultMetadata;
import com.redhat.lightblue.hooks.HookManager;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Measure;

/**
 * An implementation of this class is passed into CRUD operation
 * implementations. It contains information about the caller roles, correct
 * metadata versions, and the constraint validators that will be used in this
 * call.
 */
public abstract class CRUDOperationContext implements MetadataResolver, Serializable {

    private static final long serialVersionUID = 1l;

    private final Factory factory;
    private final String entityName;
    private final Set<String> callerRoles;
    private List<DocCtx> documents;
    private final List<Error> errors = new ArrayList<>();
    private final Map<String, Object> propertyMap = new HashMap<>();
    private final CRUDOperation CRUDOperation;
    private final HookManager hookManager;
    private final ExecutionOptions executionOptions;
    private final Set<String> documentVersions=new HashSet<>();
    private boolean updateIfCurrent;

    public final Measure measure=new Measure();

    /**
     * This is the constructor used to represent the context of an operation
     */
    public CRUDOperationContext(CRUDOperation op,
                                String entityName,
                                Factory f,
                                List<JsonDoc> docs,
                                ExecutionOptions eo) {
        this.CRUDOperation = op;
        this.entityName = entityName;
        this.factory = f;
        // can assume are adding to an empty DocCtx list
        addDocuments(docs);
        this.hookManager = new HookManager(factory.getHookResolver(), factory.getNodeFactory());
        this.callerRoles = new HashSet<>();
        this.executionOptions = eo;
    }

    public CRUDOperationContext(CRUDOperation op,
                                String entityName,
                                Factory f,
                                Set<String> callerRoles,
                                HookManager hookManager,
                                List<JsonDoc> docs,
                                ExecutionOptions eo) {
        this.CRUDOperation = op;
        this.entityName = entityName;
        this.factory = f;
        addDocuments(docs);
        this.callerRoles = callerRoles;
        this.hookManager = hookManager;
        this.executionOptions = eo;
    }

    /**
     * This constructor is used to construct an operation context that is
     * derived from another existing context.
     */
    public CRUDOperationContext(CRUDOperation op,
                                String entityName,
                                Factory f,
                                List<DocCtx> docs,
                                Set<String> callerRoles,
                                HookManager hookManager,
                                ExecutionOptions eo) {
        this.CRUDOperation = op;
        this.entityName = entityName;
        this.factory = f;
        this.documents = docs;
        this.callerRoles = callerRoles;
        this.hookManager = hookManager;
        this.executionOptions = eo;
    }

    /**
     * If this list is non-empty, then update operations should be
     * performed only if document versions are unchanged
     */
    public Set<String> getUpdateDocumentVersions() {
        return documentVersions;
    }

    /**
     * If true, then only update the documents in
     * updateDocumentVersions set, and update them only if they are
     * unchanged
     */
    public boolean isUpdateIfCurrent() {
        return updateIfCurrent;
    }

    public void setUpdateIfCurrent(boolean b) {
        updateIfCurrent=b;
    }
        
    /**
     * Returns the execution options
     */
    public ExecutionOptions getExecutionOptions() {
        return executionOptions;
    }

    /**
     * Resets the operation context. Clears errors, sets the given document list
     * as the new document list.
     */
    public void reset(List<DocCtx> docList) {
        errors.clear();
        setDocuments(docList);
    }

    /**
     * Returns the entity name in the context
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the factory instance that controls the validator and CRUD
     * instances.
     */
    public Factory getFactory() {
        return factory;
    }

    /**
     * Sets the caller roles
     */
    protected void addCallerRoles(Set<String> roles) {
        this.callerRoles.addAll(roles);
    }

    /**
     * Returns the roles the caller is in
     */
    public Set<String> getCallerRoles() {
        return callerRoles;
    }

    /**
     * Returns the list of documents in the context
     */
    public List<DocCtx> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocCtx> docs) {
        documents = docs;
    }

    /**
     * Adds a new document to the context
     *
     * @return Returns the new document
     */
    @Deprecated
    public DocCtx addDocument(JsonDoc doc) {
        return addDocument(doc,null);
    }
    
    /**
     * Adds a new document to the context with metadata
     *
     * @return Returns the new document
     */
    public DocCtx addDocument(JsonDoc doc,ResultMetadata rmd) {
        if (documents == null) {
            documents = new ArrayList<>();
        }
        DocCtx x = new DocCtx(doc,rmd);
        documents.add(x);
        return x;
    }

    /**
     * Adds new documents to the context
     */
    public void addDocuments(Collection<JsonDoc> docs) {
        if (documents == null) {
            if (docs != null) {
                documents = new ArrayList<>(docs.size());
            } else {
                documents = new ArrayList<>();
            }
        }
        if (docs != null) {
            for (JsonDoc x : docs) {
                documents.add(new DocCtx(x));
            }
        }
    }

    /**
     * Returns the current operation
     */
    public CRUDOperation getCRUDOperation() {
        return CRUDOperation;
    }

    /**
     * Returns a list of documents with no errors
     */
    public List<DocCtx> getDocumentsWithoutErrors() {
        if (documents != null) {
            List<DocCtx> list = new ArrayList<>(documents.size());
            for (DocCtx doc : documents) {
                if (!doc.hasErrors()) {
                    list.add(doc);
                }
            }
            return list;
        } else {
            return null;
        }
    }

    /**
     * Returns a list of output documents with no errors
     */
    public List<JsonDoc> getOutputDocumentsWithoutErrors() {
        if (documents != null) {
            List<JsonDoc> list = new ArrayList<>(documents.size());
            for (DocCtx doc : documents) {
                if (!doc.hasErrors() && doc.getOutputDocument() != null) {
                    list.add(doc.getOutputDocument());
                }
            }
            return list;
        } else {
            return null;
        }
    }

    /**
     * Returns a list of output document metadata with no errors
     */
    public List<ResultMetadata> getOutputDocumentMetadataWithoutErrors() {
        if (documents != null) {
            List<ResultMetadata> list = new ArrayList<>(documents.size());
            for (DocCtx doc : documents) {
                if (!doc.hasErrors() && doc.getOutputDocument() != null) {
                    list.add(doc.getResultMetadata());
                }
            }
            return list;
        } else {
            return null;
        }
    }

    /**
     * Adds an error to the context
     */
    public void addError(Error e) {
        errors.add(e);
    }

    /**
     * Adds errors to the context
     */
    public void addErrors(Collection<Error> l) {
        errors.addAll(l);
    }

    /**
     * Returns the list of errors
     */
    public List<Error> getErrors() {
        return errors;
    }

    /**
     * Returns all the data errors in the context. If there are none, returns an
     * empty list.
     */
    public List<DataError> getDataErrors() {
        List<DataError> list = new ArrayList<>();
        if (documents != null) {
            for (DocCtx doc : documents) {
                DataError err = doc.getDataError();
                if (err != null) {
                    list.add(err);
                }
            }
        }
        return list;
    }

    /**
     * Returns if there are any document errors
     */
    public boolean hasDocumentErrors() {
        if (documents != null) {
            for (DocCtx x : documents) {
                if (x.hasErrors()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if there are documents with no errors
     */
    public boolean hasDocumentsWithoutErrors() {
        if (documents != null) {
            for (DocCtx x : documents) {
                if (!x.hasErrors()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if there are any errors. This does not take into account document
     * errors.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Properties for the context
     */
    public Object getProperty(String name) {
        return propertyMap.get(name);
    }

    /**
     * Properties for the context
     */
    public void setProperty(String name, Object value) {
        propertyMap.put(name, value);
    }

    /**
     * The hookManager for this operation
     */
    public HookManager getHookManager() {
        return hookManager;
    }
}
