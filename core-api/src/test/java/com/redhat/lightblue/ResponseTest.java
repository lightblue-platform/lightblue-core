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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import static org.junit.Assert.*;

public class ResponseTest {
    public static class ResponseBuilder {

        private OperationStatus status;
        private long modifiedCount;
        private long matchCount;
        private String taskHandle;
        private SessionInfo session;
        private JsonNode entityData;
        private String hostname;
        private List<DataError> dataErrors = new ArrayList<>();
        private List<Error> errors = new ArrayList<>();

        private final JsonNodeFactory jsonNodeFactory;

        public ResponseBuilder(JsonNodeFactory jsonNodeFactory) {
            this.jsonNodeFactory = jsonNodeFactory;
        }

        public ResponseBuilder(Response response) {
            status = response.getStatus();
            modifiedCount = response.getModifiedCount();
            matchCount = response.getMatchCount();
            hostname = response.getHostname();
            taskHandle = response.getTaskHandle();
            session = response.getSessionInfo();
            entityData = response.getEntityData();
            dataErrors = response.getDataErrors();
            errors = response.getErrors();
            jsonNodeFactory = response.jsonNodeFactory;
        }

        public ResponseBuilder withHostname(JsonNode node) {
            if (node != null) {
                hostname = node.asText();
            }
            return this;
        }

        public ResponseBuilder withStatus(JsonNode node) {
            if (node != null) {
                try {
                    status = OperationStatus.valueOf(node.asText().toUpperCase());
                } catch (IllegalArgumentException e) {
                    status = OperationStatus.ERROR;
                }
            }
            return this;
        }

        public ResponseBuilder withModifiedCount(JsonNode node) {
            if (node != null) {
                modifiedCount = node.asLong();
            }
            return this;
        }

        public ResponseBuilder withMatchCount(JsonNode node) {
            if (node != null) {
                matchCount = node.asLong();
            }
            return this;
        }

        public ResponseBuilder withTaskHandle(JsonNode node) {
            if (node != null) {
                taskHandle = node.asText();
            }
            return this;
        }

        public ResponseBuilder withSession(JsonNode node) {
            //TODO
            return this;
        }

        public ResponseBuilder withEntityData(JsonNode node) {
            if (node != null) {
                entityData = node;
            }
            return this;
        }

        public ResponseBuilder withDataErrors(JsonNode node) {
            if (node instanceof ArrayNode) {
                for (Iterator<JsonNode> itr = ((ArrayNode) node).elements();
                        itr.hasNext();) {
                    dataErrors.add(DataError.fromJson((ObjectNode) itr.next()));
                }
            }
            return this;
        }

        public ResponseBuilder withErrors(JsonNode node) {
            if (node instanceof ArrayNode) {
                for (Iterator<JsonNode> itr = ((ArrayNode) node).elements();
                        itr.hasNext();) {
                    errors.add(Error.fromJson(itr.next()));
                }
            }
            return this;
        }

        public Response buildResponse() {
            Response response = new Response(jsonNodeFactory);

            response.setStatus(status);
            response.setModifiedCount(modifiedCount);
            response.setMatchCount(matchCount);
            response.setTaskHandle(taskHandle);
            response.setSessionInfo(session);
            response.setEntityData(entityData);
            response.getDataErrors().addAll(dataErrors);
            response.getErrors().addAll(errors);

            return response;
        }
    }

    Response response;

    ResponseBuilder builder;
    JsonNode node;

    @Before
    public void setUp() throws Exception {
        JsonNodeFactory jnf = JsonNodeFactory.withExactBigDecimals(true);
        response = new Response(jnf);
        builder = new ResponseBuilder(jnf);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWithStatus() {
        node = JsonNodeFactory.withExactBigDecimals(true).textNode(OperationStatus.COMPLETE.toString());
        builder.withStatus(node);
        assertTrue(OperationStatus.COMPLETE.equals(builder.buildResponse().getStatus()));
    }

    @Test
    public void testWithStatusNull() {
        builder.withStatus(null);
        assertFalse(OperationStatus.COMPLETE.equals(builder.buildResponse().getStatus()));
    }

    @Test
    public void testWithStatusIllegalArgument() {
        node = JsonNodeFactory.withExactBigDecimals(true).textNode("badStatus");
        builder.withStatus(node);
        assertTrue(OperationStatus.ERROR.equals(builder.buildResponse().getStatus()));
    }

    @Test
    public void testWithModifiedCount() {
        node = JsonNodeFactory.withExactBigDecimals(true).numberNode(Long.MAX_VALUE);
        builder.withModifiedCount(node);
        assertTrue(new Long(Long.MAX_VALUE).equals(builder.buildResponse().getModifiedCount()));
    }

    @Test
    public void testWithModifiedCountNull() {
        builder.withModifiedCount(null);
        assertFalse(new Long(Long.MAX_VALUE).equals(builder.buildResponse().getModifiedCount()));
    }

    @Test
    public void testWithMatchCount() {
        node = JsonNodeFactory.withExactBigDecimals(true).numberNode(Long.MAX_VALUE);
        builder.withMatchCount(node);
        assertTrue(new Long(Long.MAX_VALUE).equals(builder.buildResponse().getMatchCount()));
    }

    @Test
    public void testWithMatchCountNull() {
        builder.withMatchCount(null);
        assertFalse(new Long(Long.MAX_VALUE).equals(builder.buildResponse().getMatchCount()));
    }

    @Test
    public void testWithTaskHandle() {
        String taskHandle = "taskHandle";
        node = JsonNodeFactory.withExactBigDecimals(true).textNode(taskHandle);
        builder.withTaskHandle(node);
        assertTrue(taskHandle.equals(builder.buildResponse().getTaskHandle()));
    }

    @Test
    public void testWithTaskHandleNull() {
        String taskHandle = "taskHandle";
        builder.withTaskHandle(null);
        assertFalse(taskHandle.equals(builder.buildResponse().getTaskHandle()));
    }

    @Test
    public void testWithSessionNull() {
        builder.withSession(null);
        //TODO Assert something session implemented
    }

    @Test
    public void testWithEntityDataObject() {
        node = JsonNodeFactory.withExactBigDecimals(true).objectNode();
        builder.withEntityData(node);
        // note, entityData is treated as an array always.  in this case we have to extract the first element of the array for validation
        assertTrue(node.equals(builder.buildResponse().getEntityData().get(0)));
    }

    @Test
    public void testWithEntityDataArray() {
        node = JsonNodeFactory.withExactBigDecimals(true).arrayNode();
        ArrayNode anode = (ArrayNode) node;
        anode.add(anode.objectNode());
        anode.add(anode.objectNode());
        builder.withEntityData(node);
        assertEquals(2, builder.buildResponse().getEntityData().size());
        assertTrue(node.equals(builder.buildResponse().getEntityData()));
    }

    @Test
    public void testWithEntityDataNull() {
        node = JsonNodeFactory.withExactBigDecimals(true).objectNode();
        builder.withEntityData(null);
        assertFalse(node.equals(builder.buildResponse().getEntityData()));
    }

    @Test
    public void testWithDataErrors() {
        ObjectNode objNode = JsonObject.getFactory().objectNode();
        ArrayNode arr = JsonObject.getFactory().arrayNode();

        for (DataError err : getPopulatedDataErrors(3)) {
            arr.add(err.toJson());
        }

        objNode.set("dataErrors", arr);
        builder.withDataErrors(arr);

        for (int i = 0; i < builder.buildResponse().getDataErrors().size(); i++) {
            DataError de = builder.buildResponse().getDataErrors().get(i);

            for (int j = 0; j < de.getErrors().size(); j++) {
                Error error = de.getErrors().get(j);
                assertTrue(error.getErrorCode().equals(getPopulatedErrors(3).get(j).getErrorCode()));
            }
        }

    }

    @Test
    public void testWithDataErrorsNotArray() {
        builder.withDataErrors(JsonObject.getFactory().objectNode());
        assertEquals(builder.buildResponse().getDataErrors().size(), 0);
    }

    @Test
    public void testWithErrors() {
        ObjectNode objNode = JsonObject.getFactory().objectNode();
        ArrayNode arr = JsonObject.getFactory().arrayNode();

        for (Error err : getPopulatedErrors(3)) {
            arr.add(err.toJson());
        }

        objNode.set("errors", arr);

        builder.withErrors(arr);

        for (int i = 0; i < builder.buildResponse().getErrors().size(); i++) {
            Error e = builder.buildResponse().getErrors().get(i);
            assertTrue(e.getErrorCode().equals(getPopulatedErrors(3).get(i).getErrorCode()));
        }
    }

    @Test
    public void testWithErrorsNotArray() {
        builder.withErrors(JsonObject.getFactory().objectNode());
        assertEquals(builder.buildResponse().getErrors().size(), 0);
    }

    @Test
    public void testBuildResponse() {
        response.setStatus(OperationStatus.COMPLETE);
        response.setModifiedCount(Integer.MAX_VALUE);
        response.setMatchCount(Integer.MIN_VALUE);
        response.setTaskHandle("taskHandle");
        response.setSessionInfo(null);
        response.setEntityData(null);
        response.getDataErrors().addAll(new ArrayList<DataError>());
        response.getErrors().addAll(new ArrayList<Error>());

        ResponseBuilder responseBuilder = new ResponseBuilder(response);

        assertTrue(response.getStatus().equals(responseBuilder.buildResponse().getStatus()));
    }

    @Test
    public void testToJson() {
        response.setStatus(OperationStatus.COMPLETE);
        response.setModifiedCount(Integer.MAX_VALUE);
        response.setMatchCount(Integer.MIN_VALUE);
        response.setTaskHandle("taskHandle");
        response.setSessionInfo(null);
        response.setEntityData(JsonObject.getFactory().objectNode());
        response.getDataErrors().addAll(getPopulatedDataErrors(3));
        response.getErrors().addAll(getPopulatedErrors(3));
        response.setHostname("localhost");

        ObjectNode expectedNode = JsonObject.getFactory().objectNode();
        expectedNode.put("status", OperationStatus.COMPLETE.name().toLowerCase());
        expectedNode.put("modifiedCount", Integer.MAX_VALUE);
        expectedNode.put("matchCount", Integer.MIN_VALUE);
        expectedNode.put("taskHandle", "taskHandle");
        expectedNode.set("session", JsonObject.getFactory().objectNode());
        expectedNode.set("entityData", JsonObject.getFactory().objectNode());
        expectedNode.put("hostname", "localhost");
        ArrayNode arr = JsonObject.getFactory().arrayNode();
        expectedNode.set("dataErrors", arr);
        for (DataError err : getPopulatedDataErrors(3)) {
            arr.add(err.toJson());
        }

        ArrayNode arr2 = JsonObject.getFactory().arrayNode();
        expectedNode.set("errors", arr2);
        for (Error err : getPopulatedErrors(3)) {
            arr2.add(err.toJson());
        }

        assertFalse(response.toJson().equals(expectedNode));
    }

    @Test
    public void testBuildJsonNull() throws UnknownHostException {
        ObjectNode expectedNode = JsonObject.getFactory().objectNode();
        expectedNode.put("modifiedCount", 0L);
        expectedNode.put("matchCount", 0L);
        expectedNode.put("hostname", InetAddress.getLocalHost().getHostName());

        assertTrue(response.toJson().equals(expectedNode));
    }

    private List<DataError> getPopulatedDataErrors(int numberOfErrors) {
        List<DataError> dataErrors = new ArrayList<>();

        DataError dataError = new DataError(node, getPopulatedErrors(numberOfErrors));
        dataErrors.add(dataError);

        return dataErrors;
    }

    private List<Error> getPopulatedErrors(int numberOfErrors) {
        String errorText = "error";

        List<Error> errors = new ArrayList<>();

        for (int i = 0; i < numberOfErrors; i++) {
            errors.add(Error.get(errorText + i));
        }

        return errors;
    }

}
