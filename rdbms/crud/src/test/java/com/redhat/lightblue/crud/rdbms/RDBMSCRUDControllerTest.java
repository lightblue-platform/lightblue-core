package com.redhat.lightblue.crud.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.common.rdbms.RDBMSContext;
import com.redhat.lightblue.common.rdbms.RDBMSUtils;
import com.redhat.lightblue.config.common.DataSourcesConfiguration;
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
import com.redhat.lightblue.mongo.config.MongoDBResolver;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RDBMSCRUDControllerTest extends TestCase {

    public static DataSource dsMock = null;
    public static Connection cMock = null;
    public static String statement = null;
    public static PreparedStatement psMock = null;
    public static RDBMSUtils rDBMSUtils = null;
    RDBMSCRUDController cut = null; //class under test

    @Before
    protected void setUp() throws Exception {
        super.setUp();
        cut = new RDBMSCRUDController(new MongoDBResolver( new DataSourcesConfiguration()));
        dsMock = mock(DataSource.class);
        cMock = mock(Connection.class);
        psMock = mock(PreparedStatement.class);
        rDBMSUtils = new RDBMSUtils(){
            @Override
            public DataSource getDataSource(RDBMSContext rDBMSContext) {
                return dsMock;
            }
        };
        when(dsMock.getConnection()).thenReturn(cMock);
        when(cMock.prepareStatement(statement)).thenReturn(psMock);

    }

    @Test
    public void testInsert() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testUpdate() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    @Ignore
    public void testFind() throws Exception {
        String json = new Scanner(new File("metadata.json")).useDelimiter("\\Z").next();
        JsonNode node = JsonUtils.json(json);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, JsonNodeFactory.withExactBigDecimals(true));
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);

        int id = 10;
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        final Map<String, EntityMetadata> map = new HashMap<>();
        CRUDOperationContext ctx = new CRUDOperationContext(Operation.FIND, "test", factory, JsonNodeFactory.withExactBigDecimals(true), new HashSet<String>(), null){
            @Override
            public EntityMetadata getEntityMetadata(String entityName) {
                return map.get(entityName);
            }
        };
        map.put(md.getName(), md);
        cut.find(ctx,
                QueryExpression.fromJson(JsonUtils.json(("{'field':'_id','op':'=','rvalue':'" + id + "'}").replace('\'', '\"'))),
                Projection.fromJson(JsonUtils.json("{'field':'*','recursive':1}".replace('\'', '\"'))),
                null, null, null);
        JsonDoc readDoc = ctx.getDocuments().get(0);

    }
}