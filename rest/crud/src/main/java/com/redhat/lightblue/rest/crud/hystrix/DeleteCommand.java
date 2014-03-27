/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud.hystrix;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.rest.crud.RestCrudConstants;
import com.redhat.lightblue.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class DeleteCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCommand.class);

    private final String entity;
    private final String version;
    private final String request;

    public DeleteCommand(String clientKey, String entity, String version, String request) {
        this(clientKey, null, entity, version, request);
    }

    public DeleteCommand(String clientKey, Mediator mediator, String entity, String version, String request) {
        super(DeleteCommand.class.getSimpleName(), DeleteCommand.class.getSimpleName(), clientKey, mediator);
        this.entity = entity;
        this.version = version;
        this.request = request;
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("run: entity={}, version={}", entity, version);
        Error.reset();
        Error.push(getClass().getSimpleName());
        Error.push(entity);
        try {
            DeleteRequest ireq = DeleteRequest.fromJson((ObjectNode) JsonUtils.json(request));
            validateReq(ireq, entity, version);
            Response r = getMediator().delete(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("delete failure: {}", e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("delete failure: {}", e);
            return Error.get(RestCrudConstants.ERR_REST_DELETE, e.toString()).toString();
        } finally {
            Error.reset();
        }
    }
}
