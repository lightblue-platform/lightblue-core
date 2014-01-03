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
public class NaryRelationalOperatorTest {
    @Test
    public void apply_in() {
        NaryRelationalOperator op = NaryRelationalOperator._in;

        Assert.assertFalse(op.apply(false));
        Assert.assertTrue(op.apply(true));
    }

    @Test
    public void apply_not_in() {
        NaryRelationalOperator op = NaryRelationalOperator._not_in;

        Assert.assertTrue(op.apply(false));
        Assert.assertFalse(op.apply(true));
    }

    @Test
    public void fromString_in() {
        NaryRelationalOperator op = NaryRelationalOperator.fromString("$in");

        Assert.assertNotNull(op);
        Assert.assertEquals(NaryRelationalOperator._in, op);
    }

    @Test
    public void fromString_nin() {
        NaryRelationalOperator op = NaryRelationalOperator.fromString("$nin");

        Assert.assertNotNull(op);
        Assert.assertEquals(NaryRelationalOperator._not_in, op);
    }

    @Test
    public void fromString_not_in() {
        NaryRelationalOperator op = NaryRelationalOperator.fromString("$not_in");

        Assert.assertNotNull(op);
        Assert.assertEquals(NaryRelationalOperator._not_in, op);
    }
}
