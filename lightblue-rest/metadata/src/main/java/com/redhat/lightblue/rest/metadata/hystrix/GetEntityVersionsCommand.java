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
import com.redhat.lightblue.metadata.VersionInfo;
import com.redhat.lightblue.metadata.parser.MetadataParser;
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
            VersionInfo[] versions = getMetadata().getEntityVersions(entity);
            ArrayNode arr = NODE_FACTORY.arrayNode();

            JSONMetadataParser parser = getJSONParser();
            for (VersionInfo x : versions) {
                ObjectNode obj=NODE_FACTORY.objectNode();
                obj.put("version",x.getValue());
                obj.put("changelog",x.getChangelog());
                ArrayNode ev=NODE_FACTORY.arrayNode();
                if(x.getExtendsVersions()!=null) {
                    for(String v:x.getExtendsVersions())
                        ev.add(NODE_FACTORY.textNode(v));
                }
                obj.set("extendsVersions",ev);
                obj.put("status",MetadataParser.toString(x.getStatus()));
                obj.put("defaultVersion",x.isDefault());
                arr.add(obj);
            }
            return arr.toString();
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
