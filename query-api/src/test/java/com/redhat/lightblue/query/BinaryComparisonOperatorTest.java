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
