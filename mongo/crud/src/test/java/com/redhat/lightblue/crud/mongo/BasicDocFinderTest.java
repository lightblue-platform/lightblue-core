/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud.mongo;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.redhat.lightblue.crud.Operation;
import static com.redhat.lightblue.crud.mongo.AbstractMongoTest.coll;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.Path;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class BasicDocFinderTest extends AbstractMongoTest {

    private OCtx ctx;
    private Translator translator;

    @Before
    public void setup() throws IOException, ProcessingException {
        ctx = new OCtx(Operation.FIND);
        // load metadata 
        EntityMetadata md = getMd("./testMetadata.json");
        // and add it to metadata resolver (the context)
        ctx.add(md);
        // create translator with the context
        translator = new Translator(ctx, nodeFactory);
    }

    private void insert(String jsonStringFormat, String formatArg) {
        insert(jsonStringFormat, new String[]{formatArg});
    }

    private void insert(String jsonStringFormat, String[] formatArgs) {
        Gson g = new Gson();
        DBObject obj = g.fromJson(String.format(jsonStringFormat, (Object[]) formatArgs), BasicDBObject.class);
        WriteResult wr = coll.insert(obj);
        // check that insert didn't fail
        Assert.assertTrue(wr.getError() == null);
    }

    @Test
    public void findAll() throws IOException, ProcessingException {
        String id = "findBasic";
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null,
                // DBObject (sort)
                null,
                // Long (from)
                null,
                // Long (to)
                null);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(3, ctx.getDocumentsWithoutErrors().size());
    }

    @Test
    public void findOneOfMany() throws IOException, ProcessingException {
        String id = "findOneOfMany";
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        DBObject mongoQuery = new BasicDBObject();
        mongoQuery.put("_id", id + "1");

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                mongoQuery,
                // DBObject (sort)
                null,
                // Long (from)
                null,
                // Long (to)
                null);

        Assert.assertEquals("find count", 1, count);
        Assert.assertEquals(1, ctx.getDocumentsWithoutErrors().size());
    }

    @Test
    public void findLimit() throws IOException, ProcessingException {
        String id = "findLimit";
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                null,
                // Long (from)
                null,
                // Long (to)
                1l);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(2, ctx.getDocumentsWithoutErrors().size());
    }

    @Test
    public void findSort() throws IOException, ProcessingException {
        String id = "findSort";
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        DBObject sort = new BasicDBObject();
        sort.put("_id", -1);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                sort,
                // Long (from)
                null,
                // Long (to)
                null);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(3, ctx.getDocumentsWithoutErrors().size());

        // verify order
        Assert.assertEquals(id + "3", ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "2", ctx.getDocuments().get(1).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "1", ctx.getDocuments().get(2).getOutputDocument().get(new Path("_id")).asText());
    }

    @Test
    public void findSortAndLimit() throws IOException, ProcessingException {
        String id = "findSortAndLimit";
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        DBObject sort = new BasicDBObject();
        sort.put("_id", -1);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                sort,
                // Long (from)
                null,
                // Long (to)
                1l);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(2, ctx.getDocumentsWithoutErrors().size());

        // verify order
        Assert.assertEquals(id + "3", ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "2", ctx.getDocuments().get(1).getOutputDocument().get(new Path("_id")).asText());
    }

    @Test
    public void findSkip() throws IOException, ProcessingException {
        String id = "findSkip";
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                null,
                // Long (from)
                1l,
                // Long (to)
                null);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(2, ctx.getDocumentsWithoutErrors().size());

        // verify data
        Assert.assertEquals(id + "2", ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "3", ctx.getDocuments().get(1).getOutputDocument().get(new Path("_id")).asText());
    }

    @Test
    public void findSortAndSkip() throws IOException, ProcessingException {
        String id = "findSortAndSkip";
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 3, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        DBObject sort = new BasicDBObject();
        sort.put("_id", -1);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                sort,
                // Long (from)
                1l,
                // Long (to)
                null);

        Assert.assertEquals("find count", 3, count);
        Assert.assertEquals(2, ctx.getDocumentsWithoutErrors().size());

        // verify order
        Assert.assertEquals(id + "2", ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "1", ctx.getDocuments().get(1).getOutputDocument().get(new Path("_id")).asText());
    }

    @Test
    public void findSortSkipAndLimit() throws IOException, ProcessingException {
        String id = "findSortSkipAndLimit";
        insert("{_id:'%s',object_type:'test'}", id + "2");
        insert("{_id:'%s',object_type:'test'}", id + "1");
        insert("{_id:'%s',object_type:'test'}", id + "4");
        insert("{_id:'%s',object_type:'test'}", id + "3");

        Assert.assertEquals("count on collection", 4, coll.find(null).count());

        BasicDocFinder finder = new BasicDocFinder(translator);

        DBObject sort = new BasicDBObject();
        sort.put("_id", 1);

        long count = finder.find(
                // CRUDOperationContext
                ctx,
                //DBCollection
                coll,
                // DBObject (query)
                null, // all
                // DBObject (sort)
                sort,
                // Long (from)
                1l,
                // Long (to)
                2l);

        Assert.assertEquals("find count", 4, count);
        Assert.assertEquals(2, ctx.getDocumentsWithoutErrors().size());

        // verify order
        Assert.assertEquals(id + "2", ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText());
        Assert.assertEquals(id + "3", ctx.getDocuments().get(1).getOutputDocument().get(new Path("_id")).asText());
    }
}
