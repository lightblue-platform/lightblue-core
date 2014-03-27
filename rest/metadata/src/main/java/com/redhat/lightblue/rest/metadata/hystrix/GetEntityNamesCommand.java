/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class GetEntityNamesCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);

    public GetEntityNamesCommand(String clientKey) {
        this(clientKey, null);
    }

    public GetEntityNamesCommand(String clientKey, Metadata metadata) {
        super(GetEntityNamesCommand.class.getSimpleName(), GetEntityNamesCommand.class.getSimpleName(), clientKey, metadata);
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("run:");
        Error.reset();
        Error.push(getClass().getSimpleName());
        try {
            String[] names = getMetadata().getEntityNames();
            ObjectNode node = NODE_FACTORY.objectNode();
            ArrayNode arr = NODE_FACTORY.arrayNode();
            node.put("entities", arr);
            for (String x : names) {
                arr.add(NODE_FACTORY.textNode(x));
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
