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

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import java.io.IOException;

import org.junit.Test;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class CrudJsonSchemaTest extends AbstractJsonSchemaTest {

    @Test
    public void validateSchemaResponse() throws ProcessingException, IOException {
        validateSchema("json-schema/response.json");
    }

    @Test
    public void validateSchemaUpdateReq() throws ProcessingException, IOException {
        validateSchema("json-schema/updateRequest.json");
    }

    @Test
    public void validateSchemaInsertReq() throws ProcessingException, IOException {
        validateSchema("json-schema/insertRequest.json");
    }

    @Test
    public void validateSchemaDeleteReq() throws ProcessingException, IOException {
        validateSchema("json-schema/deleteRequest.json");
    }

    @Test
    public void validateSchemaSaveReq() throws ProcessingException, IOException {
        validateSchema("json-schema/saveRequest.json");
    }

    @Test
    public void validateSchemaFindReq() throws ProcessingException, IOException {
        validateSchema("json-schema/findRequest.json");
    }

    @Test
    public void validResponseSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/response.json", "crud/response/schema-test-response-simple.json");
    }

    @Test
    public void validDeleteSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/deleteRequest.json", "crud/delete/schema-test-delete-simple.json");
    }

    @Test
    public void validInsertSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/insertRequest.json", "crud/insert/schema-test-insert-simple.json");
    }

    @Test
    public void validInsertMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/insertRequest.json", "crud/insert/schema-test-insert-many.json");
    }

    @Test
    public void validUpdateSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/updateRequest.json", "crud/update/schema-test-update-simple.json");
    }

    @Test
    public void validUpdateMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/updateRequest.json", "crud/update/schema-test-update-many.json");
    }

    @Test
    public void validSaveSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/saveRequest.json", "crud/save/schema-test-save-simple.json");
    }

    @Test
    public void validSaveMany() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/saveRequest.json", "crud/save/schema-test-save-many.json");
    }

    @Test
    public void validFindSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/findRequest.json", "crud/find/schema-test-find-simple.json");
    }

    @Test
    public void validFindNoq() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/findRequest.json", "crud/find/schema-test-find-noq.json");
    }

}
