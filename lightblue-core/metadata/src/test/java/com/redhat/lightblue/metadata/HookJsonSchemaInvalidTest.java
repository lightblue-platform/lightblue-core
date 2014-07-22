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
package com.redhat.lightblue.metadata;

import java.io.IOException;

import org.junit.Test;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HookJsonSchemaInvalidTest extends AbstractJsonSchemaTest {
    @Parameterized.Parameters(name = "resource={0}")
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> output = new ArrayList<>();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("junit-files-hook-json-schema-test-invalid.log");
                InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(new Object[]{line});
            }
        }
        return output;
    }

    private final String resourceName;

    public HookJsonSchemaInvalidTest(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * The pom has an exec plugin that generates a file in the test classpath
     * that contains all the metadata to validate. This test then verifies each
     * metadata listed, collecting any errors and and failing at the end of all
     * validation.
     *
     * @throws IOException
     * @throws ProcessingException
     */
    @Test
    public void validateMetadata() throws IOException, ProcessingException {
        runInvalidJsonTest("json-schema/metadata/hook.json", resourceName);
    }
}
