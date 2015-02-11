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
import org.junit.Ignore;
import org.junit.Test;

public class RelativeRewriteIteratorTest {

    private QueryExpression getq(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void rewrite_value_comparison() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'=','rvalue':100}"));
        Assert.assertEquals("e.f.g", ((ValueComparisonExpression) q).getField().toString());
    }

    @Test
    public void rewrite_regex() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','regex':'xyz'}"));
        Assert.assertEquals("e.f.g", ((RegexMatchExpression) q).getField().toString());
    }

    @Test
    public void rewrite_nary_value_relational() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'$in','values':[1,2,3,4,5]}"));
        Assert.assertEquals("e.f.g", ((NaryValueRelationalExpression) q).getField().toString());
    }

    @Test
    public void reqrite_nary_field_relational() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'$in','rfield':'a.b.c.e.x'}"));
        Assert.assertEquals("e.f.g", ((NaryFieldRelationalExpression) q).getField().toString());
        Assert.assertEquals("e.x", ((NaryFieldRelationalExpression) q).getRfield().toString());
    }

    @Test
    public void reqrite_array_contains() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'array':'a.b.c.e.f.g','contains':'$any','values':[1,2,3]}"));
        Assert.assertEquals("e.f.g", ((ArrayContainsExpression) q).getArray().toString());
    }

    @Test
    public void rewrite_field_comparison() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'a.b.c.e.f.g','op':'=','rfield':'a.b.c.x.y.z.w'}"));
        Assert.assertEquals("e.f.g", ((FieldComparisonExpression) q).getField().toString());
        Assert.assertEquals("x.y.z.w", ((FieldComparisonExpression) q).getRfield().toString());
    }

    @Test
    public void rewrite_failure() throws Exception {
        try {
            QueryExpression q = new RelativeRewriteIterator(new Path("a.b.c")).iterate(getq("{'field':'x.y.z','op':'=','rvalue':100}"));
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void rewrite_array_elemMatch_value_comparison_simple() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a")).iterate(getq("{'array':'a.b', 'elemMatch':{'field':'c.d','op':'=','rvalue':1}}"));
        Assert.assertEquals("b", ((ArrayMatchExpression) q).getArray().toString());
        Assert.assertEquals("c.d", ((ValueComparisonExpression) ((ArrayMatchExpression) q).getElemMatch()).getField().toString());
    }

    @Test
    public void rewrite_array_elemMatch_field_comparison_simple() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a")).iterate(getq("{'array':'a.b', 'elemMatch':{'field':'c.d','op':'=','rfield':'c.e'}}"));
        Assert.assertEquals("b", ((ArrayMatchExpression) q).getArray().toString());
        Assert.assertEquals("c.d", ((FieldComparisonExpression) ((ArrayMatchExpression) q).getElemMatch()).getField().toString());
        Assert.assertEquals("c.e", ((FieldComparisonExpression) ((ArrayMatchExpression) q).getElemMatch()).getRfield().toString());
    }

    /**
     * TODO remove @Ignore once relative paths are rewritten
     * (lightblue-platform/lightblue-core#220)
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void rewrite_array_elemMatch_value_comparison_parent() throws Exception {
        QueryExpression q = new RelativeRewriteIterator(new Path("a")).iterate(getq("{'array':'a.b', 'elemMatch':{'field':'$parent.b.c.d','op':'=','rvalue':1}}"));
        Assert.assertEquals("b", ((ArrayMatchExpression) q).getArray().toString());
        Assert.assertEquals("c.d", ((ValueComparisonExpression) ((ArrayMatchExpression) q).getElemMatch()).getField().toString());
    }
}
