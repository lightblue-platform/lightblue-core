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

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MongoCRUDControllerTest extends AbstractMongoTest {

    private MongoCRUDController controller;

    @Before
    public void setup() throws Exception {
        final DB dbx = db;
        dbx.createCollection(COLL_NAME, null);

        controller = new MongoCRUDController(nodeFactory, new DBResolver() {
            @Override
            public DB get(MongoDataStore store) {
                return dbx;
            }
        });
    }

    @Test
    public void insertTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        Projection projection = projection("{'field':'_id'}");
        ctx.addDocument(doc);
        CRUDInsertionResponse response = controller.insert(ctx, projection);
        System.out.println(ctx.getDataErrors());
        Assert.assertEquals(1, ctx.getDocuments().size());
        Assert.assertTrue(ctx.getErrors() == null || ctx.getErrors().isEmpty());
        Assert.assertTrue(ctx.getDataErrors() == null || ctx.getDataErrors().isEmpty());
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), response.getNumInserted());
        String id = ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        Assert.assertEquals(1, coll.find(new BasicDBObject("_id", new ObjectId(id))).count());
    }

    @Test
    public void saveTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        Projection projection = projection("{'field':'_id'}");
        ctx.addDocument(doc);
        System.out.println("Write doc:" + doc);
        CRUDInsertionResponse response = controller.insert(ctx, projection);
        String id = ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        ctx = new OCtx(Operation.FIND);
        ctx.add(md);
        controller.find(ctx, query("{'field':'_id','op':'=','rvalue':'" + id + "'}"),
                projection("{'field':'*','recursive':1}"), null, null, null);
        JsonDoc readDoc = ctx.getDocuments().get(0);
        // Change some fields
        System.out.println("Read doc:" + readDoc);
        readDoc.modify(new Path("field1"), nodeFactory.textNode("updated"), false);
        readDoc.modify(new Path("field7.0.elemf1"), nodeFactory.textNode("updated too"), false);
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), response.getNumInserted());

        // Save it back
        ctx = new OCtx(Operation.SAVE);
        ctx.add(md);
        ctx.addDocument(readDoc);
        CRUDSaveResponse saveResponse = controller.save(ctx, false, projection);

        ctx = new OCtx(Operation.FIND);
        ctx.add(md);
        // Read it back
        controller.find(ctx, query("{'field':'_id','op':'=','rvalue':'" + id + "'}"),
                projection("{'field':'*','recursive':1}"), null, null, null);
        JsonDoc r2doc = ctx.getDocuments().get(0);
        Assert.assertEquals(readDoc.get(new Path("field1")).asText(), r2doc.get(new Path("field1")).asText());
        Assert.assertEquals(readDoc.get(new Path("field7.0.elemf1")).asText(), r2doc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), saveResponse.getNumSaved());
    }

    @Test
    public void upsertTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        ctx.addDocument(doc);
        System.out.println("Write doc:" + doc);
        CRUDInsertionResponse response = controller.insert(ctx, projection("{'field':'_id'}"));
        String id = ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        ctx = new OCtx(Operation.FIND);
        ctx.add(md);
        controller.find(ctx, query("{'field':'_id','op':'=','rvalue':'" + id + "'}"),
                projection("{'field':'*','recursive':1}"), null, null, null);
        JsonDoc readDoc = ctx.getDocuments().get(0);
        // Remove id, to force re-insert
        readDoc.modify(new Path("_id"), null, false);
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), response.getNumInserted());

        ctx = new OCtx(Operation.SAVE);
        ctx.add(md);
        ctx.addDocument(readDoc);
        // This should not insert anything
        CRUDSaveResponse sr = controller.save(ctx, false, projection("{'field':'_id'}"));
        Assert.assertEquals(1, coll.find(null).count());

        ctx = new OCtx(Operation.SAVE);
        ctx.add(md);
        ctx.addDocument(readDoc);
        sr = controller.save(ctx, true, projection("{'field':'_id'}"));
        Assert.assertEquals(2, coll.find(null).count());
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), sr.getNumSaved());
    }

    @Test
    public void updateTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);
        ctx.add(md);
        // Generate some docs
        List<JsonDoc> docs = new ArrayList<>();
        int numDocs = 20;
        for (int i = 0; i < numDocs; i++) {
            JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
            doc.modify(new Path("field1"), nodeFactory.textNode("doc" + i), false);
            doc.modify(new Path("field3"), nodeFactory.numberNode(i), false);
            docs.add(doc);
        }
        ctx.addDocuments(docs);
        CRUDInsertionResponse response = controller.insert(ctx, projection("{'field':'_id'}"));
        Assert.assertEquals(numDocs, coll.find(null).count());
        Assert.assertEquals(ctx.getDocumentsWithoutErrors().size(), response.getNumInserted());

        // Single doc update
        ctx = new OCtx(Operation.UPDATE);
        ctx.add(md);
        CRUDUpdateResponse upd = controller.update(ctx, query("{'field':'field3','op':'$eq','rvalue':10}"),
                update("{ '$set': { 'field3' : 1000 } }"),
                projection("{'field':'_id'}"));
        Assert.assertEquals(1, upd.getNumUpdated());
        Assert.assertEquals(0, upd.getNumFailed());
        Assert.assertEquals(AtomicIterateUpdate.class, ctx.getProperty(MongoCRUDController.PROP_UPDATER).getClass());
        DBObject obj = coll.find(new BasicDBObject("field3", 1000), new BasicDBObject("_id", 1)).next();
        Assert.assertNotNull(obj);
        System.out.println("DBObject:" + obj);
        System.out.println("Output doc:" + ctx.getDocuments().get(0).getOutputDocument());
        Assert.assertEquals(ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText(),
                obj.get("_id").toString());
        Assert.assertEquals(1, coll.find(new BasicDBObject("field3", 1000)).count());

        // Bulk update
        ctx = new OCtx(Operation.UPDATE);
        ctx.add(md);
        upd = controller.update(ctx, query("{'field':'field3','op':'>','rvalue':10}"),
                update("{ '$set': { 'field3' : 1000 } }"),
                projection("{'field':'_id'}"));
        Assert.assertEquals(AtomicIterateUpdate.class, ctx.getProperty(MongoCRUDController.PROP_UPDATER).getClass());
        Assert.assertEquals(10, upd.getNumUpdated());
        Assert.assertEquals(0, upd.getNumFailed());
        Assert.assertEquals(10, coll.find(new BasicDBObject("field3", new BasicDBObject("$gt", 10))).count());

        // Bulk direct update
        ctx = new OCtx(Operation.UPDATE);
        ctx.add(md);
        upd = controller.update(ctx, query("{'field':'field3','op':'>','rvalue':10}"),
                update("{ '$set': { 'field3' : 1000 } }"), null);
        Assert.assertEquals(AtomicIterateUpdate.class, ctx.getProperty(MongoCRUDController.PROP_UPDATER).getClass());
        Assert.assertEquals(10, upd.getNumUpdated());
        Assert.assertEquals(0, upd.getNumFailed());
        Assert.assertEquals(10, coll.find(new BasicDBObject("field3", new BasicDBObject("$gt", 10))).count());

        // Iterate update
        ctx = new OCtx(Operation.UPDATE);
        ctx.add(md);
        // Updating an array field will force use of IterateAndupdate
        upd = controller.update(ctx, query("{'field':'field3','op':'>','rvalue':10}"),
                update("{ '$set': { 'field7.0.elemf1' : 'blah' } }"), projection("{'field':'_id'}"));
        Assert.assertEquals(IterateAndUpdate.class, ctx.getProperty(MongoCRUDController.PROP_UPDATER).getClass());
        Assert.assertEquals(10, upd.getNumUpdated());
        Assert.assertEquals(0, upd.getNumFailed());
        Assert.assertEquals(10, coll.find(new BasicDBObject("field7.0.elemf1", "blah")).count());
    }

    @Test
    public void sortAndPageTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);

        ctx.add(md);
        // Generate some docs
        List<JsonDoc> docs = new ArrayList<>();
        int numDocs = 20;
        for (int i = 0; i < numDocs; i++) {
            JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
            doc.modify(new Path("field1"), nodeFactory.textNode("doc" + i), false);
            doc.modify(new Path("field3"), nodeFactory.numberNode(i), false);
            docs.add(doc);
        }
        ctx.addDocuments(docs);
        controller.insert(ctx, projection("{'field':'_id'}"));

        ctx = new OCtx(Operation.FIND);
        ctx.add(md);
        CRUDFindResponse response = controller.find(ctx, query("{'field':'field3','op':'>=','rvalue':0}"),
                projection("{'field':'*','recursive':1}"),
                sort("{'field3':'$desc'}"), null, null);
        Assert.assertEquals(numDocs, ctx.getDocuments().size());
        int lastValue = -1;
        for (DocCtx doc : ctx.getDocuments()) {
            int value = doc.getOutputDocument().get(new Path("field3")).asInt();
            if (value < lastValue) {
                Assert.fail("wrong order");
            }
        }

        for (int k = 0; k < 15; k++) {
            ctx = new OCtx(Operation.FIND);
            ctx.add(md);
            response = controller.find(ctx, query("{'field':'field3','op':'>=','rvalue':0}"),
                    projection("{'field':'*','recursive':1}"),
                    sort("{'field3':'$asc'}"), new Long(k), new Long(k + 5));

            int i = 0;
            for (DocCtx doc : ctx.getDocuments()) {
                int value = doc.getOutputDocument().get(new Path("field3")).asInt();
                Assert.assertEquals(i + k, value);
                i++;
            }
        }
    }

    @Test
    public void deleteTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        OCtx ctx = new OCtx(Operation.INSERT);
        ctx.add(md);
        // Generate some docs
        List<JsonDoc> docs = new ArrayList<>();
        int numDocs = 20;
        for (int i = 0; i < numDocs; i++) {
            JsonDoc jsonDOc = new JsonDoc(loadJsonNode("./testdata1.json"));
            jsonDOc.modify(new Path("field1"), nodeFactory.textNode("doc" + i), false);
            jsonDOc.modify(new Path("field3"), nodeFactory.numberNode(i), false);
            docs.add(jsonDOc);
        }
        ctx.addDocuments(docs);
        controller.insert(ctx, projection("{'field':'_id'}"));
        Assert.assertEquals(numDocs, coll.find(null).count());

        // Single doc delete
        ctx = new OCtx(Operation.DELETE);
        ctx.add(md);
        CRUDDeleteResponse del = controller.delete(ctx, query("{'field':'field3','op':'$eq','rvalue':10}"));
        Assert.assertEquals(1, del.getNumDeleted());
        Assert.assertEquals(numDocs - 1, coll.find(null).count());

        // Bulk delete
        ctx = new OCtx(Operation.DELETE);
        ctx.add(md);
        del = controller.delete(ctx, query("{'field':'field3','op':'>','rvalue':10}"));
        Assert.assertEquals(9, del.getNumDeleted());
        Assert.assertEquals(10, coll.find(null).count());
    }
}
