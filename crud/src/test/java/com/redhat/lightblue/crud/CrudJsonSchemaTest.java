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
package com.redhat.lightblue.crud;

import java.io.IOException;

import org.junit.Test;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class CrudJsonSchemaTest extends AbstractJsonSchemaTest {

    @Test
    public void validateSchemaResponse() throws ProcessingException, IOException {
        validateSchema("json-schema/response.json");
    }

    @Test
    public void validateSchemaCrud() throws ProcessingException, IOException {
        validateSchema("json-schema/crud.json");
    }

    @Test
    public void validResponseSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/response.json", "crud/response/schema-test-response-simple.json");
    }

    @Test
    public void validDeleteSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/delete/schema-test-delete-simple.json");
    }

    @Test
    public void validInsertSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/insert/schema-test-insert-simple.json");
    }

    @Test
    public void validInsertMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/insert/schema-test-insert-many.json");
    }

    @Test
    public void validUpdateSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/update/schema-test-update-simple.json");
    }

    @Test
    public void validUpdateMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/update/schema-test-update-many.json");
    }

    @Test
    public void validSaveSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/save/schema-test-save-simple.json");
    }

    @Test
    public void validSaveMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/save/schema-test-save-many.json");
    }

    @Test
    public void validFindSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/crud.json", "crud/find/schema-test-find-simple.json");
    }

}
