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
package com.redhat.lightblue.assoc.qrew;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.UnaryLogicalOperator;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;

public class QueryRewriterTest {

    private final QueryRewriter rw=new QueryRewriter();

    private static QueryExpression json(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    private QueryExpression inq(String field,String...values) {
        List<Value> v=new ArrayList<>();
        for(String x:values)
            v.add(new Value(x));
        return new NaryRelationalExpression(new Path(field),
                                            NaryRelationalOperator._in,v);
    }

    private QueryExpression ninq(String field,String...values) {
        List<Value> v=new ArrayList<>();
        for(String x:values)
            v.add(new Value(x));
        return new NaryRelationalExpression(new Path(field),
                                            NaryRelationalOperator._not_in,v);
    }

    private QueryExpression vcmp(String field,String op,String value) {
        return new ValueComparisonExpression(new Path(field),
                                             BinaryComparisonOperator.fromString(op),
                                             new Value(value));
    }

    private QueryExpression fcmp(String field,String op,String rfield) {
        return new FieldComparisonExpression(new Path(field),
                                             BinaryComparisonOperator.fromString(op),
                                             new Path(rfield));
    }

    private QueryExpression _and(QueryExpression...q) {
        return new NaryLogicalExpression(NaryLogicalOperator._and,q);
    }

    private QueryExpression _or(QueryExpression...q) {
        return new NaryLogicalExpression(NaryLogicalOperator._or,q);
    }

    private QueryExpression _not(QueryExpression q) {
        return new UnaryLogicalExpression(UnaryLogicalOperator._not,q);
    }


    @Test
    public void testCombineInsInOr() throws Exception {
        QueryExpression in1=inq("f2","1","2","3");
        QueryExpression in2=inq("f2","2","3","4");
        QueryExpression v1=vcmp("f1","=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression q=_or(v1,v2,in1,in2);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        // There should be one in, two value comparisons
        Assert.assertTrue(exists(newq,v1));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertFalse(exists(newq,in1));
        Assert.assertFalse(exists(newq,in2));
        Assert.assertTrue(exists(newq,inq("f2","1","2","3","4")));
    }

   @Test
    public void testCombineNinsInAnd() throws Exception {
        QueryExpression in1=ninq("f2","1","2","3");
        QueryExpression in2=ninq("f2","2","3","4");
        QueryExpression v1=vcmp("f1","=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression q=_and(v1,v2,in1,in2);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertTrue(exists(newq,v1));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertFalse(exists(newq,in1));
        Assert.assertFalse(exists(newq,in2));
        Assert.assertTrue(exists(newq,ninq("f2","1","2","3","4")));
    }

    @Test
    public void testCombineInsNinsInOr() throws Exception {
        QueryExpression in1=inq("f2","1","2","3");
        QueryExpression in2=inq("f2","2","3","4");
        QueryExpression nin1=ninq("f2","1","2","3");
        QueryExpression nin2=ninq("f2","2","3","4");
        QueryExpression v1=vcmp("f1","=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression q=_or(v1,v2,in1,in2,nin1,nin2);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertTrue(exists(newq,v1));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertFalse(exists(newq,in1));
        Assert.assertFalse(exists(newq,in2));
        Assert.assertTrue(exists(newq,nin1));
        Assert.assertTrue(exists(newq,nin2));
        Assert.assertFalse(exists(newq,ninq("f2","1","2","3","4")));
        Assert.assertTrue(exists(newq,inq("f2","1","2","3","4")));
    }

     @Test
    public void testExtendInsNinsInOr() throws Exception {
        QueryExpression in1=inq("f2","1","2","3");
        QueryExpression v1=vcmp("f2","=","4");
        QueryExpression q=_or(v1,in1);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertFalse(exists(newq,v1));
        Assert.assertFalse(exists(newq,in1));
        Assert.assertTrue(exists(newq,inq("f2","1","2","3","4")));
    }

   @Test
    public void testCombineORsToIn() throws Exception {
        QueryExpression v1=vcmp("f1","=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression v3=vcmp("f1","=","2");
        QueryExpression v4=vcmp("f1","=","3");
        QueryExpression v5=vcmp("f1","!=","3");
        QueryExpression q=_or(v1,v2,v3,v4,v5);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertFalse(exists(newq,v1));
        Assert.assertFalse(exists(newq,v3));
        Assert.assertFalse(exists(newq,v4));
        Assert.assertTrue(exists(newq,v5));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertTrue(exists(newq,inq("f1","1","2","3")));
    }

   @Test
    public void testNestedAnd() throws Exception {
        QueryExpression v1=vcmp("f1","=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression v3=vcmp("f1","=","2");
        QueryExpression v4=vcmp("f2","=","3");
        QueryExpression nestedand=_and(v3,v4);
        QueryExpression q=_and(v1,v2,nestedand);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertTrue(exists(newq,v1));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertTrue(exists(newq,v3));
        Assert.assertTrue(exists(newq,v4));
    }


    @Test
    public void testCombineANDsToNin() throws Exception {
        QueryExpression v1=vcmp("f1","!=","1");
        QueryExpression v2= vcmp("f3","!=","2");
        QueryExpression v3=vcmp("f1","!=","2");
        QueryExpression v4=vcmp("f1","!=","3");
        QueryExpression v5=vcmp("f1","!=","3");
        QueryExpression q=_and(v1,v2,v3,v4,v5);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertFalse(exists(newq,v1));
        Assert.assertFalse(exists(newq,v3));
        Assert.assertFalse(exists(newq,v4));
        Assert.assertFalse(exists(newq,v5));
        Assert.assertTrue(exists(newq,v2));
        Assert.assertTrue(exists(newq,ninq("f1","1","2","3")));
    }

    @Test
    public void testEliminateNot() throws Exception {
        QueryExpression v1=_not(vcmp("f1","!=","1"));
        QueryExpression v2= _not(vcmp("f2","!=","2"));
        QueryExpression v3= _not(vcmp("f3",">","3"));
        QueryExpression v4= _not(fcmp("f4",">","f5"));
        QueryExpression q=_and(v1,v2,v3,v4);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertTrue(exists(newq,vcmp("f1","=","1")));
        Assert.assertTrue(exists(newq,vcmp("f2","=","2")));
        Assert.assertTrue(exists(newq,vcmp("f3","<=","3")));
        Assert.assertTrue(exists(newq,fcmp("f4","<=","f5")));
    }

    @Test
    public void testEliminateNotNot() throws Exception {
        QueryExpression v1=_not(new RegexMatchExpression(new Path("f1"),"X",false,false,false,false));
        QueryExpression q=_not(v1);

        QueryExpression newq=rw.rewrite(q);
        System.out.println(q+"  ->\n"+newq);
        Assert.assertTrue(newq instanceof RegexMatchExpression);
    }

    @Test
    public void testEliminateNotOr() throws Exception {
        QueryExpression v1=vcmp("f1","!=","1");
        QueryExpression v2=vcmp("f2","<","2");
        QueryExpression qor=_or(v1,v2);
        QueryExpression qnot=_not(qor);

        QueryExpression newq=rw.rewrite(qnot);
        System.out.println(qnot+"  ->\n"+newq);
        Assert.assertTrue( ((NaryLogicalExpression)newq).getOp()==NaryLogicalOperator._and);
        Assert.assertTrue(exists(newq,vcmp("f1","=","1")));
        Assert.assertTrue(exists(newq,vcmp("f2",">=","2")));
    }

    /**
     * Returns if w exists in q
     */
    private boolean exists(QueryExpression q,QueryExpression w) {
        if(equals(q,w)) 
            return true;
        else {
            if(q instanceof UnaryLogicalExpression) {
                return exists( ((UnaryLogicalExpression)q).getQuery(),w);
            } else if(q instanceof NaryLogicalExpression) {
                for(QueryExpression qq:((NaryLogicalExpression)q).getQueries())
                    if(exists(qq,w))
                        return true;
            } else if(q instanceof ArrayMatchExpression) {
                return exists( ((ArrayMatchExpression)q).getElemMatch(),w);
            } 
        }
        return false;
    }

    private boolean equals(QueryExpression q,QueryExpression w) {
        if(q.getClass().equals(w.getClass())) {
            if (q instanceof ValueComparisonExpression) {
                return equals((ValueComparisonExpression)q,(ValueComparisonExpression)w);
            } else if (q instanceof FieldComparisonExpression) {
                return equals((FieldComparisonExpression)q,(FieldComparisonExpression)w);
            } else if (q instanceof RegexMatchExpression) {
                return equals((RegexMatchExpression)q,(RegexMatchExpression)w);
            } else if (q instanceof NaryRelationalExpression) {
                return equals((NaryRelationalExpression)q,(NaryRelationalExpression)w);
            } else if (q instanceof UnaryLogicalExpression) {
                return equals((UnaryLogicalExpression)q,(UnaryLogicalExpression)w);
            } else if (q instanceof NaryLogicalExpression) {
                return equals((NaryLogicalExpression)q,(NaryLogicalExpression)w);
            } else if (q instanceof ArrayContainsExpression) {
                return equals((ArrayContainsExpression)q,(ArrayContainsExpression)w);
            } else if (q instanceof ArrayMatchExpression) {
                return equals((ArrayMatchExpression)q,(ArrayMatchExpression)w);
            }
        }
        return false;
    }

    private boolean equals(ValueComparisonExpression q,
                           ValueComparisonExpression w) {
        return q.getField().equals(w.getField())&&
            q.getOp().equals(w.getOp())&&
            q.getRvalue().equals(w.getRvalue());
    }

    private boolean equals(FieldComparisonExpression q,
                           FieldComparisonExpression w) {
        return q.getField().equals(w.getField())&&
            q.getOp().equals(w.getOp())&&
            q.getRfield().equals(w.getRfield());
    }
    private boolean equals(RegexMatchExpression q,
                           RegexMatchExpression w) {
        return q.getField().equals(w.getField())&&
            q.getRegex().equals(w.getRegex())&&
            q.isCaseInsensitive()==w.isCaseInsensitive()&&
            q.isMultiline()==w.isMultiline()&&
            q.isExtended()==w.isExtended()&&
            q.isDotAll()==w.isDotAll();
    }
    private boolean equals(UnaryLogicalExpression q,
                           UnaryLogicalExpression w) {
        return q.getOp()==w.getOp()&&
            equals(q.getQuery(),w.getQuery());
    }
    private boolean equals(NaryRelationalExpression q,
                           NaryRelationalExpression w) {
        return q.getField().equals(w.getField())&&
            q.getOp()==w.getOp()&&
            q.getValues().containsAll(w.getValues())&&
            w.getValues().containsAll(q.getValues());
    }
    private boolean equals(NaryLogicalExpression q,
                           NaryLogicalExpression w) {
        if(q.getOp().equals(w.getOp())) {
            
            for(QueryExpression qq:q.getQueries()) {
                boolean found=false;
                for(QueryExpression ww:w.getQueries())
                    if(equals(qq,ww)) {
                        found=true;
                        break;
                    }
                if(!found)
                    break;
            }
            return q.getQueries().size()==w.getQueries().size();
        }
        return false;
    }
    private boolean equals(ArrayContainsExpression q,
                           ArrayContainsExpression w) {
        return q.getArray().equals(w.getArray())&&
            q.getOp().equals(w.getOp())&&
            q.getValues().containsAll(w.getValues())&&
            w.getValues().containsAll(q.getValues());
    }
    private boolean equals(ArrayMatchExpression q,
                           ArrayMatchExpression w) {
        return q.getArray().equals(w.getArray())&&
            equals(q.getElemMatch(),w.getElemMatch());
    }
}
