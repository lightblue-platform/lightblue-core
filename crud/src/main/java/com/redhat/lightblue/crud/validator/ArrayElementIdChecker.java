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
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.FieldConstraintDocChecker;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

/**
 * element identity fields are required. This constraint checker makes sure they
 * are present in the doc, and unique
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
        // Check uniqueness
        // There are two ways to interpret this:
        //   1) Element identity is unique within a doc
        //   2) Element identity is unique within an array
        // The difference between 1 and 2 is that if an array occurs more than once in a document
        // (meaning the array is inside an array), then do we allow element identities to repeat for
        // different arrays? Although #2 makes more sense to some, #1 is more useful and easier to implement,
        // so we do that.
        KeyValueCursor<Path,JsonNode> iterator=doc.getAllNodes(fieldMetadataPath);
        HashSet<Object> values=new HashSet<>();
        Type t=fieldMetadata.getType();
        while(iterator.hasNext()) {
            iterator.next();
            Path fieldName=iterator.getCurrentKey();
            JsonNode value=iterator.getCurrentValue();
            if(!values.add(t.fromJson(value)))
                validator.addDocError(Error.get(CrudConstants.ERR_DUPLICATE_ARRAY_ELEMENT_ID,value==null?"null":value.asText()));
        }
    }
}
