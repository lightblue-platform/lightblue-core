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

public class ProjectionJsonSchemaTest extends AbstractJsonSchemaTest {

    @Test
    public void invalid() throws IOException, ProcessingException {
        runInvalidJsonTest("json-schema/projection/match.json", "projection/schema-test-projection-invalid.json");
    }

    @Test
    public void validSingleField() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/projection/field.json","projection/schema-test-projection-single-field.json");
    }

    @Test
    public void validSingleMatchSingleProject() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/projection/match.json","projection/schema-test-projection-single-match-single-project.json");
    }

    @Test
    public void validSingleMatchManyProject() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/projection/match.json","projection/schema-test-projection-single-match-many-project.json");
    }

    @Test
    public void validSingleRangeSingleProject() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/projection/range.json","projection/schema-test-projection-single-range-single-project.json");
    }

    @Test
    public void validSingleRangeManyProject() throws IOException, ProcessingException {
        runValidJsonTest("json-schema/projection/range.json","projection/schema-test-projection-single-range-many-project.json");
    }
}
