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
package com.redhat.lightblue.crud.mongo;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.JsonDoc;

/**
 *
 * @author nmalik
 */
public class BasicDocFinderTest extends AbstractMongoTest {

    private TestCRUDOperationContext ctx;
    private Translator translator;

    @Before
    public void setup() throws IOException, ProcessingException {
        ctx = new TestCRUDOperationContext(Operation.FIND);
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
        try {
            JsonNode node = JsonUtils.json(String.format(jsonStringFormat, (Object[]) formatArgs));
            BasicDBObject dbObject = new BasicDBObject();
            for (Iterator<String> itr = node.fieldNames(); itr.hasNext();) {
                String fld = itr.next();
                dbObject.append(fld, node.get(fld).asText());
            }
            WriteResult wr = coll.insert(dbObject);
            // check that insert didn't fail
            Assert.assertTrue(wr.getError() == null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void findAll() throws IOException, ProcessingException {
        String id = "findBasic";
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "2");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "1");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "4");
        insert("{\"_id\":\"%s\",\"objectType\":\"test\"}", id + "3");

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
