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
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

/**
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
        Error.rest();
        Error.push("getEntityNames");
        try {
            String[] names=MetadataManager.getMetadata().getEntityNames();
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
            Error.reset();
        }
    }

    @GET @Path("/{entity}")
    public String getEntityVersions(@PathParam("entity") String entity) {
        LOGGER.debug("getEntityVersions: {}",entity);
        Error.reset();
        Error.push("getEntityVersions");
        try {
            Version[] versions=MetadataManager.getMetadata().getEntityVersions(entity);
            ObjectNode node=NODE_FACTORY.objectNode();
            ArrayNode arr=NODE_FACTORY.arrayNode();
            node.put("versions",arr);
            JSONMetadataParser parser=MetadataManager.getJSONParser();
            for(Version x:versions)
                arr.add(parser.convert(x));
            return node.toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @GET @Path("/{entity}/{version}")
    public String getMetadata(@PathParam("entity") String entity,@PathParam("version") String version) {
        LOGGER.debug("getMetadata {} {}",entity,version);
        Error.reset();
        Error.push("getMetadata");
        if("default".equals(version))
            version=null;
        try {
            EntityMetadata md=MetadataManager.getMetadata().getEntityMetadata(entity,version);
            if(md!=null) {
                JSONMetadataParser parser=MetadataManager.getJSONParser();
                return parser.convert(md);
            } else
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_VERSION,entity+":"+version);
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @PUT @Path("/{entity}/{version}")
    public String createMetadata(@PathParam("entity") String entity,@PathParam("version") String version,String data) {
        LOGGER.debug("createMetadata {} {}",entity,version);
        Error.reset();
        Error.push("createMetadata");
        Error.push(entity);
        Error.push(version);
        try {
            JSONMetadataParser parser=MetadataManager.getJSONParser();
            EntityMetadata emd=parser.parseEntityMetadata(JsonUtils.json(data));
            Metadata md=MetadataManager.getMetadata();
            md.createNewMetadata(md);
            emd=md.getEntityMetadata(entity,version);
            return parser.convert(emd).toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @PUT @Path("/{entity}/schema={version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createSchema(@PathParam("entity") String entity,@PathParam("version") String version,String schema) {
        LOGGER.debug("createSchema {} {}",entity,version);
        Error.reset();
        Error.push("createSchema");
        Error.push(entity);
        Error.push(version);
        try {
            JSONMetadataParser parser=MetadataManager.getJSONParser();
            EntitySchema sch=parser.parseEntitySchema(JsonUtils.json(schema));

            Metadata md=MetadataManager.getMetadata();
            EntityInfo ei=md.getEntityInfo(entity);
            if(ei==null)
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_NANE,entity);

            EntityMetadata emd=new EntityMetadata(ei,sch);
            md.createNewSchema(md);
            emd=md.getEntityMetadata(entity,version);
            return parser.convert(emd).toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.reset();
        }
     }

    @PUT @Path("/{entity}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateEntityInfo(@PathParam("entity") String entity,String info) {
        LOGGER.debug("updateEntityInfo {}",entity);
        Error.reset();
        Error.push("updateEntityInfo");
        Error.push(entity);
        try {
            JSONMetadataParser parser=MetadataManager.getJSONParser();
            EntityInfo ei=parser.parseEntityInfo(JsonUtils.json(info));

            Metadata md=MetadataManager.getMetadata();
            EntityInfo ei=md.getEntityInfo(entity);
            if(ei==null)
                throw Error.get(RestMetadataConstants.ERR_NO_ENTITY_NANE,entity);

            EntityMetadata emd=new EntityMetadata(ei,sch);
            md.createNewSchema(md);
            emd=md.getEntityMetadata(entity,version);
            return parser.convert(emd).toString();
        } catch (Error e) {
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("Failure: {}",e);
            return Error.get(RestMetadataConstants.ERR_REST_ERROR,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }


    @PUT @Path("/{entity}/{version}/{status}")
    public String updateSchemaStatus(@PathParam("entity") String entity, @PathParam("version") String version, @PathParam("status") String status) {
        return "";
    }
}
