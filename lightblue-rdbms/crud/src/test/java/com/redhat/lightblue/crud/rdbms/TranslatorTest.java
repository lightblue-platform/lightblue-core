package com.redhat.lightblue.crud.rdbms;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.metadata.rdbms.model.*;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TranslatorTest {
    final String valueQuery1 = "{\"field\":\"x.\", \"op\":\"$eq\", \"rvalue\":\"string\"}";
    final String fieldQuery1 = "{\"field\":\"x\", \"op\":\"$eq\", \"rfield\":\"y\"}";
    final String naryQuery1 = "{\"field\":\"z\", \"op\":\"$in\", \"values\":[1,2,3,4,5]}";
    final String regexQuery1 = "{\"field\":\"x\", \"regex\":\"*pat*\"}";
    final String unaryQuery1 = "{ \"$not\": " + valueQuery1 + "}";
    final String naryLogQueryAnd1 = "{ \"$and\" : [" + valueQuery1 + "," + fieldQuery1 + "," + naryQuery1 + "," + unaryQuery1 + "]}";
    final String arrContains1 = "{\"array\":\"z\", \"contains\":\"$any\", \"values\":[1,2,3,4,5]}";
    final String arrContains2 = "{\"array\":\"z\", \"contains\":\"$all\", \"values\":[1,2,3,4,5]}";

    Translator cut = Translator.ORACLE;
    CRUDOperationContext crud = new TestCRUD();
    RDBMSContext rdbmsContext = new RDBMSContext();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        crud = new TestCRUD();
        rdbmsContext = new RDBMSContext();
        EntityMetadata em = new EntityMetadata("test");
        em.getEntitySchema().getFields().addNew(new SimpleField("x"));
        em.getEntitySchema().getFields().addNew(new ArrayField("z", new SimpleArrayElement(StringType.TYPE)));
        rdbmsContext.setEntityMetadata(em);
        RDBMS rdbms = new RDBMS();
        SQLMapping sQLMapping = new SQLMapping();
        ArrayList<Join> joins = new ArrayList<Join>();
        Join e = new Join();
        Table table = new Table();
        table.setName("123");
        table.setAlias("xyz");
        e.getTables().add(table);
        table = new Table();
        table.setName("K");
        table.setAlias("w");
        e.getTables().add(table);
        e.setJoinTablesStatement(" xyz.c1 = w.c2 ");
        joins.add(e);
        ProjectionMapping projectionMapping = new ProjectionMapping();
        projectionMapping.setColumn("z1");
        projectionMapping.setField("z");
        e.getProjectionMappings().add(projectionMapping);
        projectionMapping = new ProjectionMapping();
        projectionMapping.setColumn("x1");
        projectionMapping.setField("x");
        e.getProjectionMappings().add(projectionMapping);
        projectionMapping = new ProjectionMapping();
        projectionMapping.setColumn("z1");
        projectionMapping.setField("z");
        e.getProjectionMappings().add(projectionMapping);
        sQLMapping.setJoins(joins);
        sQLMapping.setColumnToFieldMap(new ArrayList<ColumnToField>());
        rdbms.setSQLMapping(sQLMapping);
        rdbmsContext.setRdbms(rdbms);
    }

    @After
    public void tearDown() throws Exception {
        crud = null;
        rdbmsContext = null;
    }

    @Test
    public void testNoQuery() throws Exception {
        exception.expect(com.redhat.lightblue.util.Error.class);
        exception.expectMessage("{\"object_type\":\"error\",\"context\":\"translateQuery\",\"errorCode\":\"Not supported query\",\"msg\":\"q=null\"}");
        cut.translate(crud, rdbmsContext);
    }


    @Test
    public void testArrayContainsAny() throws Exception {
        exception.expect(com.redhat.lightblue.util.Error.class);
        exception.expectMessage("{\"object_type\":\"error\",\"context\":\"translateQuery\",\"errorCode\":\"not supported operator\",\"msg\":\"{\\\"array\\\":\\\"z\\\",\\\"contains\\\":\\\"$any\\\",\\\"values\\\":[1,2,3,4,5]}\"}");
        rdbmsContext.setQueryExpression(generateQuery(arrContains1));
        cut.translate(crud, rdbmsContext);

    }

    @Test
    public void testRecursiveTranslateArrayContains() throws Exception {
        rdbmsContext.setQueryExpression(generateQuery(arrContains2));
        List<SelectStmt> translate = cut.translate(crud, rdbmsContext);
        System.out.println(translate);

    }

    private QueryExpression generateQuery(String str) throws IOException {
        return QueryExpression.fromJson((ObjectNode) JsonUtils.json(str));
    }

    @Test
    public void testRecursiveTranslateArrayElemMatch() throws Exception {

    }

    @Test
    public void testRecursiveTranslateFieldComparison() throws Exception {

    }

    @Test
    public void testRecursiveTranslateNaryLogicalExpression() throws Exception {

    }

    @Test
    public void testRecursiveTranslateNaryRelationalExpression() throws Exception {

    }

    @Test
    public void testRecursiveTranslateRegexMatchExpression() throws Exception {

    }

    @Test
    public void testRecursiveTranslateUnaryLogicalExpression() throws Exception {

    }

    @Test
    public void testRecursiveTranslateValueComparisonExpression() throws Exception {

    }

    static class TestCRUD extends CRUDOperationContext {

        public TestCRUD(){
            this(null,null,new Factory(), null,null);
        }
        public TestCRUD(Operation op, String entityName, Factory f, Set<String> callerRoles, List<JsonDoc> docs) {
            super(op, entityName, f, callerRoles, docs);
        }

        @Override
        public EntityMetadata getEntityMetadata(String entityName) {
            return null;
        }
    }


}