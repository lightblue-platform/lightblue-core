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

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BindTest {

    private QueryExpression getq(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void cant_bind() throws Exception {
        Assert.assertTrue(getq("{'field':'x','op':'=','rvalue':'value'}").getBindableClauses().isEmpty());
        Assert.assertTrue(getq("{'$not':{'field':'x','op':'=','rvalue':'value'}}").getBindableClauses().isEmpty());
        Assert.assertTrue(getq("{'$not':{'field':'x','regex':'value'}}").getBindableClauses().isEmpty());
        Assert.assertTrue(getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'x','op':'=','rvalue':'value'}]}").getBindableClauses().isEmpty());
        Assert.assertTrue(getq("{'$not':{'array':'x','elemMatch':{'field':'x','op':'=','rvalue':'value'}}}").getBindableClauses().isEmpty());
    }

    @Test
    public void bindableFieldsTest() throws Exception {
        QueryExpression q = getq("{'field':'x','op':'=','rfield':'y'}");
        List<QueryInContext> l = q.getBindableClauses();
        System.out.println(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(q == l.get(0).getQuery());

        q = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'x','op':'=','rfield':'y'}]}");
        l = q.getBindableClauses();
        System.out.println(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(((NaryLogicalExpression) q).getQueries().get(1) == l.get(0).getQuery());

        q = getq("{'$not':{'array':'x','elemMatch':{'field':'x','op':'=','rfield':'y'}}}");
        l = q.getBindableClauses();
        System.out.println(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(((ArrayMatchExpression) ((UnaryLogicalExpression) q).getQuery()).getElemMatch()
                == l.get(0).getQuery());
        Assert.assertEquals("x.*", l.get(0).getContext().toString());

        q = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'x','op':'=','rfield':'y'},{'field':'x','op':'$in','rfield':'y'}]}");
        l = q.getBindableClauses();
        System.out.println(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(((NaryLogicalExpression) q).getQueries().get(1) == l.get(0).getQuery());
        Assert.assertTrue(((NaryLogicalExpression) q).getQueries().get(2) == l.get(1).getQuery());
    }

    @Test
    public void bindErr_value_comparison() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("x"));
        try {
            getq("{'field':'x','op':'=','rvalue':'value'}").bind(bindingInfo, paths);
            Assert.fail();
        } catch (Error e) {
        }

    }

    @Test
    public void bindErr_not_value_comparison() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("x"));
        try {
            getq("{'$not':{'field':'x','op':'=','rvalue':'value'}}").bind(bindingInfo, paths);
            Assert.fail();
        } catch (Error e) {
        }

    }

    @Test
    public void bindErr_field_comparison() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("x"));
        paths.add(new Path("y"));
        try {
            getq("{'field':'x','op':'=','rfield':'y'}").bind(bindingInfo, paths);
            Assert.fail();
        } catch (Error e) {
        }
    }

    @Test
    public void simplebind() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("x"));
        QueryExpression q = getq("{'field':'x','op':'=','rfield':'y'}");
        QueryExpression newq = q.bind(bindingInfo, paths);
        Assert.assertTrue(q != newq);
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._eq, ((ValueComparisonExpression) newq).getOp());
        Assert.assertEquals(1, bindingInfo.size());
        Assert.assertTrue(((ValueBinding) bindingInfo.get(0)).getValue() == ((ValueComparisonExpression) newq).getRvalue());
        Assert.assertTrue(bindingInfo.get(0).getOriginalQuery() == q);
        Assert.assertTrue(bindingInfo.get(0).getBoundQuery() == newq);
        Assert.assertEquals("x", bindingInfo.get(0).getField().toString());

        String newValue = "blah";
        ((ValueBinding) bindingInfo.get(0)).getValue().setValue(newValue);
        Assert.assertEquals(newValue, ((ValueBinding) bindingInfo.get(0)).getValue().getValue());
        Assert.assertEquals(newValue, ((ValueComparisonExpression) newq).getRvalue().getValue());
    }

    @Test
    public void arrayContainsBind() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("x"));
        QueryExpression q = getq("{'field':'x','op':'$in','rfield':'y'}");
        QueryExpression newq = q.bind(bindingInfo, paths);
        Assert.assertTrue(q != newq);
        Assert.assertTrue(newq instanceof ArrayContainsExpression);
        Assert.assertEquals("y", ((ArrayContainsExpression) newq).getArray().toString());
        Assert.assertEquals(ContainsOperator._all, ((ArrayContainsExpression) newq).getOp());
        Assert.assertEquals(1, bindingInfo.size());
        Assert.assertTrue(((ValueBinding) bindingInfo.get(0)).getValue() == ((ArrayContainsExpression) newq).getValues().get(0));
        Assert.assertTrue(bindingInfo.get(0).getOriginalQuery() == q);
        Assert.assertTrue(bindingInfo.get(0).getBoundQuery() == newq);
        Assert.assertEquals("x", bindingInfo.get(0).getField().toString());

        String newValue = "blah";
        ((ValueBinding) bindingInfo.get(0)).getValue().setValue(newValue);
        Assert.assertEquals(newValue, ((ValueBinding) bindingInfo.get(0)).getValue().getValue());
        Assert.assertEquals(newValue, ((ArrayContainsExpression) newq).getValues().get(0).getValue());
    }

    @Test
    public void naryValueBind() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("y"));
        QueryExpression q = getq("{'field':'x','op':'$in','rfield':'y'}");
        QueryExpression newq = q.bind(bindingInfo, paths);
        Assert.assertTrue(q != newq);
        Assert.assertTrue(newq instanceof NaryValueRelationalExpression);
        Assert.assertEquals("x", ((NaryValueRelationalExpression) newq).getField().toString());
        Assert.assertEquals(NaryRelationalOperator._in, ((NaryValueRelationalExpression) newq).getOp());
        Assert.assertEquals(1, bindingInfo.size());
        Assert.assertTrue(((ListBinding) bindingInfo.get(0)).getList() == ((NaryValueRelationalExpression) newq).getValues());
        Assert.assertTrue(bindingInfo.get(0).getOriginalQuery() == q);
        Assert.assertTrue(bindingInfo.get(0).getBoundQuery() == newq);
        Assert.assertEquals("y", bindingInfo.get(0).getField().toString());

        List<Value> newValue = new ArrayList<>();
        ((ListBinding) bindingInfo.get(0)).getList().setList(newValue);
        Assert.assertEquals(newValue, ((ListBinding) bindingInfo.get(0)).getList());
        Assert.assertEquals(newValue, ((NaryValueRelationalExpression) newq).getValues());
    }

    @Test
    public void deepbind() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("a.*.x"));
        ArrayMatchExpression q = (ArrayMatchExpression) getq("{'array':'a','elemMatch':{'field':'x','op':'=','rfield':'y'}}");
        ArrayMatchExpression newq = (ArrayMatchExpression) q.bind(bindingInfo, paths);

        Assert.assertTrue(q != newq);
        Assert.assertTrue(newq.getElemMatch() instanceof ValueComparisonExpression);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq.getElemMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._eq, ((ValueComparisonExpression) newq.getElemMatch()).getOp());
        Assert.assertEquals(1, bindingInfo.size());
        Assert.assertTrue(((ValueBinding) bindingInfo.get(0)).getValue() == ((ValueComparisonExpression) newq.getElemMatch()).getRvalue());
        Assert.assertTrue(bindingInfo.get(0).getOriginalQuery() == q.getElemMatch());
        Assert.assertTrue(bindingInfo.get(0).getBoundQuery() == newq.getElemMatch());
        Assert.assertEquals("a.*.x", bindingInfo.get(0).getField().toString());

        String newValue = "blah";
        ((ValueBinding) bindingInfo.get(0)).getValue().setValue(newValue);
        Assert.assertEquals(newValue, ((ValueComparisonExpression) newq.getElemMatch()).getRvalue().getValue());
    }

    @Test
    public void op_inversion_gt() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("a.*.x"));

        ArrayMatchExpression q = (ArrayMatchExpression) getq("{'array':'a','elemMatch':{'field':'x','op':'>','rfield':'y'}}");
        ArrayMatchExpression newq = (ArrayMatchExpression) q.bind(bindingInfo, paths);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq.getElemMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._lt, ((ValueComparisonExpression) newq.getElemMatch()).getOp());

    }

    @Test
    public void op_inversion_gte() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("a.*.x"));

        ArrayMatchExpression q = (ArrayMatchExpression) getq("{'array':'a','elemMatch':{'field':'x','op':'>=','rfield':'y'}}");
        ArrayMatchExpression newq = (ArrayMatchExpression) q.bind(bindingInfo, paths);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq.getElemMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._lte, ((ValueComparisonExpression) newq.getElemMatch()).getOp());

    }

    @Test
    public void op_inversion_lt() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("a.*.x"));

        ArrayMatchExpression q = (ArrayMatchExpression) getq("{'array':'a','elemMatch':{'field':'x','op':'<','rfield':'y'}}");
        ArrayMatchExpression newq = (ArrayMatchExpression) q.bind(bindingInfo, paths);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq.getElemMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._gt, ((ValueComparisonExpression) newq.getElemMatch()).getOp());

    }

    @Test
    public void op_inversion_lte() throws Exception {
        Set<Path> paths = new HashSet<>();
        List<FieldBinding> bindingInfo = new ArrayList<>();
        paths.add(new Path("a.*.x"));

        ArrayMatchExpression q = (ArrayMatchExpression) getq("{'array':'a','elemMatch':{'field':'x','op':'<=','rfield':'y'}}");
        ArrayMatchExpression newq = (ArrayMatchExpression) q.bind(bindingInfo, paths);
        Assert.assertEquals("y", ((ValueComparisonExpression) newq.getElemMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._gte, ((ValueComparisonExpression) newq.getElemMatch()).getOp());
    }
}
