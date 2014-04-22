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

import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.rest.crud.hystrix.*;
import com.redhat.lightblue.util.JsonUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

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

    @GET
    @Path("/find/{entity}") //?Q&P&S&from&to
    public String simpleFind(@PathParam("entity") String entity                                      , @QueryParam("Q") String q, @QueryParam("P") String p, @QueryParam("S") String s, @DefaultValue("0") @QueryParam("from") long from, @DefaultValue("-1") @QueryParam("to") long to ) throws IOException {
        return simpleFind(entity, null, q, p, s, from, to);
    }

    @GET
    @Path("/find/{entity}/{version}") //?Q&P&S&from&to
    public String simpleFind(@PathParam("entity") String entity, @PathParam("version") String version, @QueryParam("Q") String q, @QueryParam("P") String p, @QueryParam("S") String s, @DefaultValue("0") @QueryParam("from") long from, @DefaultValue("-1") @QueryParam("to") long to ) throws IOException {
        FindRequest findRequest = new FindRequest();
        findRequest.setEntityVersion(new EntityVersion(entity, version));
        findRequest.setQuery(QueryExpression.fromJson(JsonUtils.json(q)));
        findRequest.setProjection(Projection.fromJson(JsonUtils.json(p)));
        findRequest.setSort(s == null ? null : Sort.fromJson(JsonUtils.json(s)));
        findRequest.setFrom(from);
        findRequest.setTo(to);
        String request = findRequest.toString();

        return new FindCommand(null, entity, version, request).execute();
    }




}
