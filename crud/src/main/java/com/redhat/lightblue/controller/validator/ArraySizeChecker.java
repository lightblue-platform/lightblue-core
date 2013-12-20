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
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.constraints.ArraySizeConstraint;

import com.redhat.lightblue.controller.FieldConstraintValueChecker;
import com.redhat.lightblue.controller.ConstraintValidator;

public class ArraySizeChecker implements FieldConstraintValueChecker {

    public static final String ERR_ARRAY_TOO_SMALL="ARRAY_TOO_SMALL";
    public static final String ERR_ARRAY_TOO_LARGE="ARRAY_TOO_LARGE";

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                Path valuePath,
                                JsonDoc doc,
                                JsonNode fieldValue) {
        int value=((ArraySizeConstraint)constraint).getValue();
        String type=((ArraySizeConstraint)constraint).getType();
        if(ArraySizeConstraint.MIN.equals(type)) {
            if(((ArrayNode)fieldValue).size()<value)
                validator.addDocError(Error.get(ERR_ARRAY_TOO_SMALL,Integer.toString(((ArrayNode)fieldValue).size())));
        } else {
            if(((ArrayNode)fieldValue).size()>value)
                validator.addDocError(Error.get(ERR_ARRAY_TOO_LARGE,Integer.toString(((ArrayNode)fieldValue).size())));
        }
    }
}
