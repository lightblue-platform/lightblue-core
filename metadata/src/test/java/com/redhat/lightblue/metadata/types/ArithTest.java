package com.redhat.lightblue.metadata.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals(new BigDecimal(20), Arith.add(new BigDecimal(10.0), new BigDecimal(10.0), BigDecimalType.TYPE));
    }

    @Test
    public void testAddBigIntegerWithBigIntegerOutput() {
        Assert.assertEquals(new BigInteger("20", 10), Arith.add(new BigInteger("10", 10), new BigInteger("10", 10), BigIntegerType.TYPE));
    }
    
    @Test
    public void testAddDoubleWithDoubleOutput() {
        Assert.assertEquals(new Double(20), Arith.add(new Double(10), new Double(10), DoubleType.TYPE));
    }
    
    @Test
    public void testAddLongWithLongOutput() {
        Assert.assertEquals(new Long(20), Arith.add(new Long(10), new Long(10), IntegerType.TYPE));
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
    
    @Test(expected=IllegalArgumentException.class)
    public void testNonNumberTypeResultsInIllegalArgumentException() {
        Arith.promote(DoubleType.TYPE, StringType.TYPE);
    }
}
