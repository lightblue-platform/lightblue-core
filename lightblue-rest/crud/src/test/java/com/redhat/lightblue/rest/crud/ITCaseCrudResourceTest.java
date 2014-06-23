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
import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.crud.CrudConfiguration;
import com.redhat.lightblue.config.crud.CrudManager;
import com.redhat.lightblue.config.metadata.MetadataConfiguration;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.mongo.config.MongoConfiguration;
import com.redhat.lightblue.util.JsonUtils;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
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
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import static com.redhat.lightblue.util.test.FileUtil.readFile;

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
                    .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
                    .build()
            );
            try {
                mongod = mongodExe.start();
            } catch (Throwable t) {
                // try again, could be killed breakpoint in IDE
                mongod = mongodExe.start();
            }
            mongo = new Mongo(IN_MEM_CONNECTION_URL);

            MongoConfiguration config = new MongoConfiguration();
            // disable ssl for test (enabled by default)
            config.setDatabase(DB_NAME);
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
    public void setup() throws Exception {
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
                .addAsResource(new File("src/test/resources/datasources.json"), "datasources.json")
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
    public void testFirstIntegrationTest() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, URISyntaxException, JSONException {
        Assert.assertNotNull("CrudResource was not injected by the container", cutCrudResource);
        CrudRestConfiguration.setDatasources(new DataSourcesConfiguration(JsonUtils.json(readFile("datasources.json"))));
        CrudRestConfiguration.setMetadataMgr(new MetadataManager(CrudRestConfiguration.getDatasources()));
        CrudRestConfiguration.setCrudMgr(new CrudManager(CrudRestConfiguration.getDatasources(), CrudRestConfiguration.getMetadataMgr()));

        String expectedCreated = readFile("expectedCreated.json");
        String metadata = readFile("metadata.json");
        EntityMetadata em = CrudRestConfiguration.getMetadataMgr().getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));
        CrudRestConfiguration.getMetadataMgr().getMetadata().createNewMetadata(em);
        EntityMetadata em2 = CrudRestConfiguration.getMetadataMgr().getMetadata().getEntityMetadata("country", "1.0.0");
        String resultCreated = CrudRestConfiguration.getMetadataMgr().getJSONParser().convert(em2).toString();
        JSONAssert.assertEquals(expectedCreated, resultCreated, false);

        String expectedInserted = readFile("expectedInserted.json");
        String resultInserted = cutCrudResource.insert("country", "1.0.0", readFile("resultInserted.json"));
        JSONAssert.assertEquals(expectedInserted, resultInserted, false);

        String expectedUpdated = readFile("expectedUpdated.json");
        String resultUpdated = cutCrudResource.update("country", "1.0.0", readFile("resultUpdated.json"));
        JSONAssert.assertEquals(expectedUpdated, resultUpdated, false);

        String expectedFound = readFile("expectedFound.json");
        String resultFound = cutCrudResource.find("country", "1.0.0", readFile("resultFound.json"));
        JSONAssert.assertEquals(expectedFound, resultFound, false);

        String resultSimpleFound = cutCrudResource.simpleFind( //?Q&P&S&from&to
                "country",
                "1.0.0",
                "iso2code:CA,QE;iso2code:CA;iso2code:CA,EN",
                "name:1r,iso3code:1,iso2code:0r",
                "name:a,iso3code:d,iso2code:d",
                0,
                -1);
        JSONAssert.assertEquals(expectedFound, resultSimpleFound, false);

        String expectedDeleted = readFile("expectedDeleted.json");
        String resultDeleted = cutCrudResource.delete("country", "1.0.0", readFile("resultDeleted.json"));
        JSONAssert.assertEquals(expectedDeleted, resultDeleted, false);

        String expectedFound2 = readFile("expectedFound2.json");
        String resultFound2 = cutCrudResource.find("country", "1.0.0", readFile("resultFound2.json"));
        JSONAssert.assertEquals(expectedFound2, resultFound2, false);
    }
}
