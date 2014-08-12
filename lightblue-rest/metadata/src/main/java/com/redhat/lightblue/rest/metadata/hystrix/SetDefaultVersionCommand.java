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

import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetDefaultVersionCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetDefaultVersionCommand.class);
    private final String entity;
    private final String version;

    public SetDefaultVersionCommand(String clientKey, String entity, String version) {
        this(clientKey, null, entity, version);
    }

    public SetDefaultVersionCommand(String clientKey, Metadata metadata, String entity, String version) {
        super(SetDefaultVersionCommand.class, clientKey, metadata);
        this.entity = entity;
        this.version = version;
    }

    @Override
    protected String run() {
        LOGGER.debug("run: entity={}, version={}", entity, version);
        Error.reset();
        Error.push(getClass().getSimpleName());
        Error.push(entity);
        if (version != null) {
            Error.push(version);
        }
        try {
            Metadata md = getMetadata();
            EntityInfo ei = md.getEntityInfo(entity);
            if (ei != null) {
                ei.setDefaultVersion(version);
                md.updateEntityInfo(ei);
            } else {
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_VERSION, entity + ":" + version);
            }
            if (version == null) {
                return getJSONParser().convert(md.getEntityInfo(entity)).toString();
            } else {
                return getJSONParser().convert(md.getEntityMetadata(entity, version)).toString();
            }
        } catch (Error e) {
            LOGGER.error("Cannot set version", e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}", e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR, e.toString()).toString();
        }
    }
}
