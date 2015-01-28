/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.crud;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.TestDataStoreParser;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.FieldConstraintParser;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.DefaultRegistry;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Registry;

public class ConstraintValidatorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    protected ConstraintValidator createConstraintValidator(EntityMetadata metadata) {
        return createConstraintValidator(metadata, null, null);
    }

    protected ConstraintValidator createConstraintValidator(
            EntityMetadata metadata,
            Map<String, ? extends FieldConstraintChecker> fieldConstraintCheckers,
            Map<String, ? extends EntityConstraintChecker> entityConstraintCheckers) {

        Registry<String, FieldConstraintChecker> fieldCheckerRegistry =
                new DefaultRegistry<String, FieldConstraintChecker>();
        fieldCheckerRegistry.add(new DefaultFieldConstraintValidators());
        if (fieldConstraintCheckers != null) {
            for (Entry<String, ? extends FieldConstraintChecker> checker : fieldConstraintCheckers.entrySet()) {
                fieldCheckerRegistry.add(checker.getKey(), checker.getValue());
            }
        }

        Registry<String, EntityConstraintChecker> entityCheckerRegistry =
                new DefaultRegistry<String, EntityConstraintChecker>();
        entityCheckerRegistry.add(new EmptyEntityConstraintValidators());
        if (entityConstraintCheckers != null) {
            for (Entry<String, ? extends EntityConstraintChecker> checker : entityConstraintCheckers.entrySet()) {
                entityCheckerRegistry.add(checker.getKey(), checker.getValue());
            }
        }

        return new ConstraintValidator(fieldCheckerRegistry, entityCheckerRegistry, metadata);
    }

    protected EntityMetadata createEntityMetadata(
            JsonNode node,
            List<? extends EntityConstraint> entityConstraints,
            Map<String, ? extends FieldConstraintParser<JsonNode>> fieldConstraintParsers) {

        TestDataStoreParser<JsonNode> dsParser = new TestDataStoreParser<JsonNode>();

        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.registerDataStoreParser(dsParser.getDefaultName(), dsParser);
        extensions.addDefaultExtensions();
        if (fieldConstraintParsers != null) {
            for (Entry<String, ? extends FieldConstraintParser<JsonNode>> checker : fieldConstraintParsers.entrySet()) {
                extensions.registerFieldConstraintParser(checker.getKey(), checker.getValue());
            }
        }

        JSONMetadataParser jsonParser = new JSONMetadataParser(
                extensions,
                new DefaultTypes(),
                JsonNodeFactory.withExactBigDecimals(false));

        EntityMetadata entityMetadata = jsonParser.parseEntityMetadata(node);
        if ((entityConstraints != null) && !entityConstraints.isEmpty()) {
            entityMetadata.setConstraints(new ArrayList<EntityConstraint>(entityConstraints));
        }

        return entityMetadata;
    }

    /**
     * Should execute the happy path for {@link EntityConstraint}s, {@link FieldConstraintDocChecker}s,
     * and {@link FieldConstraintValueChecker}s.
     * No {@link Error}s should be generated for this test.
     */
    @Test
    public void testValidate_WithAllConstraintTypes_NoErrors() throws IOException {
        JsonNode validatorNode = JsonUtils.json(getClass().getResourceAsStream(
                "/crud/validator/schema-test-validation-simple.json"));
        EntityMetadata entityMetadata = createEntityMetadata(validatorNode, null, null);
        ConstraintValidator validator = createConstraintValidator(entityMetadata);

        validator.validateDocs(Arrays.asList(new JsonDoc(validatorNode)));

        assertFalse(validator.hasErrors());
    }

    /**
     * A {@link EntityConstraint} that does not exist is requested.
     * A {@link Error} is expected.
     */
    @Test
    public void testValidate_WithInvalidEntityConstraint_ExpectError() throws IOException {
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"validateDocs/validateDoc/Does Not Exist\",\"errorCode\":\"crud:NoConstraint\"}");

        JsonNode validatorNode = JsonUtils.json(getClass().getResourceAsStream(
                "/crud/validator/schema-test-validation-simple.json"));
        EntityMetadata entityMetadata = createEntityMetadata(validatorNode, Arrays.asList(new TestEntityConstraint("Does Not Exist")), null);
        ConstraintValidator validator = createConstraintValidator(entityMetadata);

        validator.validateDocs(Arrays.asList(new JsonDoc(validatorNode)));
    }

    /**
     * No {@link FieldConstraintChecker} exists for the {@link FieldConstraint}. This causes the registry to return
     * a null value.
     * A {@link Error} is expected.
     */
    @Test
    public void testValidate_WithNullFieldConstraint_ExpectError() throws IOException {
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"validateDocs/validateDoc/field1/testFieldConstraintChecker\",\"errorCode\":\"crud:NoConstraint\"");

        JsonNode validatorNode = JsonUtils.json(getClass().getResourceAsStream(
                "/crud/validator/schema-test-validation-testFieldConstraint.json"));

        Map<String, FieldConstraintParser<JsonNode>> fieldConstraintParsers = new HashMap<String, FieldConstraintParser<JsonNode>>();
        fieldConstraintParsers.put("testFieldConstraint", new TestFieldConstraintParser(new TestFieldConstraint("testFieldConstraintChecker")));

        EntityMetadata entityMetadata = createEntityMetadata(validatorNode, null, fieldConstraintParsers);
        ConstraintValidator validator = createConstraintValidator(entityMetadata, null, null);

        validator.validateDocs(Arrays.asList(new JsonDoc(validatorNode)));
    }

    /**
     * A valid {@link FieldConstraintChecker} is registered, however it is not of type
     * {@link FieldConstraintDocChecker} or {@link FieldConstraintValueChecker}, so it should
     * be ignored.
     * No {@link Error} is thrown for this.
     */
    @Test
    public void testValidate_WithUnsupportedFieldConstraint() throws IOException {
        String fieldConstraintCheckerName = "testFieldConstraintChecker";

        JsonNode validatorNode = JsonUtils.json(getClass().getResourceAsStream(
                "/crud/validator/schema-test-validation-testFieldConstraint.json"));

        Map<String, FieldConstraintParser<JsonNode>> fieldConstraintParsers = new HashMap<String, FieldConstraintParser<JsonNode>>();
        fieldConstraintParsers.put("testFieldConstraint", new TestFieldConstraintParser(new TestFieldConstraint(fieldConstraintCheckerName)));

        EntityMetadata entityMetadata = createEntityMetadata(validatorNode, null, fieldConstraintParsers);

        Map<String, FieldConstraintChecker> fieldConstraintCheckers = new HashMap<String, FieldConstraintChecker>();
        fieldConstraintCheckers.put(fieldConstraintCheckerName, new FieldConstraintChecker() {
        });

        ConstraintValidator validator = createConstraintValidator(entityMetadata, fieldConstraintCheckers, null);

        validator.validateDocs(Arrays.asList(new JsonDoc(validatorNode)));

        assertFalse(validator.hasErrors());
    }

    @SuppressWarnings("serial")
    protected static class TestEntityConstraint implements EntityConstraint {

        private final String type;

        public TestEntityConstraint(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

    }

    protected static class TestFieldConstraintParser implements FieldConstraintParser<JsonNode> {

        private final FieldConstraint constraint;

        public TestFieldConstraintParser(FieldConstraint constraint) {
            this.constraint = constraint;
        }

        @Override
        public FieldConstraint parse(String name, MetadataParser<JsonNode> p,
                                     JsonNode node) {
            return constraint;
        }

        @Override
        public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode,
                            FieldConstraint object) {
            throw new UnsupportedOperationException();
        }

    }

    protected static class TestFieldConstraint implements FieldConstraint {

        private final String type;

        public TestFieldConstraint(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean isValidForFieldType(Type fieldType) {
            return true;
        }

        @Override
        public String getDescription() {
            return "test";
        }

    }

}
