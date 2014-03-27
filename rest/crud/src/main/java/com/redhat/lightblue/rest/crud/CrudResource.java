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
package com.redhat.lightblue.rest.crud;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import com.redhat.lightblue.rest.crud.hystrix.DeleteCommand;
import com.redhat.lightblue.rest.crud.hystrix.FindCommand;
import com.redhat.lightblue.rest.crud.hystrix.InsertCommand;
import com.redhat.lightblue.rest.crud.hystrix.SaveCommand;
import com.redhat.lightblue.rest.crud.hystrix.UpdateCommand;

/**
 *
 * @author nmalik
 * @author bserdar
 */
@Path("/") // metadata/ prefix is the application context
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudResource {
    @PUT
    @Path("/{entity}")
    public String insert(@PathParam("entity") String entity, String request) {
        return insert(entity, null, request);
    }

    @PUT
    @Path("/{entity}/{version}")
    public String insert(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new InsertCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/save/{entity}")
    public String save(@PathParam("entity") String entity, String request) {
        return save(entity, null, request);
    }

    @POST
    @Path("/save/{entity}/{version}")
    public String save(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new SaveCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/update/{entity}")
    public String update(@PathParam("entity") String entity, String request) {
        return update(entity, null, request);
    }

    @POST
    @Path("/update/{entity}/{version}")
    public String update(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new UpdateCommand(null, entity, version, request).execute();
    }

    @POST
    @Path("/delete/{entity}")
    public String delete(@PathParam("entity") String entity, String request) {
        return delete(entity, null, request);
    }

    @POST
    @Path("/delete/{entity}/{version}")
    public String delete(@PathParam("entity") String entity, @PathParam("version") String version, String req) {
        return new DeleteCommand(null, entity, version, req).execute();
    }

    @POST
    @Path("/find/{entity}")
    public String find(@PathParam("entity") String entity, String request) {
        return find(entity, null, request);
    }

    @POST
    @Path("/find/{entity}/{version}")
    public String find(@PathParam("entity") String entity, @PathParam("version") String version, String request) {
        return new FindCommand(null, entity, version, request).execute();
    }
}
