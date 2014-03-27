/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.crud.hystrix;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.CrudManager;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.rest.crud.RestCrudConstants;
import com.redhat.lightblue.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class SaveCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveCommand.class);

    private final String entity;
    private final String version;
    private final String request;

    public SaveCommand(String clientKey, String entity, String version, String request) {
        super(SaveCommand.class.getSimpleName(), SaveCommand.class.getSimpleName(), clientKey);
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
            SaveRequest ireq = SaveRequest.fromJson((ObjectNode) JsonUtils.json(request));
            validateReq(ireq, entity, version);
            Response r = CrudManager.getMediator().save(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("save failure: {}", e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("save failure: {}", e);
            return Error.get(RestCrudConstants.ERR_REST_SAVE, e.toString()).toString();
        } finally {
            Error.reset();
        }
    }
}
