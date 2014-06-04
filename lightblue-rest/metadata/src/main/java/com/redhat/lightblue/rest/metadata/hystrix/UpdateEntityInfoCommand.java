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
package com.redhat.lightblue.rest.metadata.hystrix;

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
        this(clientKey, null, entity, info);
    }

    public UpdateEntityInfoCommand(String clientKey, Metadata metadata, String entity, String info) {
        super(UpdateEntityInfoCommand.class, clientKey, metadata);
        this.entity = entity;
        this.info = info;
    }

    @Override
    protected String run() {
        LOGGER.debug("updateEntityInfo {}", entity);
        Error.reset();
        Error.push("updateEntityInfo");
        Error.push(entity);
        try {
            JSONMetadataParser parser = getJSONParser();
            EntityInfo ei = parser.parseEntityInfo(JsonUtils.json(info));
            if (!ei.getName().equals(entity)) {
                throw Error.get(RestMetadataConstants.ERR_NO_NAME_MATCH, entity);
            }

            Metadata md = getMetadata();
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
