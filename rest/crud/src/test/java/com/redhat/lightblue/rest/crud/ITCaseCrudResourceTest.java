/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * This file is part of lightblue.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rest.crud;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.redhat.lightblue.config.metadata.MetadataConfiguration;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.mongo.config.metadata.MongoConfiguration;
import com.redhat.lightblue.util.JsonUtils;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 *
 * @author lcestari
 */
@RunWith(Arquillian.class)
public class ITCaseCrudResourceTest {

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
        db.createCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION, null);
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

    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new File("src/test/resources/lightblue-crud.json"), MetadataConfiguration.FILENAME)
                .addAsResource(EmptyAsset.INSTANCE, "resources/test.properties");

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");
        return archive;

    }

    @Inject
    private CrudResource cutCrudResource; //class under test

    @Test
    public void testFirstIntegrationTest() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        assertNotNull("CrudResource was not injected by the container", cutCrudResource);
        String expectedCreated = "{\"entityInfo\":{\"name\":\"country\",\"indexes\":[{\"name\":null,\"unique\":true,\"fields\":[\"name\"]}],\"datastore\":{\"mongo\":{\"collection\":\"country\"}}},\"schema\":{\"name\":\"country\",\"version\":{\"value\":\"1.0.0\",\"changelog\":\"blahblah\"},\"status\":{\"value\":\"active\"},\"access\":{\"insert\":[\"anyone\"],\"update\":[\"anyone\"],\"find\":[\"anyone\"],\"delete\":[\"anyone\"]},\"fields\":{\"iso3code\":{\"type\":\"string\"},\"iso2code\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"object_type\":{\"type\":\"string\",\"access\":{\"find\":[\"anyone\"],\"update\":[\"noone\"]},\"constraints\":{\"required\":true,\"minLength\":1}}}}}";
        String metadata =
"{\n" +
"   \"entityInfo\": { "+
"        \"name\": \"country\",\n" +
"        \"indexes\": [\n" +
"            {\n" +
"                \"unique\": true,\n" +
"                \"fields\": [\"name\"]\n" +
"            }\n" +
"        ],\n" +
"        \"datastore\": {\n" +
"            \"mongo\": {\n" +
"                  \"collection\": \"country\"\n" +
"            }\n" +
"        }"+
"    },\n"+
"    \"schema\": {\n" +
"        \"name\": \"country\",\n"+
"        \"version\": {\n"+
"           \"value\": \"1.0.0\",\n"+
"           \"changelog\": \"blahblah\"\n"+
"       },\n"+
"        \"status\": {\n"+
"            \"value\": \"active\"\n"+
"       },\n"+
"        \"access\" : {\n" +
"             \"insert\" : [\"anyone\"],\n" +
"             \"update\" : [\"anyone\"],\n" +
"             \"delete\" : [ \"anyone\" ] ,\n" +
"             \"find\" : [ \"anyone\" ]\n" +
"        },"+
"        \"fields\": {\n"+
"            \"name\": {\"type\": \"string\"},\n"+
"            \"iso2code\": {\"type\": \"string\"},\n"+
"            \"iso3code\": {\"type\": \"string\"}\n"+
"        }\n" +
"    }\n" +
"}";

        EntityMetadata em = MetadataManager.getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));
        MetadataManager.getMetadata().createNewMetadata(em);
        EntityMetadata em2 = MetadataManager.getMetadata().getEntityMetadata("country", "1.0.0");
        String resultCreated = MetadataManager.getJSONParser().convert(em2).toString();
        assertEquals(expectedCreated,resultCreated);



        String expectedInserted = "{\"status\":\"COMPLETE\",\"modifiedCount\":1,\"matchCount\":0,\"processed\":{\"iso3code\":\"CAN\",\"iso2code\":\"CA\",\"name\":\"Canad\",\"object_type\":\"country\"}}";
        String resultInserted = cutCrudResource.insert(
"{\n" +
"    \"entity\": \"country\",\n" +
"    \"entityVersion\": \"1.0.0\",\n" +
"    \"data\": [\n" +
"        {\n" +
"            \"name\": \"Canad\",\n" +
"            \"iso2code\": \"CA\",\n" +
"            \"iso3code\": \"CAN\"\n" +
"        }\n" +
"    ],\n" +
"    \"returning\": [\n" +
"        {\n" +
"            \"field\": \"*\",\n" +
"            \"include\": true\n" +
"        }\n" +
"    ]\n" +
"}");
        assertEquals(expectedInserted,resultInserted);


        String expectedUpdated = "{\"status\":\"COMPLETE\",\"modifiedCount\":1,\"matchCount\":0,\"processed\":{\"name\":\"Canada\"}}";
        String resultUpdated = cutCrudResource.update(
"{\n" +
"        \"entity\": \"country\",\n" +
"        \"entityVersion\": \"1.0.0\",\n" +
"        \"query\": {\n" +
"            \"field\": \"iso2code\",\n" +
"                \"op\": \"=\",\n" +
"            \"rvalue\": \"CA\"\n" +
"        },\n" +
"        \"returning\": [\n" +
"            {\n" +
"                \"field\": \"name\",\n" +
"                \"include\": true\n" +
"            }\n" +
"        ],\n" +
"        \"update\": {\n" +
"            \"$set\": {\n" +
"                \"name\": \"Canada\"\n" +
"            }\n" +
"        }\n" +
"}");
        assertEquals(expectedUpdated,resultUpdated);




        String expectedFound = "{\"status\":\"COMPLETE\",\"modifiedCount\":0,\"matchCount\":1,\"processed\":{\"iso3code\":\"CAN\",\"name\":\"Canada\"}}";
        String resultFound = cutCrudResource.find(
"{\n" +
"    \"entity\": \"country\",\n" +
"    \"entityVersion\": \"1.0.0\",\n" +
"    \"query\": {\n" +
"        \"field\": \"iso2code\",\n" +
"        \"op\": \"=\",\n" +
"        \"rvalue\": \"CA\"\n" +
"    },\n" +
"    \"returning\": [\n" +
"        {\n" +
"            \"field\": \"name\",\n" +
"            \"include\": true\n" +
"        },\n" +
"        {\n" +
"            \"field\": \"iso3code\",\n" +
"            \"include\": true\n" +
"        }\n" +
"    ]\n" +
                        "}");
        assertEquals(expectedFound,resultFound);



        String expectedDeleted = "{\"status\":\"COMPLETE\",\"modifiedCount\":1,\"matchCount\":0}";
        String resultDeleted = cutCrudResource.delete(
"{\n" +
"    \"entity\": \"country\",\n" +
"    \"entityVersion\": \"1.0.0\",\n" +
"    \"query\": {\n" +
"        \"field\": \"iso2code\",\n" +
"        \"op\": \"=\",\n" +
"        \"rvalue\": \"CA\"\n" +
"    },\n" +
"    \"project\": [\n" +
"        {\n" +
"            \"field\": \"name\",\n" +
"            \"include\": true\n" +
"        },\n" +
"        {\n" +
"            \"field\": \"iso3code\",\n" +
"            \"include\": true\n" +
"        }\n" +
"    ]\n" +
"}");
        assertEquals(expectedDeleted,resultDeleted);



        String expectedFound2 = "{\"status\":\"COMPLETE\",\"modifiedCount\":0,\"matchCount\":0,\"processed\":[]}";
        String resultFound2 = cutCrudResource.find(
"{\n" +
"    \"entity\": \"country\",\n" +
"    \"entityVersion\": \"1.0.0\",\n" +
"    \"query\": {\n" +
"        \"field\": \"iso2code\",\n" +
"        \"op\": \"=\",\n" +
"        \"rvalue\": \"CA\"\n" +
"    },\n" +
"    \"returning\": [\n" +
"        {\n" +
"            \"field\": \"name\",\n" +
"            \"include\": true\n" +
"        },\n" +
"        {\n" +
"            \"field\": \"iso3code\",\n" +
"            \"include\": true\n" +
"        }\n" +
"    ]\n" +
"}");
        assertEquals(expectedFound2,resultFound2);
    }
}
