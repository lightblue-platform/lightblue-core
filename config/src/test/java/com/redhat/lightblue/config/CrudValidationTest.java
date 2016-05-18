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
package com.redhat.lightblue.config;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.DeleteRequest;

import com.redhat.lightblue.util.test.FileUtil;

import static com.redhat.lightblue.util.JsonUtils.json;

public class CrudValidationTest {

    @Test
    public void testValidInputWithNonValidating() throws Exception {

        LightblueFactory lbf = new LightblueFactory(new DataSourcesConfiguration());

        // Emulate configuration
        lbf.getJsonTranslator().setValidation(Request.class, false);

        String jsonString = FileUtil.readFile("valid-deletion-req.json");
        JsonNode node = json(jsonString);
        DeleteRequest req = lbf.getJsonTranslator().parse(DeleteRequest.class, node);
        Assert.assertNotNull(req);
    }

    @Test
    public void testInvalidInputWithNonValidating() throws Exception {

        LightblueFactory lbf = new LightblueFactory(new DataSourcesConfiguration());

        // Emulate configuration
        lbf.getJsonTranslator().setValidation(Request.class, false);

        String jsonString = FileUtil.readFile("invalid-deletion-req.json");
        JsonNode node = json(jsonString);
        DeleteRequest req = lbf.getJsonTranslator().parse(DeleteRequest.class, node);
        Assert.assertNotNull(req);
    }

    @Test
    public void testValidInputWithValidating() throws Exception {

        LightblueFactory lbf = new LightblueFactory(new DataSourcesConfiguration());

        // Emulate configuration
        lbf.getJsonTranslator().setValidation(Request.class, true);

        String jsonString = FileUtil.readFile("valid-deletion-req.json");
        JsonNode node = json(jsonString);
        DeleteRequest req = lbf.getJsonTranslator().parse(DeleteRequest.class, node);
        Assert.assertNotNull(req);
    }

    @Test
    public void testInvalidInputWithValidating() throws Exception {

        LightblueFactory lbf = new LightblueFactory(new DataSourcesConfiguration());

        // Emulate configuration
        lbf.getJsonTranslator().setValidation(Request.class, true);

        String jsonString = FileUtil.readFile("invalid-deletion-req.json");
        JsonNode node = json(jsonString);
        try {
            lbf.getJsonTranslator().parse(DeleteRequest.class, node);
            Assert.fail();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
