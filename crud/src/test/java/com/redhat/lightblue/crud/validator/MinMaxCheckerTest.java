package com.redhat.lightblue.crud.validator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.util.Error;

@RunWith(value = Parameterized.class)
public class MinMaxCheckerTest {

    /**
     * Col1: For debugging purposes to know which test case had issues
     * Col2: A instantiation to test with. Should always represent 2.
     */
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Integer.class, new Integer(2)},
                {Byte.class, new Byte(new Integer(2).byteValue())},
                {Short.class, new Short(new Integer(2).shortValue())},
                {Long.class, new Long(2L)},
                {Float.class, new Float(2F)},
                {Double.class, new Double(2D)},
                {BigInteger.class, new BigInteger("2")},
                {BigDecimal.class, new BigDecimal(2)}
        });
    }

    private final Number number;

    public MinMaxCheckerTest(Class<Number> type, Number number) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        this.number = number;
    }

    protected JsonNode mockFieldValue(JsonNode mock, int value){
        if((number instanceof Integer)
                || (number instanceof Byte)
                || (number instanceof Short)
                || (number instanceof Long)) {
            when(mock.asLong()).thenReturn(new Long(value));
        }
        else if((number instanceof Float)
                || (number instanceof Double)){
            when(mock.asDouble()).thenReturn(new Double(value));
        }
        else if(number instanceof BigInteger){
            when(mock.bigIntegerValue()).thenReturn(new BigInteger(String.valueOf(value)));
        }
        else if(number instanceof BigDecimal){
            when(mock.decimalValue()).thenReturn(new BigDecimal(value));
        }
        else{
            throw new IllegalArgumentException("Not a supported Number type: " + number.getClass());
        }

        return mock;
    }

    protected Number convertInt(int value){
        if((number instanceof Integer)
                || (number instanceof Byte)
                || (number instanceof Short)
                || (number instanceof Long)) {
            return new Long(value);
        }
        else if((number instanceof Float)
                || (number instanceof Double)){
            return new Double(value);
        }
        else if(number instanceof BigInteger){
            return new BigInteger(String.valueOf(value));
        }
        else if(number instanceof BigDecimal){
            return new BigDecimal(value);
        }

        throw new IllegalArgumentException("Not a supported Number type: " + number.getClass());
    }

    @Test
    public void testCheckConstraint_MIN_TooSmall(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint(MinMaxConstraint.MIN);
        constraint.setValue(number);

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 0);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MIN_Equal_Pass(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint(MinMaxConstraint.MIN);
        constraint.setValue(number);

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 2);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MIN_Pass(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint(MinMaxConstraint.MIN);
        constraint.setValue(number);

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 2);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MAX_TooLarge(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint("Fake Type");
        constraint.setValue(convertInt(0));

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 2);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, times(1)).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MAX_Equal_Pass(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint("Fake Type");
        constraint.setValue(number);

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 2);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

    @Test
    public void testCheckConstraint_MAX_Pass(){
        ConstraintValidator validator = mock(ConstraintValidator.class);

        MinMaxConstraint constraint = new MinMaxConstraint("Fake Type");
        constraint.setValue(number);

        JsonNode fieldValue = mock(JsonNode.class);
        mockFieldValue(fieldValue, 0);

        new MinMaxChecker().checkConstraint(validator, null, null, constraint, null, null, fieldValue);

        verify(validator, never()).addDocError(any(Error.class));
    }

}
