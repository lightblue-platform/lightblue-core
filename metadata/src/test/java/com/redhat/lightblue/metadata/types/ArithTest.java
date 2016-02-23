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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ArithTest {

    Arith arith;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAddBigDecimalWithBigDecimalOutput() {
        Assert.assertEquals(BigDecimal.valueOf(20), Arith.add(BigDecimal.valueOf(10.0), BigDecimal.valueOf(10.0), BigDecimalType.TYPE));
    }

    @Test
    public void testAddBigIntegerWithBigIntegerOutput() {
        Assert.assertEquals(new BigInteger("20", 10), Arith.add(new BigInteger("10", 10), new BigInteger("10", 10), BigIntegerType.TYPE));
    }

    @Test
    public void testAddDoubleWithDoubleOutput() {
        Assert.assertEquals(Double.valueOf(20), Arith.add(Double.valueOf(10), Double.valueOf(10), DoubleType.TYPE));
    }

    @Test
    public void testAddLongWithLongOutput() {
        Assert.assertEquals(Long.valueOf(20), Arith.add(Long.valueOf(10), Long.valueOf(10), IntegerType.TYPE));
    }

    @Test
    public void testPromoteBigDecimalAndBigDecimalResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(BigDecimalType.TYPE, BigDecimalType.TYPE));
    }

    @Test
    public void testPromoteBigDecimalAndBigIntegerResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(BigDecimalType.TYPE, BigIntegerType.TYPE));
    }

    @Test
    public void testPromoteBigDecimalAndDoubleResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(BigDecimalType.TYPE, DoubleType.TYPE));
    }

    @Test
    public void testPromoteBigIntegerAndBigDecimalResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(DoubleType.TYPE, BigIntegerType.TYPE));
    }

    @Test
    public void testPromoteBigIntegerAndBigIntegerResultsInBigIntegerType() {
        Assert.assertEquals(BigIntegerType.TYPE, Arith.promote(BigIntegerType.TYPE, BigIntegerType.TYPE));
    }

    @Test
    public void testPromoteBigIntegerAndDoubleResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(BigIntegerType.TYPE, DoubleType.TYPE));
    }

    @Test
    public void testPromoteDoubleAndBigDecimalResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(DoubleType.TYPE, BigDecimalType.TYPE));
    }

    @Test
    public void testPromoteDoubleAndBigIntegerResultsInBigDecimalType() {
        Assert.assertEquals(BigDecimalType.TYPE, Arith.promote(DoubleType.TYPE, BigIntegerType.TYPE));
    }

    @Test
    public void testPromoteDoubleAndDoubleResultsInDoubleType() {
        Assert.assertEquals(DoubleType.TYPE, Arith.promote(DoubleType.TYPE, DoubleType.TYPE));
    }

    @Test
    public void testPromoteIntegerAndIntegerResultsInIntegerType() {
        Assert.assertEquals(IntegerType.TYPE, Arith.promote(IntegerType.TYPE, IntegerType.TYPE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonNumberTypeResultsInIllegalArgumentException() {
        Arith.promote(DoubleType.TYPE, StringType.TYPE);
    }
}
