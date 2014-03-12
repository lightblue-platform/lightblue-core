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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.DeleteRequest;
import com.redhat.lightblue.FindRequest;
import com.redhat.lightblue.InsertionRequest;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.SaveRequest;
import com.redhat.lightblue.UpdateRequest;
import com.redhat.lightblue.crud.CrudManager;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * Simple service to test out NewRelic custom metrics.
 *
 * @author nmalik
 */
@Path("/crud")
public class CrudResource {

    @GET
    @Path("/find")
    public String find(String data) {
        try {
            Response r = CrudManager.getMediator().find(FindRequest.fromJson((ObjectNode) JsonUtils.json(data)));
            return r.toJson().toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(RestCrudConstants.ERR_REST_FIND).toJson().toString();
        }
    }

    @PUT
    @Path("/insert")
    public String insert(String data) {
        try {
            Response r = CrudManager.getMediator().insert(InsertionRequest.fromJson((ObjectNode) JsonUtils.json(data)));
            return r.toJson().toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(RestCrudConstants.ERR_REST_INSERT).toJson().toString();
        }
    }

    @POST
    @Path("/update")
    public String update(String data) {
        try {
            Response r = CrudManager.getMediator().update(UpdateRequest.fromJson((ObjectNode) JsonUtils.json(data)));
            return r.toJson().toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(RestCrudConstants.ERR_REST_UPDATE).toJson().toString();
        }
    }

    @PUT
    @Path("/save")
    public String save(String data) {
        try {
            Response r = CrudManager.getMediator().save(SaveRequest.fromJson((ObjectNode) JsonUtils.json(data)));
            return r.toJson().toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(RestCrudConstants.ERR_REST_SAVE).toJson().toString();
        }
    }

    @DELETE
    @Path("/delete")
    public String delete(String data) {
        try {
            Response r = CrudManager.getMediator().delete(DeleteRequest.fromJson((ObjectNode) JsonUtils.json(data)));
            return r.toJson().toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(RestCrudConstants.ERR_REST_DELETE).toJson().toString();
        }
    }
}
