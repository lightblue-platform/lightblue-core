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

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.redhat.lightblue.util.JsonUtils;

public abstract class AbstractJsonSchemaTest extends AbstractJsonNodeTest {
    /**
     * Validate the given resource is a valid json schema.
     *
     * @param schemaResourceName
     * @throws ProcessingException
     * @throws IOException
     */
    public void validateSchema(String schemaResourceName) throws ProcessingException, IOException {
        JsonUtils.loadSchema(schemaResourceName);
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
        JsonSchema schema = JsonUtils.loadSchema(schemaResourceName);

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
        JsonSchema schema = JsonUtils.loadSchema(schemaResourceName);

        JsonNode instance = loadJsonNode(documentResourceName);

        // if report isn't null it's a failure and the value of report is the detail of why
        String report = JsonUtils.jsonSchemaValidation(schema, instance);
        Assert.assertTrue("Expected validation to succeed!\nResource: " + documentResourceName + "\nMessages:\n" + report, report == null);
    }
}
