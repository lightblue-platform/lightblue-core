/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.constraints.ArraySizeConstraint;
import com.redhat.lightblue.util.Error;

public class ArraySizeCheckerTest {

    @Test
    public void testCheckConstraint_TooLarge() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        ArraySizeConstraint constraint = new ArraySizeConstraint("Fake Type");
        constraint.setValue(2);

        ArrayNode fieldValue = mock(ArrayNode.class);
        when(fieldValue.size()).thenReturn(3);

        new ArraySizeChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_NotMin_NoErrors() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        ArraySizeConstraint constraint = new ArraySizeConstraint("Fake Type");
        constraint.setValue(2);

        ArrayNode fieldValue = mock(ArrayNode.class);
        when(fieldValue.size()).thenReturn(1);

        new ArraySizeChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_Min_TooSmall() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        ArraySizeConstraint constraint = new ArraySizeConstraint(ArraySizeConstraint.MIN);
        constraint.setValue(2);

        ArrayNode fieldValue = mock(ArrayNode.class);
        when(fieldValue.size()).thenReturn(1);

        new ArraySizeChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_Min_NoErrors() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        ArraySizeConstraint constraint = new ArraySizeConstraint(ArraySizeConstraint.MIN);
        constraint.setValue(2);

        ArrayNode fieldValue = mock(ArrayNode.class);
        when(fieldValue.size()).thenReturn(3);

        new ArraySizeChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

}
