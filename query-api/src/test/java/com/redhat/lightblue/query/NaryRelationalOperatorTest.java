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

    @Test
    public void fromStringNotValidOperator() {
        NaryRelationalOperator op = NaryRelationalOperator.fromString("notValidOperatorTest");

        Assert.assertNull(op);
    }
}
