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

import java.util.HashMap;
import java.util.Map;

import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.EntityConstraintChecker;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Resolver;

/**
 * This class contains empty entity constraint validators. These entity
 * constraint validators a implemented using back-end provided facilities, so
 * there is no need to check them.
 */
public class EmptyEntityConstraintValidators
        implements Resolver<String, EntityConstraintChecker> {

    private final Map<String, EntityConstraintChecker> checkers
            = new HashMap<>();

    private static final class EmptyChecker implements EntityConstraintChecker {
        @Override
        public void checkConstraint(ConstraintValidator validator,
                                    EntityConstraint constraint,
                                    JsonDoc doc) {
        }
    }

    public EmptyEntityConstraintValidators() {
    }

    @Override
    public EntityConstraintChecker find(String name) {
        return checkers.get(name);
    }
}
