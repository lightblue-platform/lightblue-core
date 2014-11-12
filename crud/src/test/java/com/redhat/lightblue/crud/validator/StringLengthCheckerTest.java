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
