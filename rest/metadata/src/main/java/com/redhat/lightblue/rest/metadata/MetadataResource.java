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

import com.redhat.lightblue.rest.metadata.hystrix.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author nmalik
 * @author bserdar
 * @see https://github.com/bserdar/lightblue/wiki/Rest-Spec-Metadata#rest-spec-metadata
 */
//metadata/ prefix is the application context
@Path("/") 
@Produces(MediaType.APPLICATION_JSON)
public class MetadataResource {
    @GET
    @Path("/dependencies")
    public String getDepGraph() {
        return getDepGraph(null, null);
    }

    @GET
    @Path("/{entity}/dependencies")
    public String getDepGraph(@PathParam("entity") String entity) {
        return getDepGraph(entity, null);
    }

    @GET
    @Path("/{entity}/{version}/dependencies")
    public String getDepGraph(@PathParam("entity") String entity, @PathParam("version") String version) {
        return new GetDependenciesCommand(null, entity, version).execute();
    }

    @GET
    @Path("/roles")
    public String getEntityRoles() {
        return getEntityRoles(null, null);
    }

    @GET
    @Path("/{entity}/roles")
    public String getEntityRoles(@PathParam("entity") String entity) {
        return getEntityRoles(entity, null);
    }

    @GET
    @Path("/{entity}/{version}/roles")
    public String getEntityRoles(@PathParam("entity") String entity, @PathParam("version") String version) {
        return new GetEntityRolesCommand(null, entity, version).execute();
    }

    @GET
    @Path("/")
    public String getEntityNames() {
        return new GetEntityNamesCommand(null).execute();
    }

    @GET
    @Path("/{entity}")
    public String getEntityVersions(@PathParam("entity") String entity) {
        return new GetEntityVersionsCommand(null, entity).execute();
    }

    @GET
    @Path("/{entity}/{version}")
    public String getMetadata(@PathParam("entity") String entity, @PathParam("version") String version) {
        return new GetEntityMetadataCommand(null, entity, version).execute();
    }

    @PUT
    @Path("/{entity}/{version}")
    public String createMetadata(@PathParam("entity") String entity, @PathParam("version") String version, String data) {
        return new CreateEntityMetadataCommand(null, entity, version, data).execute();
    }

    @PUT
    @Path("/{entity}/schema={version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createSchema(@PathParam("entity") String entity, @PathParam("version") String version, String schema) {
        return new CreateEntitySchemaCommand(null, entity, version, schema).execute();
    }

    @PUT
    @Path("/{entity}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateEntityInfo(@PathParam("entity") String entity, String info) {
        return new UpdateEntityInfoCommand(null, entity, info).execute();
    }

    @PUT
    @Path("/{entity}/{version}/{status}")
    public String updateSchemaStatus(@PathParam("entity") String entity,
                                     @PathParam("version") String version,
                                     @PathParam("status") String status,
                                     @QueryParam("comment") String comment) {
        return new UpdateEntitySchemaStatusCommand(null, entity, version, status, comment).execute();
    }

    @POST
    @Path("/{entity}/{version}/default")
    public String setDefaultVersion(@PathParam("entity") String entity,
                                    @PathParam("version") String version) {
        return new SetDefaultVersionCommand(null, entity, version).execute();
    }
}
