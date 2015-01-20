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

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;
import com.redhat.lightblue.util.Error;

public class StringLengthCheckerTest {

    @Test
    public void testCheckConstraint_MINLENGTH_TooShort(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        StringLengthConstraint constraint = new StringLengthConstraint(StringLengthConstraint.MINLENGTH, 5);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("1234");

        new StringLengthChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MINLENGTH(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        StringLengthConstraint constraint = new StringLengthConstraint(StringLengthConstraint.MINLENGTH, 5);
        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("12345");

        new StringLengthChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MAXLENGTH_TooLong(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        StringLengthConstraint constraint = new StringLengthConstraint("fake type", 5);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("123456");

        new StringLengthChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MAXLENGTH(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        StringLengthConstraint constraint = new StringLengthConstraint("fake type", 5);
        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("12345");

        new StringLengthChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

}
