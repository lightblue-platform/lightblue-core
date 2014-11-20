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

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;

public class RelativeRewriteTest {

    private QueryExpression getq(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void testValueRewrite() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'=','rvalue':100}"));
        Assert.assertEquals("e.f.g", ((ValueComparisonExpression) q).getField().toString());
    }

    @Test
    public void testFieldRewrite() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'=','rfield':'a.b.c.x.y.z.w'}"));
        Assert.assertEquals("e.f.g", ((FieldComparisonExpression) q).getField().toString());
        Assert.assertEquals("x.y.z.w", ((FieldComparisonExpression) q).getRfield().toString());
    }

    @Test
    public void testCantRewrite() throws Exception {
        try {
            QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'x.y.z','op':'=','rvalue':100}"));
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testNonEmptyContextIn_toRelative() throws Exception {
        QueryExpression q=new RelativeRewriteIterator(new Path("a")).iterate(getq("{'array':'a.b', 'elemMatch':{'field':'c.d','op':'=','rvalue':1}}"));
        Assert.assertEquals("b",((ArrayMatchExpression)q).getArray().toString());
        Assert.assertEquals("c.d",((ValueComparisonExpression) ((ArrayMatchExpression)q).getElemMatch()).getField().toString());
    }
}
