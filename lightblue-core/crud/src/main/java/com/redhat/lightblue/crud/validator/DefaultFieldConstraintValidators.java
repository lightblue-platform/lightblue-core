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

import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.util.Resolver;

import com.redhat.lightblue.metadata.constraints.ArraySizeConstraint;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;
import com.redhat.lightblue.metadata.constraints.IdentityConstraint;
import com.redhat.lightblue.metadata.constraints.ArrayElementIdConstraint;

import com.redhat.lightblue.crud.FieldConstraintChecker;

/**
 * Convenience class that contains all defailt field constraint validators
 */
public class DefaultFieldConstraintValidators
        implements Resolver<String, FieldConstraintChecker> {

    private final Map<String, FieldConstraintChecker> checkers
            = new HashMap<>();

    public DefaultFieldConstraintValidators() {
        checkers.put(ArraySizeConstraint.MIN, new ArraySizeChecker());
        checkers.put(ArraySizeConstraint.MAX, new ArraySizeChecker());
        checkers.put(EnumConstraint.ENUM, new EnumChecker());
        checkers.put(MinMaxConstraint.MIN, new MinMaxChecker());
        checkers.put(MinMaxConstraint.MAX, new MinMaxChecker());
        checkers.put(RequiredConstraint.REQUIRED, new RequiredChecker());
        checkers.put(StringLengthConstraint.MINLENGTH, new StringLengthChecker());
        checkers.put(StringLengthConstraint.MAXLENGTH, new StringLengthChecker());
        checkers.put(IdentityConstraint.IDENTITY, new IdentityChecker());
        checkers.put(ArrayElementIdConstraint.IDENTITY, new EmptyFieldConstraintDocChecker());
    }

    @Override
    public FieldConstraintChecker find(String name) {
        return checkers.get(name);
    }
}
