/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This file is part of lightblue.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author lcestari
 */
public class AllMatchExpressionTest {

    /**
     * Test of toJson method, of class AllMatchExpression.
     */
    @Test
    public void testEqualsHashCode() {
        AllMatchExpression instance = new AllMatchExpression();
        assertEquals(new AllMatchExpression().hashCode(), instance.hashCode());
        assertEquals(new AllMatchExpression(), instance);
    }

}
