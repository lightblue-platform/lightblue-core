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
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class RemoveEntityCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveEntityCommand.class);

    private final String entity;

    public RemoveEntityCommand(String clientKey, String entity) {
        this(clientKey, null, entity);
    }

    public RemoveEntityCommand(String clientKey, Metadata metadata, String entity) {
        super(RemoveEntityCommand.class, clientKey, metadata);
        this.entity = entity;
    }

    @Override
    protected String run() {
        LOGGER.debug("run:");
        Error.reset();
        Error.push(getClass().getSimpleName());
        try {
            getMetadata().removeEntity(entity);
            return "";
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}", e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR, e.toString()).toString();
        }
    }
}
