/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class UpdateEntityInfoCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);
    private final String entity;
    private final String info;

    public UpdateEntityInfoCommand(String clientKey, String entity, String info) {
        super(UpdateEntityInfoCommand.class.getSimpleName(), UpdateEntityInfoCommand.class.getSimpleName(), clientKey);
        this.entity = entity;
        this.info = info;
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("updateEntityInfo {}", entity);
        Error.reset();
        Error.push("updateEntityInfo");
        Error.push(entity);
        try {
            JSONMetadataParser parser = MetadataManager.getJSONParser();
            EntityInfo ei = parser.parseEntityInfo(JsonUtils.json(info));
            if (!ei.getName().equals(entity)) {
                throw Error.get(RestMetadataConstants.ERR_NO_NAME_MATCH, entity);
            }

            Metadata md = MetadataManager.getMetadata();
            md.updateEntityInfo(ei);
            ei = md.getEntityInfo(entity);
            return parser.convert(ei).toString();
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
