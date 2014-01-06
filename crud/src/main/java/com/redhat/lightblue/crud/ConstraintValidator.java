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

public class ConstraintValidator {

    public static final String ERR_NO_CONSTRAINT = "NO_CONSTRAINT";

    private static final Logger logger = LoggerFactory.getLogger(ConstraintValidator.class);

    private final Registry<String, FieldConstraintChecker> fRegistry;
    private final Registry<String, EntityConstraintChecker> eRegistry;
    private final EntityMetadata md;

    private final Map<JsonDoc, List<Error>> docErrors = new HashMap<JsonDoc, List<Error>>();
    private final List<Error> errors = new ArrayList<Error>();

    private List<JsonDoc> currentDocList;
    private JsonDoc currentDoc;
    private FieldTreeNode currentFieldNode;
    private Path currentFieldPath;
    private FieldConstraint currentFieldConstraint;
    private EntityConstraint currentEntityConstraint;
    private Path currentValuePath;
    private JsonNode currentValue;

    protected ConstraintValidator(Registry<String, FieldConstraintChecker> r,
                                  Registry<String, EntityConstraintChecker> e,
                                  EntityMetadata md) {
        this.fRegistry = r;
        this.eRegistry = e;
        this.md = md;
    }

    public List<JsonDoc> getCurrentDocList() {
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
            docErrors.put(currentDoc, list = new ArrayList<Error>());
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

    public void validateDocs(List<JsonDoc> docList) {
        currentDocList = docList;
        currentDoc = null;

        logger.debug("validateDocs() enter with {} docs", docList.size());
        Error.push("validateDocs");
        try {
            for (JsonDoc doc : docList) {
                validateDoc(doc);
            }
        } finally {
            Error.pop();
        }
        logger.debug("validateDocs() complete");
    }

    public void validateDoc(JsonDoc doc) {
        currentDoc = doc;
        Error.push("validateDoc");
        logger.debug("validateDoc() enter with entity {}", md.getName());
        try {
            currentFieldConstraint = null;
            currentFieldNode = null;
            currentFieldPath = null;
            currentValuePath = null;
            currentValue = null;
            logger.debug("checking entity constraints");
            for (EntityConstraint x : md.getConstraints()) {
                currentEntityConstraint = x;
                String constraintType = currentEntityConstraint.getType();
                logger.debug("checking entity constraint " + constraintType);
                Error.push(constraintType);
                try {
                    EntityConstraintChecker checker = eRegistry.find(constraintType);
                    if (checker == null) {
                        throw Error.get(ERR_NO_CONSTRAINT);
                    }
                    checker.checkConstraint(this,
                            currentEntityConstraint,
                            doc);

                } finally {
                    Error.pop();
                }
            }
            currentEntityConstraint = null;
            logger.debug("checking field constraints");
            FieldCursor cursor = md.getFieldCursor();
            while (cursor.next()) {
                currentFieldNode = cursor.getCurrentNode();
                currentFieldPath = cursor.getCurrentPath();
                logger.debug("checking field {}", currentFieldPath);
                Error.push(currentFieldPath.toString());
                try {
                    List<FieldConstraint> constraints = null;
                    if (currentFieldNode instanceof Field) {
                        constraints = ((Field) currentFieldNode).getConstraints();
                    }
                    if (constraints != null && !constraints.isEmpty()) {
                        for (FieldConstraint x : constraints) {
                            currentFieldConstraint = x;
                            String constraintType = currentFieldConstraint.getType();
                            logger.debug("checking constraint " + constraintType);
                            Error.push(constraintType);
                            try {
                                FieldConstraintChecker checker = fRegistry.find(constraintType);
                                if (checker == null) {
                                    throw Error.get(ERR_NO_CONSTRAINT);
                                }
                                currentValuePath = null;
                                currentValue = null;
                                if (checker instanceof FieldConstraintDocChecker) {
                                    // Constraint needs to be checked once for the doc
                                    ((FieldConstraintDocChecker) checker).checkConstraint(this,
                                            currentFieldNode,
                                            currentFieldPath,
                                            currentFieldConstraint,
                                            doc);
                                } else if (checker instanceof FieldConstraintValueChecker) {
                                    // Constraint needs to be checked for all the values in the doc
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
                                        } finally {
                                            Error.pop();
                                        }
                                    }
                                }
                            } finally {
                                Error.pop();
                            }
                        }
                    }
                } finally {
                    Error.pop();
                }
            }
        } finally {
            Error.pop();
        }
        currentDoc = null;
        currentFieldConstraint = null;
        currentFieldNode = null;
        currentFieldPath = null;
        currentValuePath = null;
        currentValue = null;
        logger.debug("validateDoc() complete");
    }

}
