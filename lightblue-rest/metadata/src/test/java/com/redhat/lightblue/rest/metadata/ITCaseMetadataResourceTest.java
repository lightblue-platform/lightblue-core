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

import static com.redhat.lightblue.util.test.FileUtil.readFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.metadata.MetadataConfiguration;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.mongo.config.MongoConfiguration;
import com.redhat.lightblue.util.JsonUtils;

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
            config.setDatabase(DB_NAME);
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
                .addAsResource(new File("src/test/resources/datasources.json"), "datasources.json")
                .addAsResource(EmptyAsset.INSTANCE, "resources/test.properties");

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");
        return archive;

    }

    private String esc(String s) {
        return s.replace("'", "\"");
    }

    @Inject
    private MetadataResource cutMetadataResource;

    @Test
    public void testFirstIntegrationTest() throws IOException, URISyntaxException, JSONException {
        Assert.assertNotNull("MetadataResource was not injected by the container", cutMetadataResource);

        MetadataRestConfiguration.setDatasources(new DataSourcesConfiguration(JsonUtils.json(readFile("datasources.json"))));
        MetadataRestConfiguration.setMetadataManager(new MetadataManager(MetadataRestConfiguration.getDatasources()));

        String expectedCreated = readFile("expectedCreated.json");
        String resultCreated = cutMetadataResource.createMetadata("country", "1.0.0", readFile("resultCreated.json"));
        JSONAssert.assertEquals(expectedCreated, resultCreated, false);

        String expectedDepGraph = readFile("expectedDepGraph.json").replace("Notsupportedyet", " Not supported yet");
        String resultDepGraph = cutMetadataResource.getDepGraph(); //TODO Not implemented yet
        JSONAssert.assertEquals(expectedDepGraph, resultDepGraph, false);

        String expectedDepGraph1 = readFile("expectedDepGraph1.json").replace("Notsupportedyet", " Not supported yet");
        String resultDepGraph1 = cutMetadataResource.getDepGraph("country"); //TODO Not implemented yet
        JSONAssert.assertEquals(expectedDepGraph1, resultDepGraph1, false);

        String expectedDepGraph2 = readFile("expectedDepGraph2.json").replace("Notsupportedyet", " Not supported yet");
        String resultDepGraph2 = cutMetadataResource.getDepGraph("country", "1.0.0"); //TODO Not implemented yet
        JSONAssert.assertEquals(expectedDepGraph2, resultDepGraph2, false);

        String expectedEntityNames = "{\"entities\":[\"country\"]}";
        String resultEntityNames = cutMetadataResource.getEntityNames();
        JSONAssert.assertEquals(expectedEntityNames, resultEntityNames, false);

        // no default version
        String expectedEntityRoles = esc("{'status':'ERROR','modifiedCount':0,'matchCount':0,'dataErrors':[{'data':{'name':'country'},'errors':[{'object_type':'error','context':'GetEntityRolesCommand/getEntityMetadata(country:null)','errorCode':'ERR_NO_METADATA','msg':'Could not get metadata for given input. Error message: version'}]}]}");
        String expectedEntityRoles1 = esc("{'status':'ERROR','modifiedCount':0,'matchCount':0,'dataErrors':[{'data':{'name':'country'},'errors':[{'object_type':'error','context':'GetEntityRolesCommand/country/getEntityMetadata(country:null)','errorCode':'ERR_NO_METADATA','msg':'Could not get metadata for given input. Error message: version'}]}]}");
        String resultEntityRoles = cutMetadataResource.getEntityRoles();
        String resultEntityRoles1 = cutMetadataResource.getEntityRoles("country");
        JSONAssert.assertEquals(expectedEntityRoles, resultEntityRoles, false);
        JSONAssert.assertEquals(expectedEntityRoles1, resultEntityRoles1, false);

        String expectedEntityRoles2 = readFile("expectedEntityRoles2.json");
        String resultEntityRoles2 = cutMetadataResource.getEntityRoles("country", "1.0.0");
        JSONAssert.assertEquals(expectedEntityRoles2, resultEntityRoles2, false);

        String expectedEntityVersions = esc("[{'version':'1.0.0','changelog':'blahblah'}]");
        String resultEntityVersions = cutMetadataResource.getEntityVersions("country");
        JSONAssert.assertEquals(expectedEntityVersions, resultEntityVersions, false);

        String expectedGetMetadata = esc("{'entityInfo':{'name':'country','indexes':[{'name':null,'unique':true,'fields':[{'name':'$asc'}]}],'datastore':{'backend':'mongo','datasource':'mongo','collection':'country'}},'schema':{'name':'country','version':{'value':'1.0.0','changelog':'blahblah'},'status':{'value':'active'},'access':{'insert':['anyone'],'update':['anyone'],'find':['anyone'],'delete':['anyone']},'fields':{'iso3code':{'type':'string'},'name':{'type':'string'},'iso2code':{'type':'string'},'object_type':{'type':'string','access':{'find':['anyone'],'update':['noone']},'constraints':{'required':true,'minLength':1}}}}}");
        String resultGetMetadata = cutMetadataResource.getMetadata("country", "1.0.0");
        JSONAssert.assertEquals(expectedGetMetadata, resultGetMetadata, false);

        String expectedCreateSchema = readFile("expectedCreateSchema.json");
        String resultCreateSchema = cutMetadataResource.createSchema("country", "1.1.0", readFile("expectedCreateSchemaInput.json"));
        JSONAssert.assertEquals(expectedCreateSchema, resultCreateSchema, false);

        String expectedUpdateEntityInfo = readFile("expectedUpdateEntityInfo.json");
        String resultUpdateEntityInfo = cutMetadataResource.updateEntityInfo("country", readFile("expectedUpdateEntityInfoInput.json"));
        JSONAssert.assertEquals(expectedUpdateEntityInfo, resultUpdateEntityInfo, false);

        String x = cutMetadataResource.setDefaultVersion("country", "1.0.0");
        String expected = esc("{'entityInfo':{'name':'country','defaultVersion':'1.0.0','indexes':[{'name':null,'unique':true,'fields':[{'name':'$asc'}]}],'datastore':{'backend':'mongo','datasource':'mongo','collection':'country'}},'schema':{'name':'country','version':{'value':'1.0.0','changelog':'blahblah'},'status':{'value':'active'},'access':{'insert':['anyone'],'update':['anyone'],'find':['anyone'],'delete':['anyone']},'fields':{'iso3code':{'type':'string'},'name':{'type':'string'},'iso2code':{'type':'string'},'object_type':{'type':'string','access':{'find':['anyone'],'update':['noone']},'constraints':{'required':true,'minLength':1}}}}}");
        JSONAssert.assertEquals(expected, x, false);

        x = cutMetadataResource.clearDefaultVersion("country");
        System.out.println(x);
        expected = esc("{'name':'country','indexes':[{'name':null,'unique':true,'fields':[{'name':'$asc'}]}],'datastore':{'backend':'mongo','datasource':'mongo','collection':'country'}}");
        JSONAssert.assertEquals(expected, x, false);

        String expectedUpdateSchemaStatus = esc("{'entityInfo':{'name':'country','indexes':[{'name':null,'unique':true,'fields':[{'name':'$asc'}]}],'datastore':{'backend':'mongo','datasource':'mongo','collection':'country'}},'schema':{'name':'country','version':{'value':'1.0.0','changelog':'blahblah'},'status':{'value':'deprecated'},'access':{'insert':['anyone'],'update':['anyone'],'find':['anyone'],'delete':['anyone']},'fields':{'iso3code':{'type':'string'},'name':{'type':'string'},'iso2code':{'type':'string'},'object_type':{'type':'string','access':{'find':['anyone'],'update':['noone']},'constraints':{'required':true,'minLength':1}}}}}");
        String resultUpdateSchemaStatus = cutMetadataResource.updateSchemaStatus("country", "1.0.0", "deprecated", "No comment");
        JSONAssert.assertEquals(expectedUpdateSchemaStatus, resultUpdateSchemaStatus, false);

    }
}
