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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Simple service to test out NewRelic custom metrics.
 *
 * @author nmalik
 * @author bserdar
 * @see https://github.com/bserdar/lightblue/wiki/Rest-Spec-Metadata#rest-spec-metadata
 */
@Path("/") // metadata/ prefix is the application context
@Produces(MediaType.APPLICATION_JSON)
public class MetadataResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResource.class);

    private static final JsonNodeFactory NODE_FACTORY=JsonNodeFactory.withExactBigDecimals(true);

    @GET @Path("/dependencies")
    public String getDepGraph() {
        return getDepGraph(null,null);
    }

    @GET @Path("/{entity}/dependencies")
    public String getDepGraph(@PathParam("entity") String entity) {
        return getDepGraph(entity,null);
    }

    @GET @Path("/{entity}/{version}/dependencies")
    public String getDepGraph(@PathParam("entity") String entity,@PathParam("version") String version) {
        LOGGER.debug("getDepGraph: entity: {}, version: {}",entity,version);
        return "{\"not\":\"implemented\"}";
    }

    @GET @Path("/roles")
    public String getEntityRoles() {
        return getEntityRoles(null,null);
    }

    @GET @Path("/{entity}/roles")
    public String getEntityRoles(@PathParam("entity") String entity) {
        return getEntityRoles(entity,null);
    }

    @GET @Path("/{entity}/{version}/roles")
    public String getEntityRoles(@PathParam("entity") String entity,@PathParam("version") String version) {
        LOGGER.debug("getEntityRoles: entity: {}, version: {}",entity,version);
        return "{\"not\":\"implemented\"}";
    }

    @GET @Path("/")
    public String getEntityNames() {
        LOGGER.debug("getEntityNames:");
        Error.push("getEntityNames");
        try {
            Metadata md=MetadataManager.getMetadata();
            String[] names=md.getEntityNames();
            ObjectNode node=NODE_FACTORY.objectNode();
            ArrayNode arr=NODE_FACTORY.arrayNode();
            node.put("entities",arr);
            for(String x:names)
                arr.add(NODE_FACTORY.textNode(x));
            return node.toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.pop();
        }
    }

    @GET @Path("/{entity}")
    public String getEntityVersions(@PathParam("entity") String entity) {
        return "";
    }

    @GET @Path("/{entity}/{version}")
    public String getMetadata(@PathParam("entity") String entity,@PathParam("version") String version) {
        return "";
    }

    /* Commented just because it isn't implemented yet, so the old implementation will be uncommented just to keep the tests running
    @PUT @Path("/{entity}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createMetadata(@PathParam("entity") String entity,@PathParam("version") String version,String metadata) {
        return "";
    }*/

    @PUT @Path("/{entity}/schema={version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createSchema(@PathParam("entity") String entity,@PathParam("version") String version,String schema) {
        return "";
    }

    @PUT @Path("/{entity}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateEntityInfo(@PathParam("entity") String entity,String metadata) {
        return "";
    }


    @PUT @Path("/{entity}/{version}/{status}")
    public String updateSchemaStatus(@PathParam("entity") String entity, @PathParam("version") String version, @PathParam("status") String status) {
        return "";
    }

    // @GET
    // @Path("/{entity}/{version}/{forceVersion}")
    public static final String PATH_PARAM_ENTITY = "entity";
    public static final String PATH_PARAM_VERSION = "version";
    public static final String PATH_PARAM_FORCEVERSION = "forceVersion";
    public String getMetadata(@PathParam(PATH_PARAM_ENTITY) String entityName, @PathParam(PATH_PARAM_VERSION) String entityVersion, @PathParam(PATH_PARAM_FORCEVERSION) boolean forceVersion) {
         try {
             if (entityName == null) {
                 throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
             }
             if (entityVersion == null) {
                 throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_VERSION);
             }

             // get the data
             EntityMetadata em = MetadataManager.getMetadata().getEntityMetadata(entityName, entityVersion, forceVersion);

             // convert to json and return
             return MetadataManager.getJSONParser().convert(em).toString();
         } catch (Error e) {
             return e.toJson().toString();
         } catch (Exception e) {
             return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
         }
    }

    // @GET
    // @Path("/names")
    // public String getEntityNames() {
    //     try {
    //         // get the data
    //         String[] names = MetadataManager.getMetadata().getEntityNames();

    //         // convert to json and return
    //         return JsonUtils.toJson(names);
    //     } catch (Error e) {
    //         return e.toJson().toString();
    //     } catch (Exception e) {
    //         return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
    //     }
    // }

    // @GET
    // @Path("/{entity}/versions")
    // public String getMetadataVersions(@PathParam(PATH_PARAM_ENTITY) String entityName) {
    //     try {
    //         if (entityName == null) {
    //             throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
    //         }

    //         // get the data
    //         Version[] versions = MetadataManager.getMetadata().getEntityVersions(entityName);

    //         StringBuilder buff = new StringBuilder("[");
    //         for (Version version : versions) {
    //             buff.append(MetadataManager.getJSONParser().convert(version));
    //         }
    //         buff.append("]");
    //         return buff.toString();
    //     } catch (Error e) {
    //         return e.toJson().toString();
    //     } catch (Exception e) {
    //         return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
    //     }
    // }

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
    @Consumes(MediaType.APPLICATION_JSON)
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
             return getMetadata(entityName, entityVersion, true);
         } catch (Error e) {
             return e.toJson().toString();
         } catch (Exception e) {
             return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
         }
    }

    // /**
    //  * Body is optional comment.
    //  *
    //  * @param entityName
    //  * @param entityVersion
    //  * @param status
    //  * @param comment
    //  * @return the metadata updated from this request
    //  */
    // @PUT
    // @Path("/{entity}/{version}/{status}")
    // public String updateMetadataStatus(@PathParam(PATH_PARAM_ENTITY) String entityName, @PathParam(PATH_PARAM_VERSION) String entityVersion, @PathParam("status") String status, String comment) {
    //     try {
    //         if (entityName == null) {
    //             throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_NAME);
    //         }
    //         if (entityVersion == null) {
    //             throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_VERSION);
    //         }
    //         if (status == null) {
    //             throw Error.get(RestMetadataConstants.ERR_REST_ERROR, RestMetadataConstants.ERR_NO_ENTITY_STATUS);
    //         }

    //         MetadataStatus ms = MetadataStatus.valueOf(status);

    //         MetadataManager.getMetadata().setMetadataStatus(entityName, entityVersion, ms, comment);

    //         // if successful fetch the metadata back out of the database and return it (simply reuse the existing rest api to do it)
    //         return getMetadata(entityName, entityVersion, true);
    //     } catch (Error e) {
    //         return e.toJson().toString();
    //     } catch (Exception e) {
    //         return Error.get(RestMetadataConstants.ERR_REST_ERROR).toJson().toString();
    //     }
    // }
}
