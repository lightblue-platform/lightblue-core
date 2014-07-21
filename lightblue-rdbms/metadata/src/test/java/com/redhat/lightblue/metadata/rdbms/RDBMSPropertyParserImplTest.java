package com.redhat.lightblue.metadata.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RDBMSPropertyParserImplTest {

    RDBMSPropertyParserImpl cut;
    JSONMetadataParser p;

    static final String expectedJSON = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"i\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}},{\"$foreach\":{\"iterateOverPath\":\"j\",\"expressions\":[{\"$statement\":{\"sql\":\"SELECT * FROM TABLE1\",\"type\":\"select\"}}]}}]}},{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";

    @Before
    public void setup() {
        cut = new RDBMSPropertyParserImpl();
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
    }

    @After
    public void tearDown() {
        cut = null;
        p = null;
    }

    @Test
    public void convertTest() throws IOException {
        JsonNode parent = p.newNode();
        RDBMS r = new RDBMS();

        Operation o = new Operation();
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
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
        e3.setIterateOverPath(new Path("j"));
        expressions.add(e3);
        e2.setExpressions(expressions);
        expressionList.add(e2);


        Conditional e4 = new Conditional();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, parent, r);

        Assert.assertEquals(expectedJSON, parent.toString());
    }

    @Test
    public void parseTest() throws IOException {
        Object r = cut.parse("rdbms", p, JsonUtils.json(expectedJSON).get("rdbms"));
        JsonNode parent = p.newNode();
        cut.convert(p, parent, r);
        Assert.assertEquals(expectedJSON, parent.toString());
    }
    
    @Test
    public void parseWrongNodeName() throws IOException {
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("x", p, JsonUtils.json(expectedJSON).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
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
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseMissingOperationsExpressions() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"ke\"}]},\"X\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseBindingsFieldsReq() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);

        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex){
            xe = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }

    @Test
    public void convertAndParseBindingsJustIn() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //With just In  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setInList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsJustOut() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"out\":[{\"column\":\"col\",\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //With just Out  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setOutList(inList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseBindingsNone() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void parseBindingsWrong() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseInOutMissingColumn() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"out\":[{\"path\":\"pat\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void convertAndParseBindingsBoth() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"pat\"}],\"out\":[{\"column\":\"col1\",\"path\":\"pat1\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();

        //No  bindings
        Bindings b = new Bindings();
        ArrayList<InOut> inList = new ArrayList<InOut>();
        InOut e = new InOut();
        e.setColumn("col");
        e.setPath(new Path("pat"));
        inList.add(e);
        b.setInList(inList);
        ArrayList<InOut> outList = new ArrayList<InOut>();
        InOut ou = new InOut();
        ou.setColumn("col1");
        ou.setPath(new Path("pat1"));
        outList.add(ou);
        b.setOutList(outList);

        o.setBindings(b);
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }

    @Test
    public void convertAndParseStatement() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"fetch\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"insert\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"save\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]},\"update\":{\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Statement e1 = new Statement();
        e1.setSQL("REQ EXPRESSION");
        e1.setType("select");
        expressionList.add(e1);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
     
    @Test
    public void parseMissingStatemtentsSQL() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
        @Test
    public void parseMissingStatemtentsType() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"ke\"}]},\"expressions\":[{\"$statement\":{\"sql\":\"REQ EXPRESSION\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void convertAndParseForEachMissingIterateOverPath() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        ForEach forEach = new ForEach();
        ArrayList<Expression> el = new ArrayList<Expression>();
        Statement es = new Statement();
        es.setSQL("X");
        es.setType("select");
        el.add(es);
        forEach.setExpressions(el);
        expressionList.add(forEach);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex){
            xe = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    
    @Test
    public void convertAndParseForEachMissingExpressions() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$foreach\":{\"iterateOverPath\":\"*\"}}]},\"fetch\":{\"expressions\":[{\"$foreach\":{\"iterateOverPath\":\"*\"}}]},\"insert\":{\"expressions\":[{\"$foreach\":{\"iterateOverPath\":\"*\"}}]},\"save\":{\"expressions\":[{\"$foreach\":{\"iterateOverPath\":\"*\"}}]},\"update\":{\"expressions\":[{\"$foreach\":{\"iterateOverPath\":\"*\"}}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        ForEach forEach = new ForEach();
        forEach.setIterateOverPath(Path.ANYPATH);
        expressionList.add(forEach);
        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        com.redhat.lightblue.util.Error xe = null;
        try {
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex){
            xe = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(xe);
        }
        Assert.assertEquals("{}", rJSON.toString());

        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    
    @Test
    public void parseForMissingLoopTimes() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$for\":{\"loopCounterVariableName\":\"2\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseForLoopTimesNotInteger() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"A\",\"loopCounterVariableName\":\"2\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseForMissingLoopCounterVariableName() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"1\",\"expressions\":[{\"$statement\":{\"sql\":\"X\",\"type\":\"select\"}}]}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
      @Test
    public void parseForMissingExpressions() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$for\":{\"loopTimes\":\"1\",\"loopCounterVariableName\":\"2\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
        
      @Test
    public void parseWrongExpression() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"Wrong\":{}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void convertAndParseElseIf() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";

        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);
        Assert.assertEquals(json, rJSON.toString());

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void parseElseIfMissingIf() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$ifx\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";        
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseElseIfMissingThen() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}],\"$elseIf\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},\"$thenX\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}]}}}";        
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }    
     
    @Test
    public void convertAndParseIfOr() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$or\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfOrAny(json);
    }
    
    @Test
    public void convertAndParseIfAny() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$any\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfOrAny(json);
    }
    
    private void convertAndParseIfOrAny(String json) throws IOException {
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfOr ifOr = new IfOr();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
        IfPathValue anIf2 = new IfPathValue();
        anIf2.setConditional("lessThan");
        anIf2.setPath1(new Path("abc"));
        anIf2.setValue2("123");
        List asList = Arrays.asList(anIf, anIf2);
        ifOr.setConditions(((List<If>)asList));
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void convertAndParseIfAll() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$all\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfAndAll(json);
    }
    
    @Test
    public void convertAndParseIfAnd() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        convertAndParseIfAndAll(json);
    }
    
    private void convertAndParseIfAndAll(String json) throws IOException {
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfAnd ifAnd = new IfAnd();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
        IfPathValue anIf2 = new IfPathValue();
        anIf2.setConditional("lessThan");
        anIf2.setPath1(new Path("abc"));
        anIf2.setValue2("123");
        List asList = Arrays.asList(anIf, anIf2);
        ifAnd.setConditions(((List<If>)asList));
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void convertAndParseIfNot() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$not\":{\"$and\":[{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"equalTo\"}},{\"$path-check-value\":{\"path1\":\"abc\",\"value2\":\"123\",\"conditional\":\"lessThan\"}}]}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfNot ifNot  = new IfNot();
        IfAnd ifAnd = new IfAnd();
        IfPathValue anIf = new IfPathValue();
        anIf.setConditional("equalTo");
        anIf.setPath1(new Path("abc"));
        anIf.setValue2("123");
        IfPathValue anIf2 = new IfPathValue();
        anIf2.setConditional("lessThan");
        anIf2.setPath1(new Path("abc"));
        anIf2.setValue2("123");
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void convertAndParseIfPathEmpty() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"*\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathEmpty pe = new IfPathEmpty();
        pe.setPath1(Path.ANYPATH);
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void parseIfPathEmptyNoPath() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-empty\":{\"path1\":\"\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
        
    @Test
    public void convertIfPathEmptyNoPath() throws IOException {
        com.redhat.lightblue.util.Error error = null;
        try {
            JsonNode rJSON = p.newNode();
            RDBMS r = new RDBMS();
            Operation o = new Operation();
            ArrayList<Expression> expressionList = new ArrayList<Expression>();
            Conditional e4 = new Conditional();
            IfPathEmpty pe = new IfPathEmpty();
            pe.setPath1(new Path (""));
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
            r.setDelete(o);
            r.setFetch(o);
            r.setInsert(o);
            r.setSave(o);
            r.setUpdate(o);
            cut.convert(p, rJSON, r);
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void parseWrongExpressions() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"bindings\":{\"in\":[{\"column\":\"col\",\"path\":\"ke\"}]},\"expressions\":[{\"$Xt\":{\"sql\":\"REQ EXPRESSION\",\"type\":\"select\"}}]}}}";
        com.redhat.lightblue.util.Error error = null;
        try {
            Object r = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        } catch (com.redhat.lightblue.util.Error ex){
            error = ex;
        } catch (Throwable exx){
            exx.printStackTrace();
        } finally {
            Assert.assertNotNull(error);
        }
    }
    
    @Test
    public void convertAndParseIfPathPath() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-path\":{\"path1\":\"*\",\"path2\":\"*\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-check-path\":{\"path1\":\"*\",\"path2\":\"*\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-check-path\":{\"path1\":\"*\",\"path2\":\"*\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-check-path\":{\"path1\":\"*\",\"path2\":\"*\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-check-path\":{\"path1\":\"*\",\"path2\":\"*\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathPath pe = new IfPathPath();
        pe.setConditional("equalTo");
        pe.setPath1(Path.ANYPATH);
        pe.setPath2(Path.ANYPATH);
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void convertAndParseIfPathValue() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"*\",\"value2\":\"1\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"*\",\"value2\":\"1\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"*\",\"value2\":\"1\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"*\",\"value2\":\"1\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-check-value\":{\"path1\":\"*\",\"value2\":\"1\",\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathValue pe = new IfPathValue();
        pe.setConditional("equalTo");
        pe.setPath1(Path.ANYPATH);
        pe.setValue2("1");
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    @Test
    public void convertAndParseIfPathValues() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-check-values\":{\"path1\":\"*\",\"values2\":[\"1\",\"2\"],\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-check-values\":{\"path1\":\"*\",\"values2\":[\"1\",\"2\"],\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-check-values\":{\"path1\":\"*\",\"values2\":[\"1\",\"2\"],\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-check-values\":{\"path1\":\"*\",\"values2\":[\"1\",\"2\"],\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-check-values\":{\"path1\":\"*\",\"values2\":[\"1\",\"2\"],\"conditional\":\"equalTo\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathValues pe = new IfPathValues();
        pe.setConditional("equalTo");
        pe.setPath1(Path.ANYPATH);
        ArrayList<String> arl = new ArrayList<String>();
        arl.add("1");
        arl.add("2");
        pe.setValues2(arl);
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
    
    
    @Test
    public void convertAndParseIfPathRegex() throws IOException {
        String json = "{\"rdbms\":{\"delete\":{\"expressions\":[{\"$if\":{\"$path-regex\":{\"path\":\"*\",\"regex\":\"*\",\"case_insensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"fetch\":{\"expressions\":[{\"$if\":{\"$path-regex\":{\"path\":\"*\",\"regex\":\"*\",\"case_insensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"insert\":{\"expressions\":[{\"$if\":{\"$path-regex\":{\"path\":\"*\",\"regex\":\"*\",\"case_insensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"save\":{\"expressions\":[{\"$if\":{\"$path-regex\":{\"path\":\"*\",\"regex\":\"*\",\"case_insensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]},\"update\":{\"expressions\":[{\"$if\":{\"$path-regex\":{\"path\":\"*\",\"regex\":\"*\",\"case_insensitive\":\"false\",\"multiline\":\"false\",\"extended\":\"false\",\"dotall\":\"false\"}},\"$then\":[{\"$statement\":{\"sql\":\"DELETE FROM somewhere WHERE someColumn=someValue\",\"type\":\"delete\"}}]}]}}}";
        JsonNode rJSON = p.newNode();
        RDBMS r = new RDBMS();
        Operation o = new Operation();
        ArrayList<Expression> expressionList = new ArrayList<Expression>();
        Conditional e4 = new Conditional();
        IfPathRegex pe = new IfPathRegex();
        pe.setPath(Path.ANYPATH);
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
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, rJSON, r);

        Assert.assertEquals(json, rJSON.toString());
        Object ro = cut.parse("rdbms", p, JsonUtils.json(json).get("rdbms"));
        JsonNode roJSON = p.newNode();
        cut.convert(p, roJSON, ro);
        Assert.assertEquals(json, roJSON.toString());
        Assert.assertEquals(roJSON.toString(), rJSON.toString());
    }
    
}
