/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.mongo.MongoConfiguration;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonUtils;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author nmalik
 */
public abstract class AbstractMongoTest extends AbstractJsonSchemaTest {
    protected static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(true);

    // Copied from  https://github.com/tommysdk/showcase/blob/master/mongo-in-mem/src/test/java/tommysdk/showcase/mongo/TestInMemoryMongo.java
    protected static final String MONGO_HOST = "localhost";
    protected static final int MONGO_PORT = 27777;
    protected static final String IN_MEM_CONNECTION_URL = MONGO_HOST + ":" + MONGO_PORT;

    protected static final String DB_NAME = "test";
    protected static final String COLL_NAME = "data";

    protected static MongodExecutable mongodExe;
    protected static MongodProcess mongod;
    protected static Mongo mongo;
    protected static DB db;
    protected static DBCollection coll;

    protected static Factory factory;

    protected class OCtx extends CRUDOperationContext {
        private final Map<String, EntityMetadata> map = new HashMap<>();

        public OCtx(Operation op) {
            super(op, "test", factory, new HashSet<String>(), null);
        }

        public void add(EntityMetadata md) {
            map.put(md.getName(), md);
        }

        @Override
        public EntityMetadata getEntityMetadata(String entityName) {
            return map.get(entityName);
        }
    }

    @BeforeClass
    public static void setupClass() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(de.flapdoodle.embed.mongo.distribution.Version.V2_4_3, MONGO_PORT, false));
        mongod = mongodExe.start();
        mongo = new Mongo(IN_MEM_CONNECTION_URL);

        MongoConfiguration config = new MongoConfiguration();
        config.setName(DB_NAME);
        // disable ssl for test (enabled by default)
        config.setSsl(Boolean.FALSE);
        config.addServerAddress(MONGO_HOST, MONGO_PORT);

        db = config.getDB();

        coll = db.createCollection(COLL_NAME, null);

        factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        if (mongod != null) {
            mongod.stop();
            mongodExe.stop();
        }
        db = null;
        mongo = null;
        mongod = null;
        mongodExe = null;
    }

    @After
    public void teardown() throws Exception {
        if (mongod != null) {
            mongo.dropDatabase(DB_NAME);
        }
    }

    protected Projection projection(String s) throws Exception {
        return Projection.fromJson(json(s));
    }

    protected QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(json(s));
    }

    protected UpdateExpression update(String s) throws Exception {
        return UpdateExpression.fromJson(json(s));
    }

    protected Sort sort(String s) throws Exception {
        return Sort.fromJson(json(s));
    }

    protected JsonNode json(String s) throws Exception {
        return JsonUtils.json(s.replace('\'', '\"'));
    }

    protected EntityMetadata getMd(String fname) throws IOException, ProcessingException  {
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

}
