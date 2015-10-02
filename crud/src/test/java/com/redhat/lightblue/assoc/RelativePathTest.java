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
package com.redhat.lightblue.assoc;

import org.junit.Test;
import org.junit.Assert;

import com.redhat.lightblue.util.Path;

public class RelativePathTest {
    @Test
    public void testRelativePaths() throws Exception {
        Assert.assertEquals(new Path("a.b.c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.c.d"),new Path("$parent.d"),Path.EMPTY));
        Assert.assertEquals(new Path("a.b.c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.c.d"),new Path("$parent.$parent.d"),Path.EMPTY));
        Assert.assertEquals(new Path("a.b.c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.c.d"),new Path("$parent.c.d"),Path.EMPTY));
        Assert.assertEquals(new Path("a.b.1.c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.*.c.d"),new Path("$parent.1.c.d"),Path.EMPTY));
        Assert.assertEquals(new Path("a.b.*.c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.*.c.d"),new Path("d"),Path.EMPTY));

        Assert.assertEquals(new Path("c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.*.c.d"),new Path("c.d"),new Path("a.b")));
        Assert.assertEquals(new Path("c.d"),ResolvedFieldInfo.getEntityRelativeFieldName(new Path("a.b.*.c.d"),new Path("$parent.d"),new Path("a.b")));
    }
}

