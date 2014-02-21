package com.redhat.lightblue.crud.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.runtime.Network;

public class CRUDControllerTest extends AbstractJsonSchemaTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(true);

    // Copied from  https://github.com/tommysdk/showcase/blob/master/mongo-in-mem/src/test/java/tommysdk/showcase/mongo/TestInMemoryMongo.java
    private static final String MONGO_HOST = "localhost";
    private static final int MONGO_PORT = 27777;
    private static final String IN_MEM_CONNECTION_URL = MONGO_HOST + ":" + MONGO_PORT;

    private static final String DB_NAME = "test";
    private static final String COLL_NAME = "data";

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;

    private MongoCRUDController controller;

    private  Factory factory;


    private class OCtx extends CRUDOperationContext {
        private final Map<String,EntityMetadata> map=new HashMap<String,EntityMetadata>();

        public OCtx() {
            super("test",factory,new HashSet<String>(),null);
        }

        public void add(EntityMetadata md) {
            map.put(md.getName(), md);
        }

        @Override
        public EntityMetadata getEntityMetadata(String entityName) {
            return map.get(entityName);
        }
    }

    @Before
    public void setup() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(de.flapdoodle.embed.mongo.distribution.Version.V2_0_5, MONGO_PORT, Network.localhostIsIPv6()));
        mongod = mongodExe.start();
        mongo = new Mongo(IN_MEM_CONNECTION_URL);
        final DB db = mongo.getDB(DB_NAME);

        db.createCollection(COLL_NAME, null);

        controller=new MongoCRUDController(nodeFactory,new DBResolver() {
                @Override
                public DB get(MongoDataStore store) {
                    return db;
                }
            });

        factory=new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
    }

    @After
    public void teardown() throws Exception {
        if (mongod != null) {
            mongo.dropDatabase(DB_NAME);
            mongod.stop();
            mongodExe.stop();
        }
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(json(s));
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(json(s));
    }

    private UpdateExpression update(String s) throws Exception {
        return UpdateExpression.fromJson(json(s));
    }

    private Sort sort(String s) throws Exception {
        return Sort.fromJson(json(s));
    }

    private JsonNode json(String s) throws Exception {
        return JsonUtils.json(s.replace('\'', '\"'));
    }

    private EntityMetadata getMd(String fname) throws Exception {
        runValidJsonTest("json-schema/metadata/metadata.json", fname);
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    @Test
    public void insertTest() throws Exception {
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        Projection projection = projection("{'field':'_id'}");
        ctx.addDocument(doc);
        CRUDInsertionResponse response=controller.insert(ctx,projection);
        Assert.assertEquals(1, ctx.getDocuments().size());
        Assert.assertTrue(ctx.getErrors() == null || ctx.getErrors().isEmpty());
        Assert.assertTrue(ctx.getDataErrors() == null || ctx.getDataErrors().isEmpty());
        String id = ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        DB db = mongo.getDB(DB_NAME);
        DBCollection coll = db.getCollection(COLL_NAME);
        Assert.assertEquals(1, coll.find(new BasicDBObject("_id", new ObjectId(id))).count());
    }

    @Test
    public void saveTest() throws Exception {
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        Projection projection = projection("{'field':'_id'}");
        ctx.addDocument(doc);
        System.out.println("Write doc:"+doc);
        CRUDInsertionResponse response=controller.insert(ctx,projection);
        String id=ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        JsonDoc readDoc=controller.find(ctx,query("{'field':'_id','op':'=','rvalue':'"+id+"'}"),
                                        projection("{'field':'*','recursive':1}"),null,null,null).getResults().get(0);
        // Change some fields
        System.out.println("Read doc:" + readDoc);
        readDoc.modify(new Path("field1"), nodeFactory.textNode("updated"), false);
        readDoc.modify(new Path("field7.0.elemf1"), nodeFactory.textNode("updated too"), false);

        // Save it back
        ctx=new OCtx();
        ctx.add(md);
        ctx.addDocument(readDoc);
        controller.save(ctx,false,projection);
        
        // Read it back
        JsonDoc r2doc=controller.find(ctx,query("{'field':'_id','op':'=','rvalue':'"+id+"'}"),
                                      projection("{'field':'*','recursive':1}"),null,null,null).getResults().get(0);
        Assert.assertEquals(readDoc.get(new Path("field1")).asText(),r2doc.get(new Path("field1")).asText());
        Assert.assertEquals(readDoc.get(new Path("field7.0.elemf1")).asText(),r2doc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void upsertTest() throws Exception {
        DB db = mongo.getDB(DB_NAME);
        DBCollection coll=db.getCollection(COLL_NAME);
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
        ctx.add(md);
        JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
        ctx.addDocument(doc);
        System.out.println("Write doc:"+doc);
        CRUDInsertionResponse response=controller.insert(ctx,projection("{'field':'_id'}"));
        String id=ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText();
        JsonDoc readDoc=controller.find(ctx,query("{'field':'_id','op':'=','rvalue':'"+id+"'}"),
                                        projection("{'field':'*','recursive':1}"),null,null,null).getResults().get(0);
        // Remove id, to force re-insert
        readDoc.modify(new Path("_id"), null, false);

        ctx=new OCtx();
        ctx.add(md);
        ctx.addDocument(readDoc);
        // This should not insert anything
        CRUDSaveResponse sr=controller.save(ctx,false,projection("{'field':'_id'}"));
        Assert.assertEquals(1,coll.find(null).count());
        
        ctx=new OCtx();
        ctx.add(md);
        ctx.addDocument(readDoc);
        sr=controller.save(ctx,true,projection("{'field':'_id'}"));
        Assert.assertEquals(2,coll.find(null).count());
    }

    @Test
    public void updateTest() throws Exception {
        DB db = mongo.getDB(DB_NAME);
        DBCollection coll=db.getCollection(COLL_NAME);
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
        ctx.add(md);
        // Generate some docs
        List<JsonDoc> docs=new ArrayList<JsonDoc>();
        int numDocs = 20;
        for (int i = 0; i < numDocs; i++) {
            JsonDoc doc = new JsonDoc(loadJsonNode("./testdata1.json"));
            doc.modify(new Path("field1"), nodeFactory.textNode("doc" + i), false);
            doc.modify(new Path("field3"), nodeFactory.numberNode(i), false);
            docs.add(doc);
        }
        ctx.addDocuments(docs);
        controller.insert(ctx,projection("{'field':'_id'}"));
        Assert.assertEquals(numDocs,coll.find(null).count());

        // Single doc update
        ctx=new OCtx();
        ctx.add(md);
        CRUDUpdateResponse upd=controller.update(ctx,query("{'field':'field3','op':'$eq','rvalue':10}"),
                                                 update("{ '$set': { 'field3' : 1000 } }"),
                                                 projection("{'field':'_id'}"));
        Assert.assertEquals(1,upd.getNumUpdated());
        Assert.assertEquals(0,upd.getNumFailed());
        Assert.assertEquals(ctx.getDocuments().get(0).getOutputDocument().get(new Path("_id")).asText(),
                            coll.find(new BasicDBObject("field3",1000),new BasicDBObject("_id",1)).next().get("_id").toString());
        Assert.assertEquals(1,coll.find(new BasicDBObject("field3",1000)).count());

        // Bulk update
        ctx=new OCtx();
        ctx.add(md);
        upd=controller.update(ctx,query("{'field':'field3','op':'>','rvalue':10}"),
                              update("{ '$set': { 'field3' : 1000 } }"),
                              projection("{'field':'_id'}"));
        Assert.assertEquals(10,upd.getNumUpdated());
        Assert.assertEquals(0,upd.getNumFailed());
        Assert.assertEquals(10,coll.find(new BasicDBObject("field3",new BasicDBObject("$gt",10))).count());
    }

    @Test
    public void sortAndPageTest() throws Exception {
        DB db = mongo.getDB(DB_NAME);
        DBCollection coll=db.getCollection(COLL_NAME);
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
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
        controller.insert(ctx,projection("{'field':'_id'}"));
        
        ctx=new OCtx();
        ctx.add(md);
        CRUDFindResponse response=controller.find(ctx,query("{'field':'field3','op':'>=','rvalue':0}"),
                                                  projection("{'field':'*','recursive':1}"),
                                                  sort("{'field3':'$desc'}"),null,null);
        Assert.assertEquals(numDocs,response.getResults().size());
        int lastValue=-1;
        for(JsonDoc doc:response.getResults()) {
            int value=doc.get(new Path("field3")).asInt();
            if(value<lastValue) {
                Assert.fail("wrong order");
            }
        }

        for(int k=0;k<15;k++) {
            ctx=new OCtx();
            ctx.add(md);
            response=controller.find(ctx,query("{'field':'field3','op':'>=','rvalue':0}"),
                                     projection("{'field':'*','recursive':1}"),
                                     sort("{'field3':'$asc'}"),new Long(k),new Long(k+5));
            
            int i=0;
            for(JsonDoc doc:response.getResults()) {
                int value=doc.get(new Path("field3")).asInt();
                Assert.assertEquals(i+k,value);
                i++;
            }
        }
    }

    @Test
    public void deleteTest() throws Exception {
        DB db = mongo.getDB(DB_NAME);
        DBCollection coll=db.getCollection(COLL_NAME);
        EntityMetadata md=getMd("./testMetadata.json");
        OCtx ctx=new OCtx();
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
        controller.insert(ctx,projection("{'field':'_id'}"));
        Assert.assertEquals(numDocs,coll.find(null).count());

        // Single doc delete
        ctx=new OCtx();
        ctx.add(md);
        CRUDDeleteResponse del=controller.delete(ctx,query("{'field':'field3','op':'$eq','rvalue':10}"));
        Assert.assertEquals(1,del.getNumDeleted());
        Assert.assertEquals(numDocs-1,coll.find(null).count());

        // Bulk delete
        ctx=new OCtx();
        ctx.add(md);
        del=controller.delete(ctx,query("{'field':'field3','op':'>','rvalue':10}"));
        Assert.assertEquals(9,del.getNumDeleted());
        Assert.assertEquals(10,coll.find(null).count());
    }
}
