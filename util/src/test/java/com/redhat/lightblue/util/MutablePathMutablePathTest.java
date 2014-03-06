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

public class MutablePathMutablePathTest extends StringMutablePathTest {

    @Override
    public MutablePath createPath() {
        return new MutablePath(super.createPath());
    }

    @Test
    public void reinterpretTest1() throws Exception {
        Assert.assertEquals(new Path("a.b.c.d.e"), new MutablePath("a.b.c.d.e").rewriteIndexes(new Path("x.y.z")));
        Assert.assertEquals(new Path("a.b.c.d.e"), new MutablePath("a.b.c.d.e").rewriteIndexes(new Path("x.y.z.a.b.c.d.e")));
        Assert.assertEquals(new Path("a.b.c.d.e"), new MutablePath("a.b.c.d.e").rewriteIndexes(new Path("a.b.c.d.e")));
        Assert.assertEquals(new Path("a.b.c.d.e"), new MutablePath("a.b.c.d.e").rewriteIndexes(new Path("a.b.c")));
        Assert.assertEquals(new Path("a.b.c.d.e"), new MutablePath("a.b.c.d.e").rewriteIndexes(new Path("a.b.c.d.e.f.g")));
    }

    @Test
    public void reinterpretTest2() throws Exception {
        Assert.assertEquals(new Path("a.b.c.d.e.*"), new MutablePath("a.b.c.d.e.*").rewriteIndexes(new Path("a.b.c.d.e")));
        Assert.assertEquals(new Path("a.b.c.1.d.e.*"), new MutablePath("a.b.c.*.d.e.*").rewriteIndexes(new Path("a.b.c.1.d.e")));
        Assert.assertEquals(new Path("a.2.b.c.*.d.e.*"), new MutablePath("a.*.b.c.*.d.e.*").rewriteIndexes(new Path("a.2.b.c")));
    }
}
