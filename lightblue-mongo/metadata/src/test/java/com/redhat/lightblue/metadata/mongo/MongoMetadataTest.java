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
package com.redhat.lightblue.metadata.mongo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.bson.BSONObject;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Index;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.common.mongo.MongoDataStore;
import com.redhat.lightblue.common.mongo.DBResolver;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;

public class MongoMetadataTest {

    public static class FileStreamProcessor implements IStreamProcessor {
        private final FileOutputStream outputStream;

        public FileStreamProcessor(File file) throws FileNotFoundException {
            outputStream = new FileOutputStream(file);
        }

        @Override
        public void process(String block) {
            try {
                outputStream.write(block.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void onProcessed() {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    // Copied from  https://github.com/tommysdk/showcase/blob/master/mongo-in-mem/src/test/java/tommysdk/showcase/mongo/TestInMemoryMongo.java
    private static final String MONGO_HOST = "localhost";
    private static final int MONGO_PORT = 27777;
    private static final String IN_MEM_CONNECTION_URL = MONGO_HOST + ":" + MONGO_PORT;

    private static final String DB_NAME = "testmetadata";

    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;
    private static Mongo mongo;
    private static DB db;

    private MongoMetadata md;

    static {
        try {
            IStreamProcessor mongodOutput = Processors.named("[mongod>]",
                    new FileStreamProcessor(File.createTempFile("mongod", "log")));
            IStreamProcessor mongodError = new FileStreamProcessor(File.createTempFile("mongod-error", "log"));
            IStreamProcessor commandsOutput = Processors.namedConsole("[console>]");

            IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD)
                    .processOutput(new ProcessOutput(mongodOutput, mongodError, commandsOutput))
                    .build();

            MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
            mongodExe = runtime.prepare(
                    new MongodConfigBuilder()
                            .version(de.flapdoodle.embed.mongo.distribution.Version.V2_6_0)
                            .net(new Net(MONGO_PORT,Network.localhostIsIPv6()))
                            .build()
            );
            try {
                mongod = mongodExe.start();
            } catch (Throwable t) {
                // try again, could be killed breakpoint in IDE
                mongod = mongodExe.start();
            }
            mongo = new Mongo(IN_MEM_CONNECTION_URL);
            db = mongo.getDB(DB_NAME);

            db.createCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION, null);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    super.run();
                    clearDatabase();
                }

            });
        } catch (IOException e) {
            throw new java.lang.Error(e);
        }
    }

    @Before
    public void setup() {
        Extensions<BSONObject> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        md = new MongoMetadata(db, new DBResolver() {
            public DB get(MongoDataStore ds) {
                return db;
            }
        }, x, new DefaultTypes());
        BasicDBObject index = new BasicDBObject("name", 1);
        index.put("version.value", 1);
        db.getCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION).ensureIndex(index, "name", true);
    }

    @After
    public void teardown() {
        if (mongo != null) {
            mongo.dropDatabase(DB_NAME);
        }
    }

    public static void clearDatabase() {
        if (mongod != null) {
            mongod.stop();
            mongodExe.stop();
        }
        db = null;
        mongo = null;
        mongod = null;
        mongodExe = null;
    }

    @Test
    public void defaultVersionTest() throws Exception {

        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        e.getEntityInfo().setDefaultVersion("1.0.0");
        md.createNewMetadata(e);

        EntityMetadata g = md.getEntityMetadata("testEntity", null);
        Assert.assertEquals("1.0.0", g.getVersion().getValue());
    }

    @Test
    public void createMdTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0.0");
        Assert.assertNotNull("Can't retrieve entity", g);
        Assert.assertEquals(e.getName(), g.getName());
        Assert.assertEquals(e.getVersion().getValue(), g.getVersion().getValue());
        Assert.assertEquals(e.getVersion().getChangelog(), g.getVersion().getChangelog());
        Assert.assertEquals(e.getStatus(), g.getStatus());
        Assert.assertEquals(((SimpleField) e.resolve(new Path("field1"))).getType(),
                ((SimpleField) g.resolve(new Path("field1"))).getType());
        Assert.assertEquals(((SimpleField) e.resolve(new Path("field2.x"))).getType(),
                ((SimpleField) g.resolve(new Path("field2.x"))).getType());
        Version[] v = md.getEntityVersions("testEntity");
        Assert.assertEquals(1, v.length);
        Assert.assertEquals("1.0.0", v[0].getValue());

        String[] names = md.getEntityNames();
        Assert.assertEquals(1, names.length);
        Assert.assertEquals("testEntity", names[0]);
    }

    /**
     * Issue #13: if you create it twice, the error thrown for the second one cleans up the first
     */
    @Test
    public void createMd2Test() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        Assert.assertNotNull(md.getEntityMetadata("testEntity", "1.0.0"));
        try {
            md.createNewMetadata(e);
            Assert.fail();
        } catch (Exception x) {}
        Assert.assertNotNull(md.getEntityMetadata("testEntity", "1.0.0"));
    }

    @Test
    public void updateStatusTest() throws Exception {
        EntityMetadata e2 = new EntityMetadata("testEntity");
        e2.setVersion(new Version("1.1.0", null, "some text blah blah"));
        e2.setStatus(MetadataStatus.ACTIVE);
        e2.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e2.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e2);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.1.0");
        Assert.assertEquals(MetadataStatus.ACTIVE, g.getStatus());

        md.setMetadataStatus("testEntity", "1.1.0", MetadataStatus.DEPRECATED, "disable testEntity");
        EntityMetadata g1 = md.getEntityMetadata("testEntity", "1.1.0");
        Assert.assertEquals(e2.getVersion().getValue(), g1.getVersion().getValue());
        Assert.assertEquals(MetadataStatus.DEPRECATED, g1.getStatus());
    }

    @Test
    public void disabledDefaultUpdateTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        e.getEntityInfo().setDefaultVersion("1.0.0");
        md.createNewMetadata(e);
        EntityMetadata g1 = md.getEntityMetadata("testEntity", "1.0.0");
        Assert.assertEquals(e.getVersion().getValue(), g1.getVersion().getValue());
        Assert.assertEquals(MetadataStatus.ACTIVE, g1.getStatus());
        try {
            md.setMetadataStatus("testEntity", "1.0.0", MetadataStatus.DISABLED, "disabling the default version");
            Assert.fail("expected " + MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void disabledDefaultCreationTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.DISABLED);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        e.getEntityInfo().setDefaultVersion("1.0.0");
        try {
            md.createNewMetadata(e);
            Assert.fail("expected " + MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void illegalArgumentTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        try {
            md.getEntityMetadata("testEntity", "");
            Assert.fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("version", ex.getMessage());
        }

        try {
            md.getEntityMetadata("testEntity", null);
            Assert.fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("version", ex.getMessage());
        }

    }

    @Test
    public void unknownVersionTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        try {
            md.getEntityMetadata("testEntity", "1.1.0");
            Assert.fail("expected " + MongoMetadataConstants.ERR_UNKNOWN_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_UNKNOWN_VERSION, ex.getErrorCode());
        }

    }

    @Test
    public void updateEntityInfo() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);

        EntityInfo ei = new EntityInfo("testEntity");
        ei.setDataStore(new MongoDataStore(null, null, null, "somethingelse"));
        md.updateEntityInfo(ei);
    }

    @Test
    public void updateEntityInfo_noEntity() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);

        EntityInfo ei = new EntityInfo("NottestEntity");
        ei.setDataStore(new MongoDataStore(null, null, null, "somethingelse"));
        try {
            md.updateEntityInfo(ei);
            Assert.fail();
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_MISSING_ENTITY_INFO, ex.getErrorCode());
        }
    }

    @Test
    public void invalidDefaultVersionTest() throws Exception {
        //with non-existant default.
        EntityMetadata eDefault = new EntityMetadata("testDefaultEntity");
        eDefault.setVersion(new Version("1.0.0", null, "some text blah blah"));
        eDefault.setStatus(MetadataStatus.DISABLED);
        eDefault.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        eDefault.getFields().put(new SimpleField("field1", StringType.TYPE));
        eDefault.getEntityInfo().setDefaultVersion("blah");
        try {
            md.createNewMetadata(eDefault);
            Assert.fail("expected " + MetadataConstants.ERR_INVALID_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MetadataConstants.ERR_INVALID_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void multipleVersions() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0.0");
        Assert.assertNotNull("Can't retrieve entity", g);
        Assert.assertEquals(e.getName(), g.getName());
        Assert.assertEquals(e.getVersion().getValue(), g.getVersion().getValue());
        Version[] v = md.getEntityVersions("testEntity");
        Assert.assertEquals(1, v.length);
        Assert.assertEquals("1.0.0", v[0].getValue());
        e.setVersion(new Version("2.0.0", null, "blahblahyadayada"));
        md.createNewSchema(e);
        v = md.getEntityVersions("testEntity");
        Assert.assertEquals(2, v.length);
        try {
            md.createNewMetadata(e);
            Assert.fail();
        } catch (Exception x) {
        }
    }

     @Test
     public void removal() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0.0");
        e.setVersion(new Version("2.0.0", null, "blahblahyadayada"));
        md.createNewSchema(e);

        try {
            md.removeEntity("testEntity");
            Assert.fail();
        } catch (Exception x) {}

        md.setMetadataStatus("testEntity","1.0.0",MetadataStatus.DEPRECATED,"x");
         try {
            md.removeEntity("testEntity");
            Assert.fail();
        } catch (Exception x) {}

        md.setMetadataStatus("testEntity","2.0.0",MetadataStatus.DISABLED,"x");
         try {
            md.removeEntity("testEntity");
            Assert.fail();
        } catch (Exception x) {}
         md.setMetadataStatus("testEntity","1.0.0",MetadataStatus.DISABLED,"x");
         md.removeEntity("testEntity");
         Assert.assertNull(md.getEntityInfo("testEntity"));
    }


   @Test
    public void getAccessEntityVersion() throws IOException, JSONException {
        // setup parser
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), new JsonNodeFactory(true));

        // get JsonNode representing metadata
        JsonNode jsonMetadata = AbstractJsonNodeTest.loadJsonNode(getClass().getSimpleName() + "-access-entity-version.json");

        // parser into EntityMetadata
        EntityMetadata e = parser.parseEntityMetadata(jsonMetadata);

        // persist
        md.createNewMetadata(e);

        // ready to test!
        Response response = md.getAccess(e.getName(), e.getVersion().getValue());

        Assert.assertNotNull(response);

        // verify response content
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertTrue(response.getDataErrors().isEmpty());

        // verify data
        Assert.assertNotNull(response.getEntityData());
        String jsonEntityData = response.getEntityData().toString();
        String jsonExpected = "[{\"role\":\"field.find\",\"find\":[\"test.name\"]},{\"role\":\"field.update\",\"update\":[\"test.name\"]},{\"role\":\"noone\",\"update\":[\"test.object_type\"]},{\"role\":\"anyone\",\"find\":[\"test.object_type\"]},{\"role\":\"entity.insert\",\"insert\":[\"test\"]},{\"role\":\"entity.update\",\"update\":[\"test\"]},{\"role\":\"entity.find\",\"find\":[\"test\"]},{\"role\":\"entity.delete\",\"delete\":[\"test\"]}]";
        JSONAssert.assertEquals(jsonExpected, jsonEntityData, false);
    }

    @Test
    public void getAccessEntityMissingDefaultVersion() throws IOException, JSONException {
        // setup parser
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), new JsonNodeFactory(true));

        // get JsonNode representing metadata
        JsonNode jsonMetadata = AbstractJsonNodeTest.loadJsonNode(getClass().getSimpleName() + "-access-entity-missing-default-version.json");

        // parser into EntityMetadata
        EntityMetadata e = parser.parseEntityMetadata(jsonMetadata);

        // persist
        md.createNewMetadata(e);

        // ready to test!
        Response response = md.getAccess(e.getName(), null);

        Assert.assertNotNull(response);

        // verify response content
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertFalse(response.getDataErrors().isEmpty());

        // verify data
        Assert.assertNull(response.getEntityData());
    }

    /**
     * TODO enable once mongo metadata allows falling back on default version in getEntityMetadata()
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void getAccessEntityDefaultVersion() throws IOException, JSONException {
        // setup parser
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), new JsonNodeFactory(true));

        // get JsonNode representing metadata
        JsonNode jsonMetadata = AbstractJsonNodeTest.loadJsonNode(getClass().getSimpleName() + "-access-entity-default-version.json");

        // parser into EntityMetadata
        EntityMetadata e = parser.parseEntityMetadata(jsonMetadata);

        // persist
        md.createNewMetadata(e);

        // ready to test!
        Response response = md.getAccess(e.getName(), null);

        Assert.assertNotNull(response);

        // verify response content
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertTrue(response.getDataErrors().isEmpty());

        // verify data
        Assert.assertNotNull(response.getEntityData());
        String jsonEntityData = response.getEntityData().toString();
        String jsonExpected = "[{\"role\":\"field.find\",\"find\":[\"test.name\"]},{\"role\":\"field.update\",\"update\":[\"test.name\"]},{\"role\":\"noone\",\"update\":[\"test.object_type\"]},{\"role\":\"anyone\",\"find\":[\"test.object_type\"]},{\"role\":\"entity.insert\",\"insert\":[\"test\"]},{\"role\":\"entity.update\",\"update\":[\"test\"]},{\"role\":\"entity.find\",\"find\":[\"test\"]},{\"role\":\"entity.delete\",\"delete\":[\"test\"]}]";
        JSONAssert.assertEquals(jsonExpected, jsonEntityData, false);
    }

    /**
     * TODO enable once mongo metadata allows falling back on default version in getEntityMetadata()
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void getAccessSingleEntityDefaultVersion() throws IOException, JSONException {
        // setup parser
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), new JsonNodeFactory(true));

        // get JsonNode representing metadata
        JsonNode jsonMetadata = AbstractJsonNodeTest.loadJsonNode(getClass().getSimpleName() + "-access-single-entity-default-version.json");

        // parser into EntityMetadata
        EntityMetadata e = parser.parseEntityMetadata(jsonMetadata);
        // persist
        md.createNewMetadata(e);

        // ready to test!
        Response response = md.getAccess(null, null);

        Assert.assertNotNull(response);

        // verify response content
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertTrue(response.getDataErrors().isEmpty());

        // verify data
        Assert.assertNotNull(response.getEntityData());
        String jsonEntityData = response.getEntityData().toString();
        String jsonExpected = "[{\"role\":\"field.find\",\"find\":[\"test.name\"]},{\"role\":\"field.update\",\"update\":[\"test.name\"]},{\"role\":\"noone\",\"update\":[\"test.object_type\"]},{\"role\":\"anyone\",\"find\":[\"test.object_type\"]},{\"role\":\"entity.insert\",\"insert\":[\"test\"]},{\"role\":\"entity.update\",\"update\":[\"test\"]},{\"role\":\"entity.find\",\"find\":[\"test\"]},{\"role\":\"entity.delete\",\"delete\":[\"test\"]}]";
        JSONAssert.assertEquals(jsonExpected, jsonEntityData, false);
    }

    /**
     * TODO enable once mongo metadata allows falling back on default version in getEntityMetadata()
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void getAccessMultipleEntitiesDefaultVersion() throws IOException, JSONException {
        // setup parser
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), new JsonNodeFactory(true));

        // get JsonNode representing metadata
        JsonNode jsonMetadata = AbstractJsonNodeTest.loadJsonNode(getClass().getSimpleName() + "-access-multiple-entities-default-version.json");

        ArrayNode an = (ArrayNode) jsonMetadata;
        Iterator<JsonNode> itr = an.iterator();
        while (itr.hasNext()) {
            // parser into EntityMetadata
            EntityMetadata e = parser.parseEntityMetadata(itr.next());

            // persist
            md.createNewMetadata(e);
        }

        // ready to test!
        Response response = md.getAccess(null, null);

        Assert.assertNotNull(response);

        // verify response content
        Assert.assertEquals(OperationStatus.PARTIAL, response.getStatus());
        Assert.assertFalse(response.getDataErrors().isEmpty());
        Assert.assertEquals(1, response.getDataErrors().size());
        String jsonErrorExpected = "[{\"data\":{\"name\":\"test2\"},\"errors\":[{\"object_type\":\"error\",\"errorCode\":\"ERR_NO_METADATA\",\"msg\":\"Could not get metadata for given input. Error message: version\"}]}]";
        JSONAssert.assertEquals(jsonErrorExpected, response.getDataErrors().toString(), false);

        // verify data
        Assert.assertNotNull(response.getEntityData());
        String jsonEntityData = response.getEntityData().toString();
        String jsonExpected = "[{\"role\":\"field.find\",\"find\":[\"test1.name\",\"test3.name\"]},{\"role\":\"noone\",\"update\":[\"test1.object_type\",\"test3.object_type\"]},{\"role\":\"field.update\",\"update\":[\"test1.name\",\"test3.name\"]},{\"role\":\"anyone\",\"find\":[\"test1.object_type\",\"test3.object_type\"]},{\"role\":\"entity.insert\",\"insert\":[\"test1\",\"test3\"]},{\"role\":\"entity.update\",\"update\":[\"test1\",\"test3\"]},{\"role\":\"entity.find\",\"find\":[\"test1\",\"test3\"]},{\"role\":\"entity.delete\",\"delete\":[\"test1\",\"test3\"]}]";
        JSONAssert.assertEquals(jsonExpected, jsonEntityData, false);
    }

    @Test
    public void entityIndexCreationTest() throws Exception {

        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollectionIndex1"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        e.getEntityInfo().setDefaultVersion("1.0.0");
        Index index = new Index();
        index.setName("testIndex");
        index.setUnique(true);
        List<SortKey> indexFields = new ArrayList<>();
        //TODO actually parse $asc/$desc here
        indexFields.add(new SortKey(new Path("field1"), true));
        index.setFields(indexFields);
        Collection<Index> indexes = new LinkedHashSet<>();
        indexes.add(index);
        e.getEntityInfo().getIndexes().setIndexes(indexes);
        md.createNewMetadata(e);

        DBCollection entityCollection = db.getCollection("testCollectionIndex1");

        boolean foundIndex = false;

        for (DBObject mongoIndex : entityCollection.getIndexInfo()) {
            if ("testIndex".equals(mongoIndex.get("name"))) {
                if (mongoIndex.get("key").toString().contains("field1")) {
                    foundIndex = true;
                }
            }
        }
        Assert.assertTrue(foundIndex);
    }

    @Test
    public void entityIndexUpdateTest() throws Exception {

        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, null, "testCollectionIndex2"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        e.getEntityInfo().setDefaultVersion("1.0.0");
        Index index = new Index();
        index.setName("testIndex");
        index.setUnique(true);
        List<SortKey> indexFields = new ArrayList<>();
        indexFields.add(new SortKey(new Path("field1"), true));
        index.setFields(indexFields);
        Collection<Index> indexes = new LinkedHashSet<>();
        indexes.add(index);
        e.getEntityInfo().getIndexes().setIndexes(indexes);
        md.createNewMetadata(e);

        DBCollection entityCollection = db.getCollection("testCollectionIndex2");

        index = new Index();
        index.setName("testIndex2");
        index.setUnique(false);
        indexFields = new ArrayList<>();
        indexFields.clear();
        indexFields.add(new SortKey(new Path("field1"), true));
        index.setFields(indexFields);
        indexes = new LinkedHashSet<>();
        indexes.add(index);
        e.getEntityInfo().getIndexes().setIndexes(indexes);

        md.updateEntityInfo(e.getEntityInfo());

        boolean foundIndex = false;

        for (DBObject mongoIndex : entityCollection.getIndexInfo()) {
            if ("testIndex2".equals(mongoIndex.get("name"))) {
                if (mongoIndex.get("key").toString().contains("field1")) {
                    foundIndex = true;
                }
            }
        }
        Assert.assertTrue(foundIndex);

        foundIndex = false;

        for (DBObject mongoIndex : entityCollection.getIndexInfo()) {
            if ("testIndex".equals(mongoIndex.get("name"))) {
                if (mongoIndex.get("key").toString().contains("field1")) {
                    foundIndex = true;
                }
            }
        }
        Assert.assertTrue(!foundIndex);
    }

}
