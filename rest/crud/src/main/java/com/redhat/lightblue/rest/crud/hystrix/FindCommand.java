/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud.hystrix;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.rest.crud.RestCrudConstants;
import com.redhat.lightblue.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class FindCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindCommand.class);

    private final String entity;
    private final String version;
    private final String request;

    public FindCommand(String clientKey, String entity, String version, String request) {
        this(clientKey, null, entity, version, request);
    }

    public FindCommand(String clientKey, Mediator mediator, String entity, String version, String request) {
        super(FindCommand.class.getSimpleName(), FindCommand.class.getSimpleName(), clientKey, mediator);
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
            FindRequest ireq = FindRequest.fromJson((ObjectNode) JsonUtils.json(request));
            validateReq(ireq, entity, version);
            Response r = getMediator().find(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("find failure: {}", e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("find failure: {}", e);
            return Error.get(RestCrudConstants.ERR_REST_FIND, e.toString()).toString();
        } finally {
            Error.reset();
        }
    }
}
