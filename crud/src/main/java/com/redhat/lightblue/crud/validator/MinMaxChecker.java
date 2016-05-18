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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.FieldConstraintValueChecker;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MinMaxChecker implements FieldConstraintValueChecker {

    @Override
    public void checkConstraint(ConstraintValidator validator,
                                FieldTreeNode fieldMetadata,
                                Path fieldMetadataPath,
                                FieldConstraint constraint,
                                Path valuePath,
                                JsonDoc doc,
                                JsonNode fieldValue) {
        Number value = ((MinMaxConstraint) constraint).getValue();
        String type = ((MinMaxConstraint) constraint).getType();
        if (!(fieldValue instanceof NullNode)) {
            int cmp = cmp(fieldValue, value);
            // cmp==0: fieldValue=value
            // cmp <0: fieldValue<value
            // cmp >0: fieldValue>value
            if (MinMaxConstraint.MIN.equals(type)) {
                if (cmp < 0) {
                    validator.addDocError(Error.get(CrudConstants.ERR_VALUE_TOO_SMALL, fieldValue.asText()));
                }
            } else if (cmp > 0) {
                validator.addDocError(Error.get(CrudConstants.ERR_VALUE_TOO_LARGE, fieldValue.asText()));
            }
        }
    }

    private int cmp(JsonNode node, Number value) {
        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            return cmp(node.asLong(), value.longValue());
        } else if (value instanceof Float
                || value instanceof Double) {
            return cmp(node.asDouble(), value.doubleValue());
        } else if (value instanceof BigInteger) {
            return cmp(node.bigIntegerValue(), (BigInteger) value);
        } else {
            return cmp(node.decimalValue(), (BigDecimal) value);
        }
    }

    private int cmp(long nodeValue, long fieldValue) {
        long k = nodeValue - fieldValue;
        if (k == 0) {
            return 0;
        } else {
            return k < 0 ? -1 : 1;
        }
    }

    private int cmp(double nodeValue, double fieldValue) {
        if (nodeValue < fieldValue) {
            return -1;
        } else if (nodeValue > fieldValue) {
            return 1;
        } else {
            return 0;
        }
    }

    private int cmp(BigInteger nodeValue, BigInteger fieldValue) {
        return nodeValue.compareTo(fieldValue);
    }

    private int cmp(BigDecimal nodeValue, BigDecimal fieldValue) {
        return nodeValue.compareTo(fieldValue);
    }
}
