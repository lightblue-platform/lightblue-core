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
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Registry;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

public class ConstraintValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintValidator.class);

    private final Registry<String, FieldConstraintChecker> fRegistry;
    private final Registry<String, EntityConstraintChecker> eRegistry;
    private final EntityMetadata md;

    private final Map<JsonDoc, List<Error>> docErrors = new HashMap<>();
    private final List<Error> errors = new ArrayList<>();

    private List<? extends JsonDoc> currentDocList;
    private JsonDoc currentDoc;
    private FieldTreeNode currentFieldNode;
    private Path currentFieldPath;
    private FieldConstraint currentFieldConstraint;
    private EntityConstraint currentEntityConstraint;

    protected ConstraintValidator(Registry<String, FieldConstraintChecker> r,
                                  Registry<String, EntityConstraintChecker> e,
                                  EntityMetadata md) {
        this.fRegistry = r;
        this.eRegistry = e;
        this.md = md;
    }

    public void clearErrors() {
        docErrors.clear();
        errors.clear();
    }

    public List<? extends JsonDoc> getCurrentDocList() {
        return currentDocList;
    }

    public JsonDoc getCurrentDoc() {
        return currentDoc;
    }

    public FieldTreeNode getCurrentFieldMetadata() {
        return currentFieldNode;
    }

    public Path getCurrentFieldPath() {
        return currentFieldPath;
    }

    public FieldConstraint getCurrentFieldConstraint() {
        return currentFieldConstraint;
    }

    public EntityConstraint getCurrentEntityConstraint() {
        return currentEntityConstraint;
    }

    public Map<JsonDoc, List<Error>> getDocErrors() {
        return docErrors;
    }

    public void addDocError(Error err) {
        getDocError().add(err);
    }

    public void addDocErrors(List<Error> list) {
        getDocError().addAll(list);
    }

    private List<Error> getDocError() {
        if (currentDoc == null) {
            throw new IllegalStateException();
        }
        List<Error> list = docErrors.get(currentDoc);
        if (list == null) {
            list = new ArrayList<>();
            docErrors.put(currentDoc, list);
        }
        return list;
    }

    public void addError(Error err) {
        errors.add(err);
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty()
                || !docErrors.isEmpty();
    }

    public void validateDocs(List<? extends JsonDoc> docList) {
        currentDocList = docList;
        currentDoc = null;

        LOGGER.debug("validateDocs() enter with {} docs", docList.size());
        Error.push("validateDocs");
        try {
            for (JsonDoc doc : docList) {
                validateDoc(doc);
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
        } finally {
            Error.pop();
        }
        LOGGER.debug("validateDocs() complete");
    }

    public void validateDoc(JsonDoc doc) {
        currentDoc = doc;
        Error.push("validateDoc");
        LOGGER.debug("validateDoc() enter with entity {}", md.getName());
        try {
            currentFieldConstraint = null;
            currentFieldNode = null;
            currentFieldPath = null;
            Path currentValuePath = null;
            JsonNode currentValue = null;
            checkEntityConstraints(doc);
            currentEntityConstraint = null;
            checkConstraints(doc, currentValuePath, currentValue);
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
        } finally {
            Error.pop();
        }
        currentDoc = null;
        currentFieldConstraint = null;
        currentFieldNode = null;
        currentFieldPath = null;
        LOGGER.debug("validateDoc() complete");
    }

    private void checkEntityConstraints(JsonDoc doc) {
        LOGGER.debug("checking entity constraints");
        for (EntityConstraint x : md.getConstraints()) {
            currentEntityConstraint = x;
            String constraintType = currentEntityConstraint.getType();
            LOGGER.debug("checking entity constraint " + constraintType);
            Error.push(constraintType);
            try {
                EntityConstraintChecker checker = eRegistry.find(constraintType);
                if (checker == null) {
                    throw Error.get(CrudConstants.ERR_NO_CONSTRAINT);
                }
                checker.checkConstraint(this, currentEntityConstraint, doc);
            } catch (Error e) {
                // rethrow lightblue error
                throw e;
            } catch (Exception e) {
                // throw new Error (preserves current error context)
                LOGGER.error(e.getMessage(), e);
                throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
            } finally {
                Error.pop();
            }
        }
    }

    private void checkConstraints(JsonDoc doc, Path currentValuePath, JsonNode currentValue) {
        LOGGER.debug("checking field constraints");
        FieldCursor cursor = md.getFieldCursor();
        Path skip=null;
        while (cursor.next()) {
            currentFieldNode = cursor.getCurrentNode();
            currentFieldPath = cursor.getCurrentPath();
            // Skip any fields reached by crossing entity boundaries
            if(skip!=null) {
                if(!currentFieldPath.prefix(skip.numSegments()).equals(skip))
                    skip=null;
            }
            if(skip==null) {
                if(currentFieldNode instanceof ResolvedReferenceField)
                    skip=currentFieldNode.getFullPath();
            } 
            if(skip==null) {
                LOGGER.debug("checking field {}", currentFieldPath);
                Error.push(currentFieldPath.toString());
                try {
                    List<FieldConstraint> constraints = null;
                    if (currentFieldNode instanceof Field) {
                        constraints = ((Field) currentFieldNode).getConstraints();
                    } else if (currentFieldNode instanceof SimpleArrayElement) {
                        constraints = ((SimpleArrayElement) currentFieldNode).getConstraints();
                    }
                    if (constraints != null && !constraints.isEmpty()) {
                        checkFieldConstraints(doc, constraints, currentValuePath, currentValue);
                    }
                } catch (Error e) {
                    // rethrow lightblue error
                    throw e;
                } catch (Exception e) {
                    // throw new Error (preserves current error context)
                    LOGGER.error(e.getMessage(), e);
                    throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
                } finally {
                    Error.pop();
                }
            }
        }
    }

    private void checkFieldConstraints(JsonDoc doc, List<FieldConstraint> constraints, Path currentValuePath, JsonNode currentValue) {

        for (FieldConstraint x : constraints) {
            currentFieldConstraint = x;
            String constraintType = currentFieldConstraint.getType();
            LOGGER.debug("checking constraint " + constraintType);
            Error.push(constraintType);
            try {
                FieldConstraintChecker checker = fRegistry.find(constraintType);
                if (checker == null) {
                    throw Error.get(CrudConstants.ERR_NO_CONSTRAINT);
                }
                if (checker instanceof FieldConstraintDocChecker) {
                    // Constraint needs to be checked once for the doc
                    checkFieldContraints(doc, (FieldConstraintDocChecker) checker);
                } else if (checker instanceof FieldConstraintValueChecker) {
                    // Constraint needs to be checked for all the values in the doc
                    checkValueContraints(doc, (FieldConstraintChecker) checker, currentValuePath, currentValue);
                }
            } catch (Error e) {
                // rethrow lightblue error
                throw e;
            } catch (Exception e) {
                // throw new Error (preserves current error context)
                LOGGER.error(e.getMessage(), e);
                throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
            } finally {
                Error.pop();
            }
        }
    }

    private void checkFieldContraints(JsonDoc doc, FieldConstraintDocChecker checker) {
        ((FieldConstraintDocChecker) checker).checkConstraint(this,
                currentFieldNode,
                currentFieldPath,
                currentFieldConstraint,
                doc);
    }

    private void checkValueContraints(JsonDoc doc, FieldConstraintChecker checker, Path currentValuePath, JsonNode currentValue) {
        KeyValueCursor<Path, JsonNode> fieldValues = doc.getAllNodes(currentFieldPath);
        while (fieldValues.hasNext()) {
            fieldValues.next();
            currentValuePath = fieldValues.getCurrentKey();
            currentValue = fieldValues.getCurrentValue();
            Error.push(currentValuePath.toString());
            try {
                ((FieldConstraintValueChecker) checker).checkConstraint(this,
                        currentFieldNode,
                        currentFieldPath,
                        currentFieldConstraint,
                        currentValuePath,
                        doc,
                        currentValue);
            } catch (Error e) {
                // rethrow lightblue error
                throw e;
            } catch (Exception e) {
                // throw new Error (preserves current error context)
                LOGGER.error(e.getMessage(), e);
                throw Error.get(CrudConstants.ERR_CRUD, e.getMessage());
            } finally {
                Error.pop();
            }
        }
    }

    public EntityMetadata getEntityMetadata() {
        return md;
    }
}
