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
package com.redhat.lightblue.crud.validator;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.FieldConstraintDocChecker;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * element identity fields are required. This constraint checker makes sure they are
 * present in the doc
 */
public class ArrayElementIdChecker implements FieldConstraintDocChecker {

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                JsonDoc doc) {
        List<Path> errors = RequiredChecker.getMissingFields(fieldMetadataPath, doc);
        for (Path x : errors) {
            validator.addDocError(Error.get(CrudConstants.ERR_REQUIRED, x.toString()));
        }
    }
}
