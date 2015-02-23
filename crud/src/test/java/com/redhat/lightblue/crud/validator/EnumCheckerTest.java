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

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Enum;
import com.redhat.lightblue.metadata.EnumValue;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.util.Error;

public class EnumCheckerTest {

    protected ConstraintValidator mockEnum(ConstraintValidator validator, Enum e){
        EntityInfo entityInfo = new EntityInfo("fake name");
        if(e != null){
            entityInfo.getEnums().addEnum(e);
        }

        EntityMetadata entityMetadata = mock(EntityMetadata.class);
        when(entityMetadata.getEntityInfo()).thenReturn(entityInfo);

        when(validator.getEntityMetadata()).thenReturn(entityMetadata);

        return validator;
    }

    /**
     * If the fieldValue's text value is in the returned Enum's values, then an error should
     * not be created.
     */
    @Test
    public void testCheckConstraint_WithEnum_FieldValueIsInSet(){
        final String name = "Fake Name";

        Enum e = new Enum(name);
        e.setValues(new HashSet<EnumValue>(Arrays.asList(new EnumValue(name, null))));

        ConstraintValidator validator = mock(ConstraintValidator.class);
        mockEnum(validator, e);

        EnumConstraint constraint = new EnumConstraint();
        constraint.setName(name);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn(name);

        new EnumChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    /**
     * If the fieldValue's text value is not in the returned Enum's values, then an error should
     * be created.
     */
    @Test
    public void testCheckConstraint_WithEnum_ButFieldValueNotInSet(){
        final String name = "Fake Name";

        Enum e = new Enum("Fake Enum");

        ConstraintValidator validator = mock(ConstraintValidator.class);
        mockEnum(validator, e);

        EnumConstraint constraint = new EnumConstraint();
        constraint.setName(name);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn(name);

        new EnumChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    /**
     * If the returned Enum is null, then an error should be created.
     */
    @Test
    public void testCheckConstraint_NullEnum(){
        String name = "Fake Name";

        ConstraintValidator validator = mock(ConstraintValidator.class);
        mockEnum(validator, null);

        EnumConstraint constraint = new EnumConstraint();
        constraint.setName(name);

        JsonNode fieldValue = mock(JsonNode.class);

        new EnumChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    /**
     * If the name on the Constraint is null, then an error should be created.
     */
    @Test
    public void testCheckConstraint_NullName(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        EnumConstraint constraint = new EnumConstraint();
        constraint.setName(null);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("String value");

        new EnumChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

}
