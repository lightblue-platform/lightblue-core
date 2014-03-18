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
package com.redhat.lightblue.rest.metadata;

import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple service to test out NewRelic custom metrics.
 *
 * @author nmalik
 */
@Path("/metadata")
public class MetadataResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResource.class);

    public static final String PATH_PARAM_ENTITY = "entity";
    public static final String PATH_PARAM_VERSION = "version";

    @GET
    @Path("/{entity}/{version}")
    public String getMetadata(@PathParam(PATH_PARAM_ENTITY) String entityName, @PathParam(PATH_PARAM_VERSION) String entityVersion) {
        try {
            if (entityName == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
            }
            if (entityVersion == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_VERSION);
            }

            // get the data
            EntityMetadata em = MetadataManager.getMetadata().getEntityMetadata(entityName, entityVersion);

            // convert to json and return
            return MetadataManager.getJSONParser().convert(em).toString();
        } catch (Error e) {
            return e.toJson().toString();
        } catch (Exception e) {
            return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
        }
    }

    @GET
    @Path("/names")
    public String getEntityNames() {
        try {
            // get the data
            String[] names = MetadataManager.getMetadata().getEntityNames();

            // convert to json and return
            return JsonUtils.toJson(names);
        } catch (Error e) {
            return e.toJson().toString();
        } catch (Exception e) {
            return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
        }
    }

    @GET
    @Path("/{entity}/versions")
    public String getMetadataVersions(@PathParam(PATH_PARAM_ENTITY) String entityName) {
        try {
            if (entityName == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
            }

            // get the data
            Version[] versions = MetadataManager.getMetadata().getEntityVersions(entityName);

            StringBuilder buff = new StringBuilder("[");
            for (int x = 0; x < versions.length; x++) {
                buff.append(MetadataManager.getJSONParser().convert(versions[x]));
            }
            buff.append("]");
            return buff.toString();
        } catch (Error e) {
            return e.toJson().toString();
        } catch (Exception e) {
            return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
        }
    }

    /**
     * Body is required metadata (json).
     *
     * @param entityName
     * @param entityVersion
     * @param metadata
     * @return the metadata created from this request
     */
    @PUT
    @Path("/{entity}/{version}")
    public String createMetadata(@PathParam(PATH_PARAM_ENTITY) String entityName, @PathParam(PATH_PARAM_VERSION) String entityVersion, String metadata) {
        try {
            if (entityName == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
            }
            if (entityVersion == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_VERSION);
            }

            // convert to object
            EntityMetadata em = MetadataManager.getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));

            // verify entity name and version
            if (!entityName.equals(em.getName())) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_NAME_MATCH);
            }
            if (!entityVersion.equals(em.getVersion().getValue())) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_VERSION_MATCH);
            }

            // execute creation
            MetadataManager.getMetadata().createNewMetadata(em);

            // if successful fetch the metadata back out of the database and return it (simply reuse the existing rest api to do it)
            return getMetadata(entityName, entityVersion);
        } catch (Error e) {
            return e.toJson().toString();
        } catch (Exception e) {
            return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
        }
    }

    /**
     * Body is optional comment.
     *
     * @param entityName
     * @param entityVersion
     * @param status
     * @param comment
     * @return the metadata updated from this request
     */
    @PUT
    @Path("/{entity}/{version}/{status}")
    public String updateMetadataStatus(@PathParam(PATH_PARAM_ENTITY) String entityName, @PathParam(PATH_PARAM_VERSION) String entityVersion, @PathParam("status") String status, String comment) {
        try {
            if (entityName == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
            }
            if (entityVersion == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_VERSION);
            }
            if (status == null) {
                throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_STATUS);
            }

            MetadataStatus ms = MetadataStatus.valueOf(status);

            MetadataManager.getMetadata().setMetadataStatus(entityName, entityVersion, ms, comment);

            // if successful fetch the metadata back out of the database and return it (simply reuse the existing rest api to do it)
            return getMetadata(entityName, entityVersion);
        } catch (Error e) {
            return e.toJson().toString();
        } catch (Exception e) {
            return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
        }
    }
}
