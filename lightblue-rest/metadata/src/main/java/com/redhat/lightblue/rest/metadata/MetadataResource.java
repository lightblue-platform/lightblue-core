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

import java.util.StringTokenizer;

import com.redhat.lightblue.rest.metadata.hystrix.*;
import com.redhat.lightblue.util.Error;

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
    private static final String PARAM_ENTITY = "entity";
    private static final String PARAM_VERSION = "version";

    @GET
    @Path("/dependencies")
    public String getDepGraph() {
        return getDepGraph(null, null);
    }

    @GET
    @Path("/{entity}/dependencies")
    public String getDepGraph(@PathParam(PARAM_ENTITY) String entity) {
        return getDepGraph(entity, null);
    }

    @GET
    @Path("/{entity}/{version}/dependencies")
    public String getDepGraph(@PathParam(PARAM_ENTITY) String entity, @PathParam(PARAM_VERSION) String version) {
        Error.reset();
        return new GetDependenciesCommand(null, entity, version).execute();
    }

    @GET
    @Path("/roles")
    public String getEntityRoles() {
        return getEntityRoles(null, null);
    }

    @GET
    @Path("/{entity}/roles")
    public String getEntityRoles(@PathParam(PARAM_ENTITY) String entity) {
        return getEntityRoles(entity, null);
    }

    @GET
    @Path("/{entity}/{version}/roles")
    public String getEntityRoles(@PathParam(PARAM_ENTITY) String entity, @PathParam(PARAM_VERSION) String version) {
        Error.reset();
        return new GetEntityRolesCommand(null, entity, version).execute();
    }

    @GET
    @Path("/")
    public String getEntityNames() {
        Error.reset();
        return new GetEntityNamesCommand(null, new String[0]).execute();
    }

    @GET
    @Path("/s={statuses}")
    public String getEntityNames(@PathParam("statuses") String statuses) {
        StringTokenizer tok = new StringTokenizer(" ,;:.");
        String[] s = new String[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            s[i++] = tok.nextToken();
        }
        Error.reset();
        return new GetEntityNamesCommand(null, s).execute();
    }

    @GET
    @Path("/{entity}")
    public String getEntityVersions(@PathParam(PARAM_ENTITY) String entity) {
        Error.reset();
        return new GetEntityVersionsCommand(null, entity).execute();
    }

    @GET
    @Path("/{entity}/{version}")
    public String getMetadata(@PathParam(PARAM_ENTITY) String entity, @PathParam(PARAM_VERSION) String version) {
        Error.reset();
        return new GetEntityMetadataCommand(null, entity, version).execute();
    }

    @PUT
    @Path("/{entity}/{version}")
    public String createMetadata(@PathParam(PARAM_ENTITY) String entity, @PathParam(PARAM_VERSION) String version, String data) {
        Error.reset();
        return new CreateEntityMetadataCommand(null, entity, version, data).execute();
    }

    @PUT
    @Path("/{entity}/schema={version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createSchema(@PathParam(PARAM_ENTITY) String entity, @PathParam(PARAM_VERSION) String version, String schema) {
        Error.reset();
        return new CreateEntitySchemaCommand(null, entity, version, schema).execute();
    }

    @PUT
    @Path("/{entity}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateEntityInfo(@PathParam(PARAM_ENTITY) String entity, String info) {
        Error.reset();
        return new UpdateEntityInfoCommand(null, entity, info).execute();
    }

    @PUT
    @Path("/{entity}/{version}/{status}")
    public String updateSchemaStatus(@PathParam(PARAM_ENTITY) String entity,
                                     @PathParam(PARAM_VERSION) String version,
                                     @PathParam("status") String status,
                                     @QueryParam("comment") String comment) {
        Error.reset();
        return new UpdateEntitySchemaStatusCommand(null, entity, version, status, comment).execute();
    }

    @POST
    @Path("/{entity}/{version}/default")
    public String setDefaultVersion(@PathParam(PARAM_ENTITY) String entity,
                                    @PathParam(PARAM_VERSION) String version) {
        Error.reset();
        return new SetDefaultVersionCommand(null, entity, version).execute();
    }

    @DELETE
    @Path("/{entity}")
    public String removeEntity(@PathParam(PARAM_ENTITY) String entity) {
        Error.reset();
        return new RemoveEntityCommand(null, entity).execute();
    }

    @DELETE
    @Path("/{entity}/default")
    public String clearDefaultVersion(@PathParam(PARAM_ENTITY) String entity) {
        Error.reset();
        return new SetDefaultVersionCommand(null, entity, null).execute();
    }
}
