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
package com.redhat.lightblue.mediator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.*;
import com.redhat.lightblue.crud.*;
import com.redhat.lightblue.crud.interceptors.*;
import com.redhat.lightblue.crud.valuegenerators.*;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;

public abstract class AbstractMediatorTest extends AbstractJsonSchemaTest {

    protected Mediator mediator;
    protected final TestMetadata mdManager = new TestMetadata();
    protected final MockCrudController mockCrudController = new MockCrudController();

    public static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    public static final class TestMetadata extends DatabaseMetadata {
        EntityMetadata md;

        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return md;
        }
    }

    public static class MockValueGeneratorSupport implements ValueGeneratorSupport {
        public static int v = 0;

        @Override
        public ValueGenerator.ValueGeneratorType[] getSupportedGeneratorTypes() {
            return new ValueGenerator.ValueGeneratorType[]{ValueGenerator.ValueGeneratorType.IntSequence};
        }

        @Override
        public Object generateValue(EntityMetadata md, ValueGenerator generator) {
            return new Integer(v++);
        }
    }

    public static final class RestClientIdentification extends ClientIdentification {

        private final Set<String> clientRoles;

        public RestClientIdentification(List<String> roles) {
            clientRoles = new HashSet<>();
            clientRoles.addAll(roles);
        }

        @Override
        public String getPrincipal() {
            return "";
        }

        @Override
        public boolean isUserInRole(String role) {
            return clientRoles.contains(role);
        }

        @Override
        public JsonNode toJson() {
            return null;
        }
    }

    public EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    protected Mediator newMediator(Metadata md, Factory f) {
        return new Mediator(md, f);
    }

    @Before
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        factory.setBulkParallelExecutions(10);
        new UIDInterceptor().register(factory.getInterceptors());
        new GeneratedFieldInterceptor().register(factory.getInterceptors());
        factory.addCRUDController("mongo", mockCrudController);
        mdManager.md = getMd("./testMetadata.json");
        mediator = newMediator(mdManager, factory);
    }
}
