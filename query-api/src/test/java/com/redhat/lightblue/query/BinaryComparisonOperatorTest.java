/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.query;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class BinaryComparisonOperatorTest {

    @Test
    public void apply_eq() {
        BinaryComparisonOperator op = BinaryComparisonOperator._eq;

        Assert.assertFalse(op.apply(-1));
        Assert.assertTrue(op.apply(0));
        Assert.assertFalse(op.apply(1));
    }

    @Test
    public void apply_neq() {
        BinaryComparisonOperator op = BinaryComparisonOperator._neq;

        Assert.assertTrue(op.apply(-1));
        Assert.assertFalse(op.apply(0));
        Assert.assertTrue(op.apply(1));
    }

    @Test
    public void apply_lt() {
        BinaryComparisonOperator op = BinaryComparisonOperator._lt;

        Assert.assertTrue(op.apply(-1));
        Assert.assertFalse(op.apply(0));
        Assert.assertFalse(op.apply(1));
    }

    @Test
    public void apply_gt() {
        BinaryComparisonOperator op = BinaryComparisonOperator._gt;

        Assert.assertFalse(op.apply(-1));
        Assert.assertFalse(op.apply(0));
        Assert.assertTrue(op.apply(1));
    }

    @Test
    public void apply_lte() {
        BinaryComparisonOperator op = BinaryComparisonOperator._lte;

        Assert.assertTrue(op.apply(-1));
        Assert.assertTrue(op.apply(0));
        Assert.assertFalse(op.apply(1));
    }

    @Test
    public void apply_gte() {
        BinaryComparisonOperator op = BinaryComparisonOperator._gte;

        Assert.assertFalse(op.apply(-1));
        Assert.assertTrue(op.apply(0));
        Assert.assertTrue(op.apply(1));
    }
}
