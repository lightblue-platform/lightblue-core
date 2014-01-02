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
package com.redhat.lightblue.query.schema;

import java.io.IOException;

import org.junit.Test;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class QueryJsonSchemaTest extends AbstractJsonSchemaTest {

    @Test
    public void invalid() throws IOException, ProcessingException {
        runInvalidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-invalid.json");
    }

    @Test
    public void validAllArrayNested() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-all-array-nested.json");
    }

    @Test
    public void validAllArrayNot() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-all-array-not.json");
    }

    @Test
    public void validAllArray() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-all-array.json");
    }

    @Test
    public void validAnyArrayNested() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-any-array-nested.json");
    }

    @Test
    public void validAnyArray() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-any-array.json");
    }

    @Test
    public void validAnyArrayCompleteSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-any-complete-simple.json");
    }

    @Test
    public void validNotAnyArrayNested() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-not-any-array-nested.json");
    }

    @Test
    public void validNotAnyArrayNot() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-not-any-array-not.json");
    }

    @Test
    public void validNotAnyArray() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-not-any-array.json");
    }

    @Test
    public void validNot() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-not.json");
    }

    @Test
    public void validAndArrayNested() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-and-array-nested.json");
    }

    @Test
    public void validAndArrayNot() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-and-array-not.json");
    }

    @Test
    public void validAndArray() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-and-array.json");
    }

    @Test
    public void validOrArrayNested() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-or-array-nested.json");
    }

    @Test
    public void validOrArray() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-or-array.json");
    }

    @Test
    public void validOrArrayCompleteSimple() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/conditional.json", "query/schema-test-query-or-complete-simple.json");
    }

    @Test
    public void validArrayMatch() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/array-matches.json", "query/schema-test-query-array-match.json");
    }

    @Test
    public void validFieldBinaryField() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/field-binary-field.json", "query/schema-test-query-field-binary-field.json");
    }

    @Test
    public void validFieldBinaryValue() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/query/field-binary-value.json", "query/schema-test-query-field-binary-value.json");
    }
}
