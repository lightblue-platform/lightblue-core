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
package com.redhat.lightblue.controller.validator;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;

import com.redhat.lightblue.controller.FieldConstraintValueChecker;
import com.redhat.lightblue.controller.ConstraintValidator;

public class StringLengthChecker implements FieldConstraintValueChecker {

    public static final String ERR_TOO_SHORT = "TOO_SHORT";
    public static final String ERR_TOO_LONG = "TOO_LONG";

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                Path valuePath,
                                JsonDoc doc,
                                JsonNode fieldValue) {
        int value = ((StringLengthConstraint) constraint).getValue();
        String type = ((StringLengthConstraint) constraint).getType();
        int len = fieldValue.asText().length();
        if (StringLengthConstraint.MINLENGTH.equals(type)) {
            if (len < value) {
                validator.addDocError(Error.get(ERR_TOO_SHORT, fieldValue.asText()));
            }
        } else {
            if (len > value) {
                validator.addDocError(Error.get(ERR_TOO_LONG, fieldValue.asText()));
            }
        }
    }
}
