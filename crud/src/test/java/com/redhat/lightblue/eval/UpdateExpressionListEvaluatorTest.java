package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class UpdateExpressionListEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        doc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void setSimpleFieldTest() throws Exception {

        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field1' : 'set1', 'field2':'set2', 'field5': 0, 'field6.nf1':'set6' } }, {'$add' : { 'field3':1 } } ] ");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(doc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("set1", doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2", doc.get(new Path("field2")).asText());
        Assert.assertEquals(4, doc.get(new Path("field3")).asInt());
        Assert.assertFalse(doc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6", doc.get(new Path("field6.nf1")).asText());
    }

    @Test
    public void one_$parent_setSimpleFieldTest() throws Exception {

        UpdateExpression expr = EvalTestContext
                .updateExpressionFromJson("[ {'$set' : { 'field2.$parent.field1' : 'set1', 'field3.$parent.field2':'set2', 'field2.$parent.field5': 0, 'field2.$parent.field6.nf1':'set6' } }, {'$add' : { 'field2.$parent.field3':1 } } ] ");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(doc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("set1", doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2", doc.get(new Path("field2")).asText());
        Assert.assertEquals(4, doc.get(new Path("field3")).asInt());
        Assert.assertFalse(doc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6", doc.get(new Path("field6.nf1")).asText());
    }

}
