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

public class HookJsonSchemaTest extends AbstractJsonSchemaTest {

    @Test
    public void invalidConstraints() throws IOException, ProcessingException {
        runInvalidJsonTest("json-schema/metadata/hook.json", "metadata/schema-test-hook-invalid.json");
    }

    @Test
    public void validEsb() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/metadata/hook.json", "metadata/schema-test-hook-esb.json");
    }

    @Test
    public void validFind() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/metadata/hook.json", "metadata/schema-test-hook-find.json");
    }

    @Test
    public void validInsert() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/metadata/hook.json", "metadata/schema-test-hook-insert.json");
    }

    @Test
    public void validUpdate() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/metadata/hook.json", "metadata/schema-test-hook-update.json");
    }
}
