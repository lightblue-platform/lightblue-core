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
package com.redhat.lightblue.metadata.rdbms.parser;

import com.redhat.lightblue.metadata.rdbms.impl.RDBMSPropertyParserImpl;
import com.redhat.lightblue.metadata.rdbms.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class RDBMSPropertyParserImplTest {

    RDBMSPropertyParserImpl cut;
    JSONMetadataParser p;

    static final String expectedJSON = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverField\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverField\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverField\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverField\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverField\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";

    @Before
    public void setup() {
        cut = new RDBMSPropertyParserImpl();
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms", cut);
        p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
    }

    @After
    public void tearDown() {
        cut = null;
        p = null;
    }

    @Test
    public void convertTest() throws IOException, JSONException {
        JsonNode parent = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");

        Operation o = new Operation();
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setField(new Path("pat"));
        inList.add(e);
        b.setInList(inList);
        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();

        Statement e1 = new Statement();
        e1.setSQL("SELECT * FROM TABLE1");
        e1.setType("select");
        expressionList.add(e1);

        For e2 = new For();
        e2.setLoopTimes(1);
        e2.setLoopCounterVariableName("i");
        ArrayList<Expression> expressions = new ArrayList<Expression>();
        expressions.add(e1);
        ForEach e3 = new ForEach();
        ArrayList<Expression> expressions1 = new ArrayList<Expression>();
        expressions1.add(e1);
        e3.setExpressions(expressions1);
        e3.setIterateOverField(new Path("j"));
        expressions.add(e3);
        e2.setExpressions(expressions);
        expressionList.add(e2);

        Conditional e4 = new Conditional();
        IfFieldCheckValue anIf = new IfFieldCheckValue();
        anIf.setOp("$eq");
        anIf.setField(new Path("abc"));
        anIf.setValue("123");
        e4.setIf(anIf);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);

        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, parent, r);

        System.out.println(parent.toString());
        assertEqualJson(expectedJSON, parent.toString());
    }

    private Operation duplicate(String name, Operation ori) {
        Operation o = new Operation();
        o.setBindings(ori.getBindings());
        o.setExpressionList(ori.getExpressionList());
        o.setName(name);
        return o;
    }

    private void assertEqualJson(String a, String b) {
        try {
            JSONAssert.assertEquals(a, b, false);
        } catch (JSONException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void parseTest() throws IOException {
        Object r = cut.parse("rdbms", p, JsonUtils.json(expectedJSON).get("rdbms"));
        JsonNode parent = p.newNode();
        cut.convert(p, parent, r);
        assertEqualJson(expectedJSON, parent.toString());
    }

    @Test
    public void parseWrongNodeName() throws IOException {
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("x", p, JsonUtils.json(expectedJSON).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseMissingOperation() throws IOException {
        String json = "{\"rdbms\":{}}";

        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseMissingOperationsExpressions() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"X\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseMissingOneOfOperations() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        Throwable error = null;
        Object r = null;
        try {
            r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (Throwable ex) {
            ex.printStackTrace();
            error = ex;
        } finally {
            Assert.assertNull(error);
            Assert.assertNotNull(r);
            Assert.assertTrue(r instanceof RDBMS);
        }
    }

    @Test
    public void convertMissingOneOfOperations() throws IOException {
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //With just In  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setField(Path.ANYPATH);
        inList.add(e);
        b.setInList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        o.setName("delete");
        r.setDelete(o);

        Throwable error = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (Throwable ex) {
            ex.printStackTrace();
            error = ex;
        } finally {
            Assert.assertNull(error);
            Assert.assertNotNull(r);
            Assert.assertTrue(r instanceof RDBMS);
        }
    }

    @Test
    public void convertAndParseBindingsFieldsReq() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //With just In  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        inList.add(e);
        b.setInList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));

        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex) {
            xe = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseBindingsJustIn() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //With just In  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setField(new Path("pat"));
        inList.add(e);
        b.setInList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsJustOut() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //With just Out  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setField(new Path("pat"));
        inList.add(e);
        b.setOutList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsNone() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //No  bindings
        Bindings b = null;

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void parseBindingsWrong() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseInOutMissingColumn() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"out\":[{\"field\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseBindingsBoth() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"field\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"field\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"field\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"field\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"field\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();

        //No  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setField(new Path("pat"));
        inList.add(e);
        b.setInList(inList);
        ArrayList<InOut> outList = new ArrayList<InOut>();
        InOut ou = new InOut();
        ou.setColumn("col1");
        ou.setField(new Path("pat1"));
        outList.add(ou);
        b.setOutList(outList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseStatement() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void parseMissingStatemtentsSQL() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseMissingStatemtentsType() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseForEachMissingIterateOverPath() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        ForEach forEach = new ForEach();
        ArrayList<Expression> el = new ArrayList<Expression>();
        Statement es = new Statement();
        es.setSQL("X");
        es.setType("select");
        el.add(es);
        forEach.setExpressions(el);
        Assert.assertEquals(el, forEach.getExpressions());
        expressionList.add(forEach);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex) {
            xe = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseForEachMissingExpressions() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$foreach\":{\"iterateOverField\":\"*\"}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"iterateOverField\":\"*\"}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"iterateOverField\":\"*\"}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"iterateOverField\":\"*\"}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"iterateOverField\":\"*\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        ForEach forEach = new ForEach();
        forEach.setIterateOverField(Path.ANYPATH);
        Assert.assertEquals(Path.ANYPATH, forEach.getIterateOverField());
        expressionList.add(forEach);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex) {
            xe = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForMissingLoopTimes() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$for\":{\"loopCounterVariableName\":\"2\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForLoopTimesNotInteger() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"A\",\"loopCounterVariableName\":\"2\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForMissingLoopCounterVariableName() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"1\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForMissingExpressions() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"2\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseWrongIfField() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"WRONG\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseWrongExpression() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"Wrong\":{}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseElseIf() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldCheckValue anIf = new IfFieldCheckValue();
        anIf.setOp("$eq");
        anIf.setField(new Path("abc"));
        anIf.setValue("123");
        e4.setIf(anIf);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        ArrayList<ElseIf> arrayList = new ArrayList<ElseIf>();
        final ElseIf elseIf = new ElseIf();
        elseIf.setIf(anIf);
        elseIf.setThen(then);
        arrayList.add(elseIf);
        e4.setElseIfList(arrayList);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);
        assertEqualJson(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void parseElseIfMissingIf() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$ifx\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseElseIfMissingThen() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$thenX\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseIfOr() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfOrAny(json);
    }

    @Test
    public void convertAndParseIfAny() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfOrAny(json);
    }

    private void convertAndParseIfOrAny(String json) throws IOException {
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfOr ifOr = new IfOr();
        IfFieldCheckValue anIf = new IfFieldCheckValue();
        anIf.setOp("$eq");
        anIf.setField(new Path("abc"));
        anIf.setValue("123");
        IfFieldCheckValue anIf2 = new IfFieldCheckValue();
        anIf2.setOp("$lt");
        anIf2.setField(new Path("abc"));
        anIf2.setValue("123");
        List asList = Arrays.asList(anIf, anIf2);
        ifOr.setConditions(((List<If>) asList));
        e4.setIf(ifOr);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfAll() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfAndAll(json);
    }

    @Test
    public void convertAndParseIfAnd() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfAndAll(json);
    }

    private void convertAndParseIfAndAll(String json) throws IOException {
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfAnd ifAnd = new IfAnd();
        IfFieldCheckValue anIf = new IfFieldCheckValue();
        anIf.setOp("$eq");
        anIf.setField(new Path("abc"));
        anIf.setValue("123");
        IfFieldCheckValue anIf2 = new IfFieldCheckValue();
        anIf2.setOp("$lt");
        anIf2.setField(new Path("abc"));
        anIf2.setValue("123");
        List asList = Arrays.asList(anIf, anIf2);
        ifAnd.setConditions(((List<If>) asList));
        e4.setIf(ifAnd);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfNot() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$lt\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfNot ifNot = new IfNot();
        IfAnd ifAnd = new IfAnd();
        IfFieldCheckValue anIf = new IfFieldCheckValue();
        anIf.setOp("$eq");
        anIf.setField(new Path("abc"));
        anIf.setValue("123");
        IfFieldCheckValue anIf2 = new IfFieldCheckValue();
        anIf2.setOp("$lt");
        anIf2.setField(new Path("abc"));
        anIf2.setValue("123");
        List asList = Arrays.asList(anIf, anIf2);
        ifAnd.setConditions(asList);
        List l = Arrays.asList(ifAnd);
        ifNot.setConditions(l);
        e4.setIf(ifNot);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfFieldEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldEmpty pe = new IfFieldEmpty();
        pe.setField(Path.ANYPATH);
        e4.setIf(pe);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void parseIfFieldEmptyNoField() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertIfFieldEmptyNoField() throws IOException {
        com.redhat.lightblue.util.Error error = null;
        try {
            JsonNode rJSON = p.newNode();
            RDBMS r = new RDBMS();
            r.setDialect("oracle");
            r.setSQLMapping(new SQLMapping());
            r.getSQLMapping().setJoins(new ArrayList<Join>());
            r.getSQLMapping().getJoins().add(new Join());
            r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
            r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
            r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
            r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
            r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
            r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
            r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
            r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
            r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
            r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
            r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
            r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
            r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
            r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
            r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
            Operation o = new Operation();
            ArrayList<Expression> expressionList = new ArrayList<Expression>();
            Conditional e4 = new Conditional();
            IfFieldEmpty pe = new IfFieldEmpty();
            pe.setField(new Path(""));
            e4.setIf(pe);
            Then then = new Then();
            ArrayList<Expression> expressions2 = new ArrayList<Expression>();
            Statement e5 = new Statement();
            e5.setType("delete");
            e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
            expressions2.add(e5);
            then.setExpressions(expressions2);
            e4.setThen(then);
            expressionList.add(e4);
            o.setExpressionList(expressionList);
            r.setDelete(duplicate("delete", o));
            r.setFetch(duplicate("fetch", o));
            r.setInsert(duplicate("insert", o));
            r.setSave(duplicate("save", o));
            r.setUpdate(duplicate("update", o));
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseWrongExpressions() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[{\"$Xt\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseIfFieldCheckField() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldCheckField pe = new IfFieldCheckField();
        pe.setOp("$eq");
        pe.setField(Path.ANYPATH);
        pe.setRfield(Path.ANYPATH);
        e4.setIf(pe);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfFieldCheckValue() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"1\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"1\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"1\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"1\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"1\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldCheckValue pe = new IfFieldCheckValue();
        pe.setOp("$eq");
        pe.setField(Path.ANYPATH);
        pe.setValue("1");
        e4.setIf(pe);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfFieldCheckValues() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"1\",\"2\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"1\",\"2\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"1\",\"2\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"1\",\"2\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"1\",\"2\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldCheckValues pe = new IfFieldCheckValues();
        pe.setOp("$eq");
        pe.setField(Path.ANYPATH);
        ArrayList<String> arl = new ArrayList<String>();
        arl.add("1");
        arl.add("2");
        pe.setValues(arl);
        e4.setIf(pe);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseIfFieldRegex() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldRegex\":{\"field\":\"*\",\"regex\":\"*\",\"caseInsensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldRegex\":{\"field\":\"*\",\"regex\":\"*\",\"caseInsensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldRegex\":{\"field\":\"*\",\"regex\":\"*\",\"caseInsensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldRegex\":{\"field\":\"*\",\"regex\":\"*\",\"caseInsensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldRegex\":{\"field\":\"*\",\"regex\":\"*\",\"caseInsensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfFieldRegex pe = new IfFieldRegex();
        pe.setField(Path.ANYPATH);
        pe.setRegex("*");
        e4.setIf(pe);
        Then then = new Then();
        ArrayList<Expression> expressions2 = new ArrayList<Expression>();
        Statement e5 = new Statement();
        e5.setType("delete");
        e5.setSQL("DELETE FROM somewhere WHERE someColumn=someValue");
        expressions2.add(e5);
        then.setExpressions(expressions2);
        e4.setThen(then);
        expressionList.add(e4);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        cut.convert(p, rJSON, r);

        assertEqualJson(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        assertEqualJson(json, roJSON.toString());
        assertEqualJson(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertForEachIterateOverFieldEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        r.setDialect("oracle");
        r.setSQLMapping(new SQLMapping());
        r.getSQLMapping().setJoins(new ArrayList<Join>());
        r.getSQLMapping().getJoins().add(new Join());
        r.getSQLMapping().getJoins().get(0).setJoinTablesStatement("x");
        r.getSQLMapping().getJoins().get(0).setProjectionMappings(new ArrayList<ProjectionMapping>());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().add(new ProjectionMapping());
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setColumn("c");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setField("f");
        r.getSQLMapping().getJoins().get(0).getProjectionMappings().get(0).setSort("s");
        r.getSQLMapping().getJoins().get(0).setTables(new ArrayList<Table>());
        r.getSQLMapping().getJoins().get(0).getTables().add(new Table());
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setAlias("a");
        r.getSQLMapping().getJoins().get(0).getTables().get(0).setName("n");
        r.getSQLMapping().setColumnToFieldMap(new ArrayList<ColumnToField>());
        r.getSQLMapping().getColumnToFieldMap().add(new ColumnToField());
        r.getSQLMapping().getColumnToFieldMap().get(0).setColumn("c");
        r.getSQLMapping().getColumnToFieldMap().get(0).setField("f");
        r.getSQLMapping().getColumnToFieldMap().get(0).setTable("t");
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        ForEach forEach = new ForEach();
        ArrayList<Expression> el = new ArrayList<Expression>();
        Statement es = new Statement();
        es.setSQL("X");
        es.setType("select");
        el.add(es);
        forEach.setExpressions(el);
        forEach.setIterateOverField(Path.EMPTY);
        Assert.assertEquals(el, forEach.getExpressions());
        expressionList.add(forEach);
        o.setExpressionList(expressionList);
        r.setDelete(duplicate("delete", o));
        r.setFetch(duplicate("fetch", o));
        r.setInsert(duplicate("insert", o));
        r.setSave(duplicate("save", o));
        r.setUpdate(duplicate("update", o));
        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex) {
            xe = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());
    }

    @Test
    public void parseOperationsExpressionsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseBindingsEmptyInAndOut() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"out\":[],\"in\":[]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseInOutEmptyColumn() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"out\":[{\"column\":\"\",\"field\":\"y\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseInOutEmptyField() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"out\":[{\"column\":\"x\",\"field\":\"\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseWrongExpressionsSQLAndTypeIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"field\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"\",\"type\":\"\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForWithEmptyFields() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"\",\"loopCounterVariableName\":\"\",\"expressions\":[]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseForEachWithEmptyFields() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$foreach\":{\"expressions\":[],\"iterateOverField\":\"\"}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"expressions\":[],\"iterateOverField\":\"\"}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"expressions\":[],\"iterateOverField\":\"\"}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"expressions\":[],\"iterateOverField\":\"\"}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"expressions\":[],\"iterateOverField\":\"\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseElseIfEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"abc\",\"value\":\"123\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldEmptyEmptyField() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldEmpty\":{\"field\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldFieldIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldFieldIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"\",\"rfield\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldRfieldIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldRfieldIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldOpIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"rfield\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckFieldOpIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckField\":{\"field\":\"*\",\"rfield\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueFieldIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueFieldIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"\",\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"\",\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"\",\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"\",\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"\",\"value\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueValueIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueValueIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"\",\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueOpIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"value\":\"*\",\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValueOpIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValue\":{\"field\":\"*\",\"value\":\"*\",\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesFieldIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesField1IsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"\",\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"\",\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"\",\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"\",\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"\",\"values\":[\"*\"],\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesValuesIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesValuesIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[],\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[],\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[],\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[],\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[],\"field\":\"*\",\"op\":\"$eq\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesOpIsNull() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"values\":[\"*\"],\"field\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void parseIfFieldCheckValuesOpIsEmpty() throws IOException {
        String json = "{\"rdbms\":{\"dialect\":\"oracle\",\"SQLMapping\": {\"columnToFieldMap\":[{\"table\":\"t\",\"column\":\"c\",\"field\":\"f\"}], \"joins\" :[{\"tables\":[{\"name\":\"n\",\"alias\":\"a\"}],\"joinTablesStatement\" : \"x\", \"projectionMappings\": [{\"column\":\"c\",\"field\":\"f\",\"sort\":\"s\"}]}]},\"delete\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"*\"],\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"*\"],\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"*\"],\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"*\"],\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$fieldCheckValues\":{\"field\":\"*\",\"values\":[\"*\"],\"op\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex) {
            error = ex;
        } catch (Throwable exx) {
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
}
