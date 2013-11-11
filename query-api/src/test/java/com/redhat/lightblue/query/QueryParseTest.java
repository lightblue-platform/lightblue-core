package com.redhat.lightblue.query;

import java.math.BigInteger;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.skyscreamer.jsonassert.JSONAssert;

import com.redhat.lightblue.util.JsonUtils;

public class QueryParseTest {
    
    final String fieldQuery1="{\"field\":\"x.y.z\", \"op\":\"$eq\", \"rvalue\":\"string\"}";
    final String fieldQuery2="{\"field\":\"x.y.[1]\", \"op\":\"$gte\", \"rvalue\":1}";
    final String fieldQuery3="{\"field\":\"x.y.[1]\", \"op\":\"$neq\", \"rvalue\":12345678901234567890}";
    final String fieldQuery4="{\"field\":\"x.y.[1]\", \"op\":\"$eq\", \"rvalue\":true}";
    final String fieldQuery5="{\"field\":\"x.y.[1]\", \"op\":\"$neq\", \"rvalue\":12345678901234567890123456789123456789.123456}";

    @Test
    public void testQuery1() throws Exception {
        testValueComparisonExpression(fieldQuery1,"x.y.z",BinaryComparisonOperator._eq,"string");
    }

    @Test
    public void testQuery2() throws Exception {
        testValueComparisonExpression(fieldQuery2,"x.y.[1]",BinaryComparisonOperator._gte,new Integer(1));
    }

    @Test
    public void testQuery3() throws Exception {
        testValueComparisonExpression(fieldQuery3,"x.y.[1]",BinaryComparisonOperator._neq,new BigInteger("12345678901234567890"));
    }

    @Test
    public void testQuery4() throws Exception {
        testValueComparisonExpression(fieldQuery4,"x.y.[1]",BinaryComparisonOperator._eq,Boolean.TRUE);
    }

    @Test
    public void testQuery5() throws Exception {
        testValueComparisonExpression(fieldQuery5,"x.y.[1]",BinaryComparisonOperator._neq,new BigDecimal("12345678901234567890123456789123456789.123456"));
    }

   @Test
    public void convertTest() throws Exception {
       JSONAssert.assertEquals(fieldQuery1,QueryExpression.fromJson(JsonUtils.json(fieldQuery1)).toString(),false);
       JSONAssert.assertEquals(fieldQuery2,QueryExpression.fromJson(JsonUtils.json(fieldQuery2)).toString(),false);
       JSONAssert.assertEquals(fieldQuery3,QueryExpression.fromJson(JsonUtils.json(fieldQuery3)).toString(),false);
       JSONAssert.assertEquals(fieldQuery4,QueryExpression.fromJson(JsonUtils.json(fieldQuery4)).toString(),false);
    }

    private void testValueComparisonExpression(String q,
                                               String field,
                                               BinaryComparisonOperator op,
                                               Object value) 
        throws Exception {
        QueryExpression query=QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof ValueComparisonExpression);
        ValueComparisonExpression x=(ValueComparisonExpression)query;
        Assert.assertEquals(field,x.getField().toString());
        Assert.assertEquals(op,x.getOp());
        Assert.assertTrue(value.getClass().equals(x.getRvalue().getValue().getClass()));
        Assert.assertEquals(value.toString(),x.getRvalue().getValue().toString());
    }
   
}
