/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class GetEntityVersionsCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);

    private final String entity;

    public GetEntityVersionsCommand(String clientKey, String entity) {
        super(GetEntityVersionsCommand.class.getSimpleName(), GetEntityVersionsCommand.class.getSimpleName(), clientKey);
        this.entity = entity;
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("run: entity={}", entity);
        Error.reset();
        Error.push(getClass().getSimpleName());
        try {
            Version[] versions = getMetadata().getEntityVersions(entity);
            ObjectNode node = NODE_FACTORY.objectNode();
            ArrayNode arr = NODE_FACTORY.arrayNode();
            node.put("versions", arr);
            JSONMetadataParser parser = MetadataManager.getJSONParser();
            for (Version x : versions) {
                arr.add(parser.convert(x));
            }
            return node.toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}", e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR, e.toString()).toString();
        } finally {
            Error.reset();
        }
    }
}
