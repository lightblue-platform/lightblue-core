package com.redhat.lightblue.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.config.exception.ResponseHasErrorsException;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;

/**
 * <p>A simple client that allows Lightblue plugins to talk directly to the
 * {@link com.redhat.lightblue.mediator.Mediator} to perform CRUD operations.</p>
 * <p>A client application that is not part of the lightblue framework should not use
 * this class, rather look into using the language specific lightblue-client library.<p>
 *
 * @author dcrissman
 */
public class MediatorClient {

    private final LightblueFactory lbFactory;

    public MediatorClient(LightblueFactory lbFactory) {
        this.lbFactory = lbFactory;
    }

    /**
     * <p>Builds the {@link InsertionRequest} and pass that along to the mediator.</p>
     * <p>Uses jackson to convert POJO to JSON. See the com.fasterxml.jackson.annotation
     * namespace if the default translations won't work.</p>
     * @param entityName - entity name
     * @param entityVersion - entity version, can be null if the default version is acceptable.
     * @param entity - POJO object to be transformed to be inserted
     * @return {@link Response}
     * @throws ResponseHasErrorsException
     */
    public Response insert(String entityName, String entityVersion, Object entity) throws ResponseHasErrorsException {
        if (entityName == null) {
            throw new IllegalArgumentException("entityName cannot be null");
        }
        else if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }

        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("entity", entityName);
        if (entityVersion != null) {
            jsonNode.put("entityVersion", entityVersion);
        }
        ArrayNode data = jsonNode.putArray("data");
        data.add(new ObjectMapper().valueToTree(entity));

        return insert(InsertionRequest.fromJson(jsonNode));
    }

    /**
     * Passes the {@link InsertionRequest} request onto the
     * {@link com.redhat.lightblue.mediator.Mediator} and returns the {@link Response}.
     * @param insertRequest - {@link InsertionRequest}
     * @return {@link Response}.
     * @throws ResponseHasErrorsException
     */
    public Response insert(InsertionRequest insertRequest) throws ResponseHasErrorsException {
        if (insertRequest == null) {
            throw new IllegalArgumentException("insertRequest cannot be null");
        }

        try {
            return checkForErrors(lbFactory.getMediator().insert(insertRequest));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
            throw new RuntimeException("Unexpected exception from Mediator.", e);
        }
    }

    /**
     * Passes the {@link FindRequest} request onto the
     * {@link com.redhat.lightblue.mediator.Mediator} and returns the {@link Response}.
     * @param insertRequest - {@link FindRequest}
     * @return {@link Response}.
     * @throws ResponseHasErrorsException
     */
    public Response find(FindRequest findRequest) throws ResponseHasErrorsException {
        if (findRequest == null) {
            throw new IllegalArgumentException("findRequest cannot be null");
        }

        try {
            return checkForErrors(lbFactory.getMediator().find(findRequest));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
            throw new RuntimeException("Unexpected exception from Mediator.", e);
        }
    }

    /**
     * Passes the {@link UpdateRequest} request onto the
     * {@link com.redhat.lightblue.mediator.Mediator} and returns the {@link Response}.
     * @param insertRequest - {@link UpdateRequest}
     * @return {@link Response}.
     * @throws ResponseHasErrorsException
     */
    public Response update(UpdateRequest updateRequest) throws ResponseHasErrorsException {
        if (updateRequest == null) {
            throw new IllegalArgumentException("updateRequest cannot be null");
        }

        try {
            return checkForErrors(lbFactory.getMediator().update(updateRequest));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
            throw new RuntimeException("Unexpected exception from Mediator.", e);
        }
    }

    /**
     * Passes the {@link SaveRequest} request onto the
     * {@link com.redhat.lightblue.mediator.Mediator} and returns the {@link Response}.
     * @param insertRequest - {@link SaveRequest}
     * @return {@link Response}.
     * @throws ResponseHasErrorsException
     */
    public Response save(SaveRequest saveRequest) throws ResponseHasErrorsException {
        if (saveRequest == null) {
            throw new IllegalArgumentException("saveRequest cannot be null");
        }

        try {
            return checkForErrors(lbFactory.getMediator().save(saveRequest));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
            throw new RuntimeException("Unexpected exception from Mediator.", e);
        }
    }

    /**
     * Passes the {@link DeleteRequest} request onto the
     * {@link com.redhat.lightblue.mediator.Mediator} and returns the {@link Response}.
     * @param insertRequest - {@link DeleteRequest}
     * @return {@link Response}.
     * @throws ResponseHasErrorsException
     */
    public Response delete(DeleteRequest deleteRequest) throws ResponseHasErrorsException {
        if (deleteRequest == null) {
            throw new IllegalArgumentException("deleteRequest cannot be null");
        }

        try {
            return checkForErrors(lbFactory.getMediator().delete(deleteRequest));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | IOException e) {
            throw new RuntimeException("Unexpected exception from Mediator.", e);
        }
    }

    private Response checkForErrors(Response response) throws ResponseHasErrorsException {
        if (!response.getErrors().isEmpty() || !response.getDataErrors().isEmpty()) {
            throw new ResponseHasErrorsException(response);
        }
        return response;
    }

}
