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
package com.redhat.lightblue.crud.rdbms;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RDBMSCRUDControllerTest {

    public static DataSource dsMock = null;
    public static Connection cMock = null;
    public static String statement = null;
    public static PreparedStatement psMock = null;
    RDBMSCRUDController cut = null; //class under test

    @Before
    public void setUp() throws Exception {
        cut = new RDBMSCRUDController(null);
        dsMock = mock(DataSource.class);
        cMock = mock(Connection.class);
        psMock = mock(PreparedStatement.class);
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

    @Ignore
    @Test
    public void testFind() throws Exception {
        /*
         String json = new Scanner(this.getClass().getClassLoader().getResourceAsStream("metadata.json")).useDelimiter("\\Z").next();
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
         */
    }
}
