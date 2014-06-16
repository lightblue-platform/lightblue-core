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
package com.redhat.lightblue.util;

import org.junit.Assert;
import org.junit.Test;

public class NormalizeTest {

    @Test
    public void parentTest() {
        Assert.assertEquals("zero.three.four", new Path("zero.one.$parent.three.four").normalize().toString());
    }

    @Test
    public void parentParentTest() {
        Assert.assertEquals("three.four", new Path("zero.one.$parent.$parent.three.four").normalize().toString());
    }

    @Test
    public void manyParentTest() {
        try {
            new Path("zero.one.$parent.$parent.$parent.three.four").normalize();
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void parent_parentTest() {
        Assert.assertEquals("zero.four", new Path("zero.one.$parent.three.$parent.four").normalize().toString());
    }

    @Test
    public void thisTest() {
        Assert.assertEquals("zero.one.two.three.four", new Path("zero.one.two.$this.three.$this.four").normalize().toString());
    }

    @Test
    public void thisTest2() {
        Assert.assertEquals("zero.one.two.three.four", new Path("$this.zero.one.two.$this.three.$this.four").normalize().toString());
    }
}
