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
package com.redhat.lightblue.rest.metadata;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.redhat.lightblue.config.metadata.MetadataConfiguration;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.mongo.config.metadata.MongoConfiguration;
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
import java.net.URISyntaxException;

import static com.redhat.lightblue.util.test.FileUtil.readFile;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 *
 * @author lcestari
 */
@RunWith(Arquillian.class)
public class ITCaseMetadataResourceTest {

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
            try {
                mongod = mongodExe.start();
            } catch (Throwable t) {
                // try again, could be killed breakpoint in IDE
                mongod = mongodExe.start();
            }
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
            throw new Error(e);
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
                .addAsResource(new File("src/test/resources/lightblue-metadata.json"), MetadataConfiguration.FILENAME)
                .addAsResource(EmptyAsset.INSTANCE, "resources/test.properties");

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");
        return archive;

    }

    @Inject
    private MetadataResource cutMetadataResource;

    @Test
    public void testFirstIntegrationTest() throws IOException, URISyntaxException {
        assertNotNull("MetadataResource was not injected by the container", cutMetadataResource);

        String expectedCreated = readFile("expectedCreated.json");
        String resultCreated = cutMetadataResource.createMetadata("country", "1.0.0", readFile("resultCreated.json"));
        assertEquals(expectedCreated,resultCreated);

        String expectedDepGraph = readFile("expectedDepGraph.json").replace("Notsupportedyet"," Not supported yet");
        String resultDepGraph = cutMetadataResource.getDepGraph(); //Not implemented yet
        assertEquals(expectedDepGraph,resultDepGraph);

        String expectedDepGraph1 = readFile("expectedDepGraph1.json").replace("Notsupportedyet", " Not supported yet");
        String resultDepGraph1 = cutMetadataResource.getDepGraph("country"); //Not implemented yet
        assertEquals(expectedDepGraph1,resultDepGraph1);

        String expectedDepGraph2 = readFile("expectedDepGraph2.json").replace("Notsupportedyet", " Not supported yet");
        String resultDepGraph2 = cutMetadataResource.getDepGraph("country", "1.0.0"); //Not implemented yet
        assertEquals(expectedDepGraph2,resultDepGraph2);

        String expectedEntityNames = "{\"entities\":[\"country\"]}";
        String resultEntityNames = cutMetadataResource.getEntityNames();
        assertEquals(expectedEntityNames,resultEntityNames);

        //Following getEntityRoles calls shows some implementation problem, TODO to solve these problems
        String expectedEntityRoles = "{\"status\":\"ERROR\",\"modifiedCount\":0,\"matchCount\":0,\"dataErrors\":[\"{\\\"data\\\":{\\\"name\\\":\\\"country\\\"},\\\"errors\\\":[{\\\"object_type\\\":\\\"error\\\",\\\"context\\\":\\\"GetEntityRolesCommand\\\",\\\"errorCode\\\":\\\"ERR_NO_METADATA\\\",\\\"msg\\\":\\\"Could not get metadata for given input. Error message: version\\\"}]}\"]}";
        String expectedEntityRoles1 = "{\"status\":\"ERROR\",\"modifiedCount\":0,\"matchCount\":0,\"dataErrors\":[\"{\\\"data\\\":{\\\"name\\\":\\\"country\\\"},\\\"errors\\\":[{\\\"object_type\\\":\\\"error\\\",\\\"context\\\":\\\"GetEntityRolesCommand/country\\\",\\\"errorCode\\\":\\\"ERR_NO_METADATA\\\",\\\"msg\\\":\\\"Could not get metadata for given input. Error message: version\\\"}]}\"]}";

        String resultEntityRoles = cutMetadataResource.getEntityRoles();
        String resultEntityRoles1 = cutMetadataResource.getEntityRoles("country");

        assertEquals(expectedEntityRoles,resultEntityRoles);
        assertEquals(expectedEntityRoles1,resultEntityRoles1);
        //////////////////

        String expectedEntityRoles2 =  readFile("expectedEntityRoles2.json");
        String resultEntityRoles2 = cutMetadataResource.getEntityRoles("country","1.0.0");
        assertEquals(expectedEntityRoles2,resultEntityRoles2);

        String expectedEntityVersions = "{\"versions\":[{\"value\":\"1.0.0\",\"changelog\":\"blahblah\"}]}";
        String resultEntityVersions = cutMetadataResource.getEntityVersions("country");
        assertEquals(expectedEntityVersions,resultEntityVersions);

        String expectedGetMetadata = "{\"entityInfo\":{\"name\":\"country\",\"indexes\":[{\"name\":null,\"unique\":true,\"fields\":[\"name\"]}],\"datastore\":{\"mongo\":{\"collection\":\"country\"}}},\"schema\":{\"name\":\"country\",\"version\":{\"value\":\"1.0.0\",\"changelog\":\"blahblah\"},\"status\":{\"value\":\"active\"},\"access\":{\"insert\":[\"anyone\"],\"update\":[\"anyone\"],\"find\":[\"anyone\"],\"delete\":[\"anyone\"]},\"fields\":{\"iso3code\":{\"type\":\"string\"},\"iso2code\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"object_type\":{\"type\":\"string\",\"access\":{\"find\":[\"anyone\"],\"update\":[\"noone\"]},\"constraints\":{\"required\":true,\"minLength\":1}}}}}";
        String resultGetMetadata = cutMetadataResource.getMetadata("country","1.0.0");
        assertEquals(expectedGetMetadata,resultGetMetadata);

        //  TODO to solve these problems following calls
        //System.out.println("x "+cutMetadataResource.createSchema("country","1.0.0",readFile("expectedCreateSchema.json")));
        //System.out.println("y "+cutMetadataResource.updateEntityInfo("country",readFile("expectedCreateSchema.json")));

        String expectedUpdateSchemaStatus = "{\"entityInfo\":{\"name\":\"country\",\"indexes\":[{\"name\":null,\"unique\":true,\"fields\":[\"name\"]}],\"datastore\":{\"mongo\":{\"collection\":\"country\"}}},\"schema\":{\"name\":\"country\",\"version\":{\"value\":\"1.0.0\",\"changelog\":\"blahblah\"},\"status\":{\"value\":\"deprecated\"},\"access\":{\"insert\":[\"anyone\"],\"update\":[\"anyone\"],\"find\":[\"anyone\"],\"delete\":[\"anyone\"]},\"fields\":{\"iso3code\":{\"type\":\"string\"},\"iso2code\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"object_type\":{\"type\":\"string\",\"access\":{\"find\":[\"anyone\"],\"update\":[\"noone\"]},\"constraints\":{\"required\":true,\"minLength\":1}}}}}";
        String resultUpdateSchemaStatus = cutMetadataResource.updateSchemaStatus("country","1.0.0","deprecated","No comment");
        assertEquals(expectedUpdateSchemaStatus,resultUpdateSchemaStatus);
    }
}
