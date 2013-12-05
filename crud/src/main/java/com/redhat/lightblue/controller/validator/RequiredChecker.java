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

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.constraints.RequiredConstraint;

import com.redhat.lightblue.controller.ConstraintValidator;
import com.redhat.lightblue.controller.FieldConstraintDocChecker;

public class RequiredChecker implements FieldConstraintDocChecker {   

    public static final String ERR_REQUIRED="REQUIRED";

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                JsonDoc doc) {
        if(((RequiredConstraint)constraint).getValue()) {
            int nAnys=fieldMetadataPath.nAnys();
            if(nAnys==0) {
                if(doc.get(fieldMetadataPath)==null)
                    validator.addDocError(Error.get(ERR_REQUIRED));
            } else {
                // The required field is a member of an object that's an element of an array
                // If the array element exists, then the member must exist in that object
                Path parent=fieldMetadataPath.prefix(-1);
                String fieldName=fieldMetadata.getName();
                KeyValueCursor<Path,JsonNode> cursor=doc.getAllNodes(parent);
                while(cursor.hasNext()) {
                    cursor.next();
                    JsonNode parentObject=cursor.getCurrentValue();
                    if(parentObject.get(fieldName)==null)
                        validator.addDocError(Error.get(ERR_REQUIRED,cursor.getCurrentKey()+"."+fieldName));
                }
            }
        }
    }
}

    