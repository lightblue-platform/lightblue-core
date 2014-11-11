package com.redhat.lightblue.crud.validator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Enum;
import com.redhat.lightblue.metadata.Enums;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.util.Error;

public class EnumCheckerTest {

    protected ConstraintValidator mockEnum(ConstraintValidator validator, String name, Enum e){
        Enums enums = mock(Enums.class);
        when(enums.getEnum(name)).thenReturn(e);

        EntityInfo entityInfo = mock(EntityInfo.class);
        when(entityInfo.getEnums()).thenReturn(enums);

        EntityMetadata entityMetadata = mock(EntityMetadata.class);
        when(entityMetadata.getEntityInfo()).thenReturn(entityInfo);

        when(validator.getEntityMetadata()).thenReturn(entityMetadata);

        return validator;
    }

    /**
     * If the fieldValue's text value is in the returned Enum's values, then an error should
     * not be created.
     */
    @SuppressWarnings("serial")
    @Test
    public void testCheckConstraint_WithEnum_FieldValueIsInSet(){
        final String name = "Fake Name";

        Enum e = mock(Enum.class);
        when(e.getValues()).thenReturn(new HashSet<String>(){{add(name);}});

        ConstraintValidator validator = mock(ConstraintValidator.class);
        mockEnum(validator, name, e);

        EnumConstraint constraint = mock(EnumConstraint.class);
        when(constraint.getName()).thenReturn(name);

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

        Enum e = mock(Enum.class);
        when(e.getValues()).thenReturn(new HashSet<String>());

        ConstraintValidator validator = mock(ConstraintValidator.class);
        mockEnum(validator, name, e);

        EnumConstraint constraint = mock(EnumConstraint.class);
        when(constraint.getName()).thenReturn(name);

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
        mockEnum(validator, name, null);

        EnumConstraint constraint = mock(EnumConstraint.class);
        when(constraint.getName()).thenReturn(name);

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

        EnumConstraint constraint = mock(EnumConstraint.class);
        when(constraint.getName()).thenReturn(null);

        JsonNode fieldValue = mock(JsonNode.class);
        when(fieldValue.asText()).thenReturn("String value");

        new EnumChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

}
