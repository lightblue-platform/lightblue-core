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
package com.redhat.lightblue.util.test;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;

public abstract class AbstractJsonSchemaTest extends AbstractJsonNodeTest {

    /**
     * Load a schema from given resourceName.
     *
     * @param resourceName
     * @return the schema
     * @throws ProcessingException
     * @throws IOException
     */
    public final JsonSchema loadSchema(String resourceName) throws ProcessingException, IOException {
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        SyntaxValidator validator = factory.getSyntaxValidator();

        JsonNode node = loadJsonNode(resourceName);

        ProcessingReport report = validator.validateSchema(node);
        Assert.assertTrue("Schema is not valid!", report.isSuccess());

        JsonSchema schema = factory.getJsonSchema("resource:/" + resourceName);
        Assert.assertNotNull(schema);

        return schema;
    }

    /**
     * Validate the given resource is a valid json schema.
     *
     * @param schemaResourceName
     * @throws ProcessingException
     * @throws IOException
     */
    public void validateSchema(String schemaResourceName) throws ProcessingException, IOException {
        loadSchema(schemaResourceName);
    }

    /**
     * Verify the given document does not validate against the given schema.
     *
     * @param schemaResourceName
     * @param documentResourceName
     * @throws IOException
     * @throws ProcessingException
     */
    public void runInvalidJsonTest(String schemaResourceName, String documentResourceName) throws IOException, ProcessingException {
        JsonSchema schema = loadSchema(schemaResourceName);

        JsonNode instance = loadJsonNode(documentResourceName);

        ProcessingReport report = schema.validate(instance);
        Assert.assertFalse("Expected validation to fail!", report.isSuccess());

    }

    /**
     * Verify the given document validates against the given schema.
     *
     * @param schemaResourceName
     * @param documentResourceName
     * @throws IOException
     * @throws ProcessingException
     */
    public void runValidJsonTest(String schemaResourceName, String documentResourceName) throws IOException, ProcessingException {
        JsonSchema schema = loadSchema(schemaResourceName);

        JsonNode instance = loadJsonNode(documentResourceName);

        ProcessingReport report = schema.validate(instance);
        Iterator<ProcessingMessage> i = report.iterator();
        StringBuilder buff = new StringBuilder("Expected validation to succeed!\nResource: ").append(documentResourceName).append("\nMessages:\n");
        while (i != null && i.hasNext()) {
            ProcessingMessage pm = i.next();

            // attempting to pretty print the json
            ObjectMapper mapper = new ObjectMapper();
            String prettyPrintJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pm.asJson());

            buff.append(prettyPrintJson).append("\n\n");
        }
        Assert.assertTrue(buff.toString(), report.isSuccess());
    }

}
