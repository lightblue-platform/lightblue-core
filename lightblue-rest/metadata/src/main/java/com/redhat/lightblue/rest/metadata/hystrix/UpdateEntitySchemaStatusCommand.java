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
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.rest.metadata.RestMetadataConstants;
import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nmalik
 */
public class UpdateEntitySchemaStatusCommand extends AbstractRestCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntityRolesCommand.class);
    private final String entity;
    private final String version;
    private final String status;
    private final String comment;

    public UpdateEntitySchemaStatusCommand(String clientKey, String entity, String version, String status, String comment) {
        this(clientKey, null, entity, version, status, comment);
    }

    public UpdateEntitySchemaStatusCommand(String clientKey, Metadata metadata, String entity, String version, String status, String comment) {
        super(UpdateEntitySchemaStatusCommand.class, clientKey, metadata);
        this.entity = entity;
        this.version = version;
        this.status = status;
        this.comment = comment;
    }

    @Override
    protected String run() {
        LOGGER.debug("run: enitty={}, version={}, status={}", entity, version, status);
        Error.reset();
        Error.push(getClass().getSimpleName());
        Error.push(entity);
        Error.push(version);
        try {
            MetadataStatus st = MetadataParser.statusFromString(status);
            Metadata md = getMetadata();
            md.setMetadataStatus(entity, version, st, comment);
            return getJSONParser().convert(md.getEntityMetadata(entity, version)).toString();
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
