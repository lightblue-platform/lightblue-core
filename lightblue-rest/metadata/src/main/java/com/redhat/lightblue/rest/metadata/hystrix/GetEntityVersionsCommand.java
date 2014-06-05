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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.Metadata;
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
        this(clientKey, null, entity);
    }

    public GetEntityVersionsCommand(String clientKey, Metadata metadata, String entity) {
        super(GetEntityVersionsCommand.class, clientKey, metadata);
        this.entity = entity;
    }

    @Override
    protected String run() {
        LOGGER.debug("run: entity={}", entity);
        Error.reset();
        Error.push(getClass().getSimpleName());
        try {
            Version[] versions = getMetadata().getEntityVersions(entity);
            ObjectNode node = NODE_FACTORY.objectNode();
            ArrayNode arr = NODE_FACTORY.arrayNode();
            node.put("versions", arr);
            JSONMetadataParser parser = getJSONParser();
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
