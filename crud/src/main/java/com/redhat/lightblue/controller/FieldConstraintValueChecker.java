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
package com.redhat.lightblue.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

public interface FieldConstraintValueChecker extends FieldConstraintChecker {

    /**
     * Constraint checker function that validates the value of a field
     *
     * @param validator The constraint validator instance from which
     * the implementation can access the metadata and context information
     * @param fieldMedata The field metadata
     * @param fieldMetadataPath The path for the field metadata (i.e. may contain *)
     * @param constraint field constraint
     * @param valuePath The path to the current value being validated
     * @param doc The document containing the field
     * @param fieldValue The field value
     *
     * The function should add the field errors to validator
     */
    void checkConstraint(ConstraintValidator validator,
                         FieldTreeNode fieldMetadata,
                         Path fieldMetadataPath,
                         FieldConstraint constraint,
                         Path valuePath,
                         JsonDoc doc,
                         JsonNode fieldValue);
}
