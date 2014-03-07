package com.redhat.lightblue.metadata.mongo;

import org.junit.After;
import org.junit.Before;
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
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.*;
import com.redhat.lightblue.mongo.MongoConfiguration;

public class MongoMetadataTest {

    // Copied from  https://github.com/tommysdk/showcase/blob/master/mongo-in-mem/src/test/java/tommysdk/showcase/mongo/TestInMemoryMongo.java
    private static final String MONGO_HOST = "localhost";
    private static final int MONGO_PORT = 27777;
    private static final String IN_MEM_CONNECTION_URL = MONGO_HOST + ":" + MONGO_PORT;

    private static final String DB_NAME = "testmetadata";

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private Mongo mongo;
    private DB db;

    private MongoMetadata md;

    @Before
    public void setup() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
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
        Extensions<BSONObject> x = new Extensions<>();
        x.addDefaultExtensions();
        x.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        md = new MongoMetadata(db, x, new DefaultTypes());
        BasicDBObject index = new BasicDBObject("name", 1);
        index.put("version.value", 1);
        db.getCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION).ensureIndex(index, "name", true);
    }

    @After
    public void teardown() throws Exception {
        if (mongod != null) {
            mongo.dropDatabase(DB_NAME);
            mongod.stop();
            mongodExe.stop();
        }
        db = null;
        mongo = null;
        mongod = null;
        mongodExe = null;
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
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0");
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
    public void unknownVersionTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        try {
            EntityMetadata g = md.getEntityMetadata("testEntity", "1.1");
            Assert.fail("expected "+MongoMetadataConstants.ERR_UNKNOWN_VERSION);
        } catch(Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_UNKNOWN_VERSION, ex.getErrorCode());
        }
    }

    @Test
    public void inactiveVersionTest() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.DEPRECATED);
        e.setDataStore(new MongoDataStore(null, null, "testCollection"));
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewMetadata(e);
        try {
            EntityMetadata g = md.getEntityMetadata("testEntity", "1.0");
            Assert.fail("expected "+MongoMetadataConstants.ERR_INACTIVE_VERSION);
        } catch(Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_INACTIVE_VERSION, ex.getErrorCode());
        }

        //with non-existant default. need to add Inactive default test too
        EntityMetadata eDefault = new EntityMetadata("testDefaultEntity");
        eDefault.setVersion(new Version("1.0", null, "some text blah blah"));
        eDefault.setStatus(MetadataStatus.DISABLED);
        eDefault.setDataStore(new MongoDataStore(null, null, "testCollection"));
        eDefault.getFields().put(new SimpleField("field1", StringType.TYPE));
        eDefault.getEntityInfo().setDefaultVersion("blah");
        md.createNewMetadata(eDefault);
        try {
            EntityMetadata g = md.getEntityMetadata("testDefaultEntity", "1.0");
            Assert.fail("expected "+MongoMetadataConstants.ERR_INACTIVE_VERSION);
        } catch(Error ex) {
            Assert.assertEquals(MongoMetadataConstants.ERR_INACTIVE_VERSION, ex.getErrorCode());
        }
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
        eDeprecated.setStatus(MetadataStatus.DEPRECATED);
        eDeprecated.setDataStore(new MongoDataStore(null, null, "testCollection"));
        eDeprecated.getFields().put(new SimpleField("field1", StringType.TYPE));
        md.createNewSchema(eDeprecated);
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.1");
        Assert.assertEquals("1.0", g.getVersion().getValue());
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
        EntityMetadata g = md.getEntityMetadata("testEntity", "1.0");
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
