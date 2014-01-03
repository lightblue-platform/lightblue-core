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
public class UnaryLogicalOperatorTest {
    @Test
    public void apply() {
        UnaryLogicalOperator op = UnaryLogicalOperator._not;

        Assert.assertFalse(op.apply(true));
        Assert.assertTrue(op.apply(false));
    }
}
