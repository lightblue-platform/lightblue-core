package com.redhat.lightblue.metadata.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.Path;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class RDBMSPropertyParserImplTest {

    RDBMSPropertyParserImpl cut;

    @Before
    public void setup() {
        cut = new RDBMSPropertyParserImpl();
    }

    @After
    public void tearDown() {
        cut = null;
    }

    @Test
    public void convertTest() throws IOException, ParseException, JSONException, ProcessingException {
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerPropertyParser("rdbms",cut);
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
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
        e1.setType("SELECT");
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

        //////
        Conditional e4 = new Conditional();
        IfPathValue anIf = new IfPathValue();
        e4.setIf(anIf);
        Then then = new Then();
        e4.setThen(then);
        expressionList.add(e4);

        o.setExpressionList(expressionList);
        r.setDelete(o);
        r.setFetch(o);
        r.setInsert(o);
        r.setSave(o);
        r.setUpdate(o);
        cut.convert(p, parent, r);
        Assert.assertEquals(null, parent);
    }
}
