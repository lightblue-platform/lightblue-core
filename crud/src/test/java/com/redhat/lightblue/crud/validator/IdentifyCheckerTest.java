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
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class IdentifyCheckerTest {

    @Test
    public void testCheckConstraint_NoErrors() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(mock(JsonNode.class));

        new IdentityChecker().checkConstraint(validator, null, path, null, doc);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_WithErrors() {
        ConstraintValidator validator = mock(ConstraintValidator.class);

        Path path = mock(Path.class);
        when(path.nAnys()).thenReturn(0);

        JsonDoc doc = mock(JsonDoc.class);
        when(doc.get(path)).thenReturn(null);

        new IdentityChecker().checkConstraint(validator, null, path, null, doc);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

}
