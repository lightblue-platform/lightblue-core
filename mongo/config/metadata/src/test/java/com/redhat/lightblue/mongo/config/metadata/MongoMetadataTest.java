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
package com.redhat.lightblue.mongo.config.metadata;

import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.process.runtime.Network;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.BasicDBObject;

import org.bson.BSONObject;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.mongo.MongoMetadataConstants;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.*;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;

public class MongoMetadataTest {

    public static class FileStreamProcessor implements IStreamProcessor {
        private FileOutputStream outputStream;

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
            mongodExe = runtime.prepare(new MongodConfig(de.flapdoodle.embed.mongo.distribution.Version.V2_0_5, MONGO_PORT, Network.localhostIsIPv6()));
            mongod = mongodExe.start();
            mongo = new Mongo(IN_MEM_CONNECTION_URL);

            MongoConfiguration config = new MongoConfiguration();
            config.setName(DB_NAME);
            // disable ssl for test (enabled by default)
            config.setSsl(Boolean.FALSE);
            config.addServerAddress(MONGO_HOST, MONGO_PORT);

            db = config.getDB();

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
        md = new MongoMetadata(db, x, new DefaultTypes());
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
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        e.getEntityInfo().setDefaultVersion("1.0");
        md.createNewMetadata(e);

        EntityMetadata eDeprecated = new EntityMetadata("testEntity");
        eDeprecated.setVersion(new Version("1.1", null, "some text blah blah"));
        eDeprecated.setStatus(MetadataStatus.DISABLED);
        eDeprecated.setDataStore(new MongoDataStore(null, null, "testCollection"));
        eDeprecated.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewSchema(eDeprecated);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.1", false);
        Assert.assertEquals("1.0", g.getVersion().getValue());
    }

    @Test
    public void createMdTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0", false);
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
        Assert.assertEquals("1.0", v[0].getValue());

        String[] names = md.getEntityNames();
        Assert.assertEquals(1, names.length);
        Assert.assertEquals("testEntity", names[0]);
    }

    @Test
    public void updateStatusTest() throws Exception {
        EntityMetadata e2 = new EntityMetadata("testEntity");
        e2.setVersion(new Version("1.1", null, "some text blah blah"));
        e2.setStatus(MetadataStatus.ACTIVE);
        e2.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e2.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e2);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.1", true);
        Assert.assertEquals(MetadataStatus.ACTIVE, g.getStatus());

        md.setMetadataStatus("testEntity", "1.1", MetadataStatus.DEPRECATED, "disable testEntity");
        EntityMetadata g1 = md.getEntityMetadata("testEntity", "1.1", true);
        Assert.assertEquals(e2.getVersion().getValue(), g1.getVersion().getValue());
        Assert.assertEquals(MetadataStatus.DEPRECATED, g1.getStatus());
    }

    @Test
    public void disabledDefaultUpdateTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        e.getEntityInfo().setDefaultVersion("1.0");
        md.createNewMetadata(e);
        EntityMetadata g1 = md.getEntityMetadata("testEntity", "1.0", true);
        Assert.assertEquals(e.getVersion().getValue(), g1.getVersion().getValue());
        Assert.assertEquals(MetadataStatus.ACTIVE, g1.getStatus());
        try {
            md.setMetadataStatus("testEntity", "1.0", MetadataStatus.DISABLED, "disabling the default version");
            Assert.fail("expected " + MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void disabledDefaultCreationTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.DISABLED);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        e.getEntityInfo().setDefaultVersion("1.0");
        try {
            md.createNewMetadata(e);
            Assert.fail("expected " + MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void unknownVersionTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.1", true);
        Assert.assertEquals("testEntity", g.getEntityInfo().getName());
        Assert.assertNull(g.getEntitySchema());
    }

    @Test
    public void disabledVersionTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.DISABLED);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        try {
            EntityMetadata g = md.getEntityMetadata("testEntity", "1.0", false);
            Assert.fail("expected " + MongoMetadataConstants.ERR_DISABLED_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_DISABLED_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void invalidDefaultVersionTest() throws Exception {
        //with non-existant default.
        EntityMetadata eDefault = new EntityMetadata("testDefaultEntity");
        eDefault.setVersion(new Version("1.0", null, "some text blah blah"));
        eDefault.setStatus(MetadataStatus.DISABLED);
        eDefault.setDataStore(new MongoDataStore(null, null, "testCollection"));
        eDefault.getFields().put(new SimpleField("field1", StringType.TYPE));
        eDefault.getEntityInfo().setDefaultVersion("blah");
        try {
            md.createNewMetadata(eDefault);
            Assert.fail("expected " + MongoMetadataConstants.ERR_INVALID_DEFAULT_VERSION);
        } catch (Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_INVALID_DEFAULT_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void multipleVersions() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        md.createNewMetadata(e);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0", true);
        Assert.assertNotNull("Can't retrieve entity", g);
        Assert.assertEquals(e.getName(), g.getName());
        Assert.assertEquals(e.getVersion().getValue(), g.getVersion().getValue());
        Version[] v = md.getEntityVersions("testEntity");
        Assert.assertEquals(1, v.length);
        Assert.assertEquals("1.0", v[0].getValue());
        e.setVersion(new Version("2.0", null, "blahblahyadayada"));
        md.createNewSchema(e);
        v = md.getEntityVersions("testEntity");
        Assert.assertEquals(2, v.length);
        try {
            md.createNewMetadata(e);
            Assert.fail();
        } catch (Exception x) {
        }
    }

}
