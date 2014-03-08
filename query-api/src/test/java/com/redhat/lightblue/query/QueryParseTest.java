package com.redhat.lightblue.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

import java.math.BigInteger;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import java.lang.reflect.Method;

public class QueryParseTest {

    final String valueQuery1 = "{\"field\":\"x.y.z\", \"op\":\"$eq\", \"rvalue\":\"string\"}";
    final String valueQuery2 = "{\"field\":\"x.y.1\", \"op\":\"$gte\", \"rvalue\":1}";
    final String valueQuery3 = "{\"field\":\"x.y.1\", \"op\":\"$neq\", \"rvalue\":12345678901234567890}";
    final String valueQuery4 = "{\"field\":\"x.y.1\", \"op\":\"$eq\", \"rvalue\":true}";
    final String valueQuery5 = "{\"field\":\"x.y.-1.x\", \"op\":\"$neq\", \"rvalue\":12345678901234567890123456789123456789.123456}";

    final String fieldQuery1 = "{\"field\":\"x.-1.y\", \"op\":\"$eq\", \"rfield\":\"y.z.-1\"}";
    final String fieldQuery2 = "{\"field\":\"x.1.y\", \"op\":\"$neq\", \"rfield\":\"y\"}";

    final String naryQuery1 = "{\"field\":\"x.y\", \"op\":\"$in\", \"values\":[1,2,3,4,5]}";
    final String naryQuery2 = "{\"field\":\"x.y.x\", \"op\":\"$nin\", \"values\":[\"x\",\"y\",\"z\"]}";

    final String regexQuery1 = "{\"field\":\"x.y\", \"regex\":\"*pat*\"}";
    final String regexQuery2 = "{\"field\":\"x.y\", \"regex\":\"*pat*\",\"case_insensitive\":true}";
    final String regexQuery3 = "{\"field\":\"x.y\", \"regex\":\"*pat*\",\"multiline\":true}";
    final String regexQuery4 = "{\"field\":\"x.y\", \"regex\":\"*pat*\",\"extended\":true}";
    final String regexQuery5 = "{\"field\":\"x.y\", \"regex\":\"*pat*\",\"dotall\":true}";

    final String unaryQuery1 = "{ \"$not\": " + valueQuery1 + "}";
    final String unaryQuery2 = "{ \"$not\": " + regexQuery1 + "}";

    final String naryLogQueryAnd1 = "{ \"$and\" : [" + valueQuery1 + "," + fieldQuery1 + "," + naryQuery1 + "," + unaryQuery1 + "]}";
    final String naryLogQueryOr1 = "{ \"$or\" : [" + valueQuery1 + "," + fieldQuery1 + "," + naryQuery1 + "," + unaryQuery1 + "]}";

    final String arrContains1 = "{\"array\":\"x.y\", \"contains\":\"$any\", \"values\":[1,2,3,4,5]}";
    final String arrContains2 = "{\"array\":\"x.y\", \"contains\":\"$any\", \"values\":[\"x\", \"y\"]}";
    final String arrContains3 = "{\"array\":\"x.y\", \"contains\":\"$all\", \"values\":[\"x\", \"y\"]}";
    final String arrContains4 = "{\"array\":\"x.y\", \"contains\":\"$none\", \"values\":[\"x\", \"y\"]}";
    final String arrContains5 = "{\"array\":\"x.y\", \"contains\":\"$invalid\", \"values\":[\"x\", \"y\"]}";

    final String arrMatch1 = "{\"array\":\"x.y\",\"elemMatch\":" + regexQuery1 + "}";

    interface NestedTest {
        public void test(QueryExpression x);
    }

    @Test
    public void testValueQueries() throws Exception {
        testValueComparisonExpression(valueQuery1, "x.y.z", BinaryComparisonOperator._eq, "string");
        testValueComparisonExpression(valueQuery2, "x.y.1", BinaryComparisonOperator._gte, new Integer(1));
        testValueComparisonExpression(valueQuery3, "x.y.1", BinaryComparisonOperator._neq, new BigInteger("12345678901234567890"));
        testValueComparisonExpression(valueQuery4, "x.y.1", BinaryComparisonOperator._eq, Boolean.TRUE);
        testValueComparisonExpression(valueQuery5, "x.y.-1.x", BinaryComparisonOperator._neq, new BigDecimal("12345678901234567890123456789123456789.123456"));
    }

    @Test
    public void testFieldQueries() throws Exception {
        testFieldComparisonExpression(fieldQuery1, "x.-1.y", BinaryComparisonOperator._eq, "y.z.-1");
        testFieldComparisonExpression(fieldQuery2, "x.1.y", BinaryComparisonOperator._neq, "y");
    }

    @Test
    public void testNaryQueries() throws Exception {
        testNaryRelationalExpression(naryQuery1, "x.y", NaryRelationalOperator._in, 1, 2, 3, 4, 5);
        testNaryRelationalExpression(naryQuery2, "x.y.x", NaryRelationalOperator._not_in, "x", "y", "z");
    }

    @Test
    public void testRegexQueries() throws Exception {
        testRegexQuery(regexQuery1, "x.y", "*pat*", false, false, false, false);
        testRegexQuery(regexQuery2, "x.y", "*pat*", true, false, false, false);
        testRegexQuery(regexQuery3, "x.y", "*pat*", false, true, false, false);
        testRegexQuery(regexQuery4, "x.y", "*pat*", false, false, true, false);
        testRegexQuery(regexQuery5, "x.y", "*pat*", false, false, false, true);
    }

    @Test
    public void testUnaries() throws Exception {
        testUnaryQuery(unaryQuery1, factoryU1NestedTest());
        testUnaryQuery(unaryQuery2, new NestedTest() {
            @Override
            public void test(QueryExpression x) {
                asserts((RegexMatchExpression) x, "x.y", "*pat*", false, false, false, false);
            }
        });
    }

    @Test
    public void testNaries() throws Exception {
        NestedTest[] tests = new NestedTest[]{
            new NestedTest() {
                @Override
                public void test(QueryExpression x) {
                    asserts((ValueComparisonExpression) x, "x.y.z", BinaryComparisonOperator._eq, "string");
                }
            },
            new NestedTest() {
                @Override
                public void test(QueryExpression x) {
                    asserts((FieldComparisonExpression) x, "x.-1.y", BinaryComparisonOperator._eq, "y.z.-1");
                }
            },
            new NestedTest() {
                @Override
                public void test(QueryExpression x) {
                    asserts((NaryRelationalExpression) x, "x.y", NaryRelationalOperator._in, 1, 2, 3, 4, 5);
                }
            },
            new NestedTest() {
                @Override
                public void test(QueryExpression x) {
                    asserts((UnaryLogicalExpression) x, factoryU1NestedTest());
                }
            }};

        testNaryQuery(naryLogQueryAnd1, tests);
        testNaryQuery(naryLogQueryOr1, tests);
    }

    @Test
    public void testArrContains() throws Exception {
        testArrContains(arrContains1, "x.y", ContainsOperator._any, 1, 2, 3, 4, 5);
        testArrContains(arrContains2, "x.y", ContainsOperator._any, "x", "y");
        testArrContains(arrContains3, "x.y", ContainsOperator._all, "x", "y");
        testArrContains(arrContains4, "x.y", ContainsOperator._none, "x", "y");
        try {
            testArrContains(arrContains5, "x.y", null);
            Assert.fail("invalid contains operator should fail");
        } catch (Throwable t) {
            // valid
        }
    }

    @Test
    public void testArrMatch() throws Exception {
        testArrMatch(arrMatch1, "x.y", new NestedTest() {
            @Override
            public void test(QueryExpression x) {
                asserts((RegexMatchExpression) x, "x.y", "*pat*", false, false, false, false);
            }
        });
    }

    /**
     * Check the behavior of RegexMatchExpression in case of bad formated input.
     * In this scenario, 'regex' is missing, expecting the system to raise a
     * Error Exception
     *
     * PS1:RegexMatchExpression (and other classes that extends QueryExpression)
     * hide their fromJson (shadowing), due it is static method and it can't
     * override (so this can lead to a problem when someone expects to rely in
     * the polymorphism using instance). But the shadowing methods use to change
     * the return type and the method's parameter type as well. PS2:The Error
     * exception is not reachable so far because the previous validations made
     * in the chain of fromJson calls , maybe removed in a future.
     *
     * @throws Exception
     */
    @Test
    public void testRegexMatchExpressionFromJsonMethodExecptionField() throws Exception {
        String withoutFieldString = "{\"missing\":\"x.y\",\"regex\":\"*pat*\",\"dotall\":true}";
        abstractTestRegexMatchExpressionFromJsonMethodExecption(withoutFieldString);
    }

    /**
     * Check the behavior of RegexMatchExpression in case of bad formated input.
     * In this scenario, 'regex' is missing, expecting the system to raise a
     * Error Exception
     *
     * PS1:RegexMatchExpression (and other classes that extends QueryExpression)
     * hide their fromJson (shadowing), due it is static method and it can't
     * override (so this can lead to a problem when someone expects to rely in
     * the polymorphism using instance). But the shadowing methods use to change
     * the return type and the method's parameter type as well. PS2:The Error
     * exception is not reachable so far because the previous validations made
     * in the chain of fromJson calls , maybe removed in a future.
     *
     * @throws Exception
     */
    @Test
    public void testRegexMatchExpressionFromJsonMethodExecptionRegex() throws Exception {
        String withoutRegexString = "{\"field\":\"x.y\",\"missing\":\"*pat*\",\"dotall\":true}";
        abstractTestRegexMatchExpressionFromJsonMethodExecption(withoutRegexString);
    }

    private void abstractTestRegexMatchExpressionFromJsonMethodExecption(String badInput) throws Exception {
        ObjectNode node = (ObjectNode) JsonUtils.json(badInput);

        /*
         I would use the ExpectedException but due the 
         com.fasterxml.jackson.databind.ObjectNode implementation, the json 
         returned from toString() uses some differnt enconding characters 
         which would make very difficult to maintain the test, so I will just 
         check the contents of the Error instance.
         Also the badInput need to be kind of trim()
         */
        try {
            RegexMatchExpression query = RegexMatchExpression.fromJson(node);
            Assert.assertNull("Invocation must throw an execption", query);
        } catch (Error e) {
            Assert.assertEquals("query-api:InvalidRegexExpression", e.getErrorCode());
            Assert.assertEquals(badInput, e.getMsg());
        }

    }

    @Test
    public void testArrayContainsExpressionFromJsonMethodExecptionArray() throws Exception {
        String str = "{\"missing\":\"x.y\",\"contains\":\"$invalid\",\"values\":[\"x\",\"y\"]}";
        abstractTestArrayContainsExpressionFromJsonMethodExecption(str);
    }

    @Test
    public void testArrayContainsExpressionFromJsonMethodExecptionContains() throws Exception {
        String str = "{\"array\":\"x.y\",\"missing\":\"$invalid\",\"values\":[\"x\",\"y\"]}";
        abstractTestArrayContainsExpressionFromJsonMethodExecption(str);
    }

    @Test
    public void testArrayContainsExpressionFromJsonMethodExecptionValues() throws Exception {
        String str = "{\"array\":\"x.y\",\"contains\":\"$invalid\",\"missing\":[\"x\",\"y\"]}";
        abstractTestArrayContainsExpressionFromJsonMethodExecption(str);
    }

    private void abstractTestArrayContainsExpressionFromJsonMethodExecption(String badInput) throws Exception {
        ObjectNode node = (ObjectNode) JsonUtils.json(badInput);

        try {
            ArrayContainsExpression query = ArrayContainsExpression.fromJson(node);
            Assert.assertNull("Invocation must throw an execption", query);
        } catch (Error e) {
            Assert.assertEquals("query-api:InvalidArrayComparisonExpression", e.getErrorCode());
            Assert.assertEquals(badInput, e.getMsg());
        }

    }

    private void testValueComparisonExpression(String q,
                                               String field,
                                               BinaryComparisonOperator op,
                                               Object value)
            throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof ValueComparisonExpression);
        asserts((ValueComparisonExpression) query, field, op, value);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private static void asserts(ValueComparisonExpression x,
                                String field,
                                BinaryComparisonOperator op,
                                Object value) {
        Assert.assertEquals(field, x.getField().toString());
        Assert.assertEquals(op, x.getOp());
        Assert.assertTrue(value.getClass().equals(x.getRvalue().getValue().getClass()));
        Assert.assertEquals(value.toString(), x.getRvalue().getValue().toString());
    }

    private void testFieldComparisonExpression(String q,
                                               String field,
                                               BinaryComparisonOperator op,
                                               String rfield)
            throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof FieldComparisonExpression);
        asserts((FieldComparisonExpression) query, field, op, rfield);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private void asserts(FieldComparisonExpression x, String field, BinaryComparisonOperator op, String rfield) {
        Assert.assertEquals(field, x.getField().toString());
        Assert.assertEquals(op, x.getOp());
        Assert.assertEquals(rfield, x.getRfield().toString());
    }

    private void testNaryRelationalExpression(String q,
                                              String field,
                                              NaryRelationalOperator op,
                                              Object... value)
            throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof NaryRelationalExpression);
        asserts((NaryRelationalExpression) query, field, op, value);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private static void asserts(NaryRelationalExpression x, String field, NaryRelationalOperator op, Object... value) {
        Assert.assertEquals(field, x.getField().toString());
        Assert.assertEquals(op, x.getOp());
        Assert.assertEquals(value.length, x.getValues().size());
        for (int i = 0; i < value.length; i++) {
            Assert.assertEquals(value[i].getClass(), x.getValues().get(i).getValue().getClass());
        }
    }

    private void testRegexQuery(String q,
                                String field,
                                String regex,
                                boolean c,
                                boolean m,
                                boolean x,
                                boolean d)
            throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof RegexMatchExpression);
        RegexMatchExpression mx = (RegexMatchExpression) query;
        asserts(mx, field, regex, c, m, x, d);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private static void asserts(RegexMatchExpression x,
                                String field,
                                String regex,
                                boolean c,
                                boolean m,
                                boolean ox,
                                boolean d) {
        Assert.assertEquals(field, x.getField().toString());
        Assert.assertEquals(regex, x.getRegex());
        Assert.assertEquals(c, x.isCaseInsensitive());
        Assert.assertEquals(m, x.isMultiline());
        Assert.assertEquals(ox, x.isExtended());
        Assert.assertEquals(d, x.isDotAll());
    }

    private void testUnaryQuery(String q, NestedTest t) throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof UnaryLogicalExpression);
        UnaryLogicalExpression x = (UnaryLogicalExpression) query;
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private static void asserts(UnaryLogicalExpression x, NestedTest t) {
        t.test(x.getQuery());
    }

    private void testNaryQuery(String q, NestedTest... t) throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof NaryLogicalExpression);
        NaryLogicalExpression x = (NaryLogicalExpression) query;
        List<QueryExpression> queries = x.getQueries();
        Assert.assertEquals(t.length, queries.size());
        for (int i = 0; i < t.length; i++) {
            t[i].test(queries.get(i));
        }
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private void testArrContains(String q, String field, ContainsOperator op, Object... value) throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof ArrayContainsExpression);
        asserts((ArrayContainsExpression) query, field, op, value);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private void asserts(ArrayContainsExpression x, String field, ContainsOperator op, Object... value) {
        Assert.assertEquals(field, x.getArray().toString());
        Assert.assertEquals(op, x.getOp());
        Assert.assertEquals(value.length, x.getValues().size());
        for (int i = 0; i < value.length; i++) {
            Assert.assertEquals(value[i].getClass(), x.getValues().get(i).getValue().getClass());
            Assert.assertEquals(value[i].toString(), x.getValues().get(i).getValue().toString());
        }
    }

    private void testArrMatch(String q, String field, NestedTest t) throws Exception {
        QueryExpression query = QueryExpression.fromJson(JsonUtils.json(q));
        Assert.assertTrue(query instanceof ArrayMatchExpression);
        asserts((ArrayMatchExpression) query, field, t);
        JSONAssert.assertEquals(q, QueryExpression.fromJson(JsonUtils.json(q)).toString(), false);
    }

    private void asserts(ArrayMatchExpression x, String field, NestedTest t) {
        Assert.assertEquals(field, x.getArray().toString());
        t.test(x.getElemMatch());
    }

    private NestedTest factoryU1NestedTest() {
        return new NestedTest() {
            public void test(QueryExpression x) {
                asserts((ValueComparisonExpression) x, "x.y.z", BinaryComparisonOperator._eq, "string");
            }
        };
    }
}
