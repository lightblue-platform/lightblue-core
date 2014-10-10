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
import com.redhat.lightblue.hooks.HookManager;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

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
    private final Operation operation;
    private final HookManager hookManager;

    /**
     * This is the constructor used to represent the context of an operation
     */
    public CRUDOperationContext(Operation op,
                                String entityName,
                                Factory f,
                                List<JsonDoc> docs) {
        this.operation = op;
        this.entityName = entityName;
        this.factory = f;
        this.hookManager = new HookManager(factory.getHookResolver(), factory.getNodeFactory());
        this.callerRoles=new HashSet<>();
        if (docs != null) {
            documents = new ArrayList<>(docs.size());
            for (JsonDoc doc : docs) {
                documents.add(new DocCtx(doc));
            }
        } else {
            documents = null;
        }
    }

    /**
     * This constructor is used to construct an operation context that
     * is derived from another existing context.
     */
    public CRUDOperationContext(Operation op,
                                String entityName,
                                Factory f,
                                List<DocCtx> docs,
                                Set<String> callerRoles,
                                HookManager hookManager) {
        this.operation=op;
        this.entityName=entityName;
        this.factory=f;
        this.hookManager=hookManager;
        this.documents =docs;
        this.callerRoles=callerRoles;
    }

    /**
     * Resets the operation context. Clears arrors, sets the given
     * document list as the new document list.
     */
    public void reset(List<DocCtx> docList) {
        errors.clear();
        documents=docList;
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
    protected void setCallerRoles(Set<String> roles) {
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
        documents=docs;
    }

    /**
     * Adds a new document to the context
     *
     * @return Returns the new document
     */
    public DocCtx addDocument(JsonDoc doc) {
        if (documents == null) {
            documents = new ArrayList<>();
        }
        DocCtx x = new DocCtx(doc);
        documents.add(x);
        return x;
    }

    /**
     * Adds new documents to the context
     */
    public void addDocuments(Collection<JsonDoc> docs) {
        if (documents == null) {
            documents = new ArrayList<>();
        }
        for (JsonDoc x : docs) {
            documents.add(new DocCtx(x));
        }
    }

    /**
     * Returns the current operation
     */
    public Operation getOperation() {
        return operation;
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
