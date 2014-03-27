/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class GetEntityMetadataCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);
    private final String entity;
    private final String version;

    public GetEntityMetadataCommand(String clientKey, String entity, String version) {
        super(GetEntityMetadataCommand.class.getSimpleName(), GetEntityMetadataCommand.class.getSimpleName(), clientKey);
        this.entity = entity;
        if ("default".equals(version)) {
            this.version = null;
        } else {
            this.version = version;
        }
    }

    @Override
    protected String run() throws Exception {
        LOGGER.debug("run: entity={}, version={}", entity, version);
        Error.reset();
        Error.push(getClass().getSimpleName());
        try {
            EntityMetadata md = getMetadata().getEntityMetadata(entity, version);
            if (md != null) {
                JSONMetadataParser parser = MetadataManager.getJSONParser();
                return parser.convert(md).toString();
            } else {
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_VERSION, entity + ":" + version);
            }
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
