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
import com.redhat.lightblue.config.CrudConfiguration;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.config.MetadataConfiguration;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.mongo.config.MongoConfiguration;
import com.redhat.lightblue.rest.RestConfiguration;
import com.redhat.lightblue.util.JsonUtils;
import static com.redhat.lightblue.util.test.FileUtil.readFile;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
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

/**
 *
 * @author lcestari
 */
@RunWith(Arquillian.class)
public class ITCaseCrudResourceRDBMSTest {

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
        File folder = new File("/tmp");
        File[] files = folder.listFiles( new FilenameFilter() {@Override public boolean accept( final File dir,final String name ) {
            return name.startsWith("test.db" );
        }});
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.out.println( "Failed to remove " + file.getAbsolutePath() );
            }
        }
        db.createCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION, null);
        BasicDBObject index = new BasicDBObject("name", 1);
        index.put("version.value", 1);
        db.getCollection(MongoMetadata.DEFAULT_METADATA_COLLECTION).ensureIndex(index, "name", true);
        
        
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            // already tried System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.as.naming.InitialContextFactory");
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");

            JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:file:/tmp/test.db;FILE_LOCK=NO;MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE", "sa", "sasasa");
            
            ic.bind("java:/mydatasource", ds);
        } catch (NamingException ex) {
            throw new IllegalStateException(ex);
        }
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
                .addAsResource(new File("src/test/resources/rdbms/lightblue-metadata.json"), MetadataConfiguration.FILENAME)
                .addAsResource(new File("src/test/resources/rdbms/lightblue-crud.json"), CrudConfiguration.FILENAME)
                .addAsResource(new File("src/test/resources/rdbms/datasources.json"), "datasources.json")
                .addAsResource(new File("src/test/resources/config.properties"),"config.properties");

        for (File file : libs) {
            archive.addAsLibrary(file);
        }
        archive.addPackages(true, "com.redhat.lightblue");
        
                      System.out.print(archive.toString());

        return archive;

    }

    @Inject
    private CrudResource cutCrudResource; //class under test

    @Test
    public void testFirstIntegrationTest() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, URISyntaxException, JSONException {
        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/mydatasource");
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE People ( PersonID int, Name varchar(255) );");
            
            // https://code.google.com/p/h2database/source/browse/trunk/h2/src/test/org/h2/samples/Function.java
            stmt.execute("CREATE ALIAS getVersion FOR \"org.h2.engine.Constants.getVersion\"");
            ResultSet rs = stmt.executeQuery("CALL getVersion()");
            if (rs.next()) System.out.println("Version: " + rs.getString(1));
            stmt.close();
            
             byte[] contents = new byte[1024];
    int bytesRead = 0;
    String strFileContents;
    while ((bytesRead = Thread.currentThread().getContextClassLoader().getResourceAsStream("/WEB-INF/classes/datasources.json").read(contents)) != -1) {
      strFileContents = new String(contents, 0, bytesRead);
      System.out.print(strFileContents);
    }
            
            Assert.assertNotNull("CrudResource was not injected by the container", cutCrudResource);
            RestConfiguration.setDatasources(new DataSourcesConfiguration(JsonUtils.json(readFile("datasources.json"))));
            RestConfiguration.setFactory(new LightblueFactory(RestConfiguration.getDatasources()));
            
            String expectedCreated = readFile("expectedCreated.json");
            String metadata = readFile("metadata.json");
            EntityMetadata em = RestConfiguration.getFactory().getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));
            RestConfiguration.getFactory().getMetadata().createNewMetadata(em);
            EntityMetadata em2 = RestConfiguration.getFactory().getMetadata().getEntityMetadata("country", "1.0.0");
            String resultCreated = RestConfiguration.getFactory().getJSONParser().convert(em2).toString();
            JSONAssert.assertEquals(expectedCreated, resultCreated, false);
            
            String expectedInserted = readFile("expectedInserted.json");
            String resultInserted = cutCrudResource.insert("country", "1.0.0", readFile("resultInserted.json"));
            JSONAssert.assertEquals(expectedInserted, resultInserted, false);
        } catch (NamingException ex) {
            throw new IllegalStateException(ex);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
