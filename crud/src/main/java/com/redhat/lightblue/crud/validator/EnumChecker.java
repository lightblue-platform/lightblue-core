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

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.FieldConstraintValueChecker;
import com.redhat.lightblue.metadata.Enum;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class EnumChecker implements FieldConstraintValueChecker {

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                Path valuePath,
                                JsonDoc doc,
                                JsonNode fieldValue) {
        String name = ((EnumConstraint) constraint).getName();
        Set<String> values = null;

        if (name != null) {
            // find value set for this enum
            Enum e=validator.getEntityMetadata().getEntityInfo().getEnums().getEnum(name);
            if(e!=null) {
                values=e.getValues();
            }
        }

        if (null == values || !values.contains(fieldValue.asText())) {
            validator.addDocError(Error.get(CrudConstants.ERR_INVALID_ENUM, fieldValue.asText()));
        }
    }
}
