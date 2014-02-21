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
package com.redhat.lightblue.metadata.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.Type;

/**
 * Arithmetical operations using types
 */
public final class Arith {

    /**
     * Adds two numbers, and returns the result in resultType
     */
    public static Object add(Object operand1,
                             Object operand2,
                             Type resultType) {
        Object o1=resultType.cast(operand1);
        Object o2=resultType.cast(operand2);
        Object result;
        if(resultType instanceof BigDecimalType) {
            result=((BigDecimal)o1).add((BigDecimal)o2);
        } else if(resultType instanceof BigIntegerType) {
            result=((BigInteger)o1).add((BigInteger)o2);
        } else if(resultType instanceof DoubleType) {
            result=((Double)o1).doubleValue()+((Double)o2).doubleValue();
        } else {
            result=((Long)o1).longValue()+((Long)o2).longValue();
        }
        return result;
    }

    /**
     * Determines the resulting type of an arithmetic operation
     * between a value of type operand1 and a value of type
     * operand2. Throws IllegalArgumentException if the operation is
     * invalid.
     */
    public static Type promote(Type operand1,
                               Type operand2) {
        // For all cases except BigInteger-double operations, ordering
        // the types and getting max determines the reslt type. For
        // BigInteger-double operations, result is BigDecimal.
        if((operand1 instanceof BigIntegerType && operand2 instanceof DoubleType)||
           (operand1 instanceof DoubleType && operand2 instanceof BigIntegerType)) {
            return BigDecimalType.TYPE;
        } else {
            int o1=arithType(operand1);
            int o2=arithType(operand2);
            return arithType(Math.max(o1,o2));
        }
    }

    private static int arithType(Type operand) {
        if (operand instanceof IntegerType) {
            return 0;
        } else if (operand instanceof BigIntegerType) {
            return 1;
        } else if (operand instanceof DoubleType) {
            return 2;
        } else if (operand instanceof BigDecimalType) {
            return 3;
        } else {
            throw new IllegalArgumentException(operand.getName() + MetadataConstants.ERR_NOT_A_NUMBER_TYPE);
        }  
    }

    private static Type arithType(int type) {
        switch(type) {
        case 0: return IntegerType.TYPE;
        case 1: return BigIntegerType.TYPE;
        case 2: return DoubleType.TYPE;
        default: return BigDecimalType.TYPE; 
        }   
    }

    private Arith() {}
}