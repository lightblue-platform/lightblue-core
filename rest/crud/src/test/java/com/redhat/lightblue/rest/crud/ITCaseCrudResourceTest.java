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
import com.redhat.lightblue.config.crud.CrudConfiguration;
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
import java.net.URISyntaxException;

import static com.redhat.lightblue.util.test.FileUtil.readFile;
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
                .addAsResource(new File("src/test/resources/lightblue-metadata.json"), MetadataConfiguration.FILENAME)
                .addAsResource(new File("src/test/resources/lightblue-crud.json"), CrudConfiguration.FILENAME)
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
    public void testFirstIntegrationTest() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, URISyntaxException {
        assertNotNull("CrudResource was not injected by the container", cutCrudResource);


        String expectedCreated = readFile("expectedCreated.json");
        String metadata = readFile("metadata.json");
        EntityMetadata em = MetadataManager.getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));
        MetadataManager.getMetadata().createNewMetadata(em);
        EntityMetadata em2 = MetadataManager.getMetadata().getEntityMetadata("country", "1.0.0");
        String resultCreated = MetadataManager.getJSONParser().convert(em2).toString();
        assertEquals(expectedCreated,resultCreated);


        String expectedInserted = readFile("expectedInserted.json");
        String resultInserted = cutCrudResource.insert("country","1.0.0",readFile("resultInserted.json"));
        assertEquals(expectedInserted,resultInserted);


        String expectedUpdated = readFile("expectedUpdated.json");
        String resultUpdated = cutCrudResource.update("country","1.0.0",readFile("resultUpdated.json"));
        assertEquals(expectedUpdated,resultUpdated);


        String expectedFound = readFile("expectedFound.json");
        String resultFound = cutCrudResource.find("country","1.0.0", readFile("resultFound.json"));
        assertEquals(expectedFound,resultFound);

        String resultSimpleFound = cutCrudResource.simpleFind(    //?Q&P&S&from&to
                "country",
                "1.0.0",
                "{\"field\": \"iso2code\", \"op\": \"=\",\"rvalue\": \"CA\"}",
                "[{\"field\": \"name\",\"include\": true}, {\"field\": \"iso3code\",\"include\": true}]",
                null,
                0,
                -1);
        assertEquals(expectedFound,resultSimpleFound);


        String expectedDeleted = readFile("expectedDeleted.json");
        String resultDeleted = cutCrudResource.delete("country","1.0.0",readFile("resultDeleted.json"));
        assertEquals(expectedDeleted,resultDeleted);


        String expectedFound2 = readFile("expectedFound2.json");
        String resultFound2 = cutCrudResource.find("country","1.0.0", readFile("resultFound2.json"));
        assertEquals(expectedFound2,resultFound2);
    }
}
