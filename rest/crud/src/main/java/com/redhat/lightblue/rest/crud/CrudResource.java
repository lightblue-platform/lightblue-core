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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.CrudManager;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

/**
 *
 * @author nmalik
 * @author bserdar
 */
@Path("/") // metadata/ prefix is the application context
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrudResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrudResource.class);

    @PUT @Path("/{entity}")
    public String insert(@PathParam("entity") String entity, String req) {
        return insert(entity,null,req);
    }

    @PUT @Path("/{entity}/{version}")
    public String insert(@PathParam("entity") String entity,@PathParam("version") String version,String req) {
        LOGGER.debug("insert: {} {}",entity,version);
        Error.reset();
        Error.push("insert");
        Error.push(entity);
        try {
            InsertionRequest ireq=InsertionRequest.fromJson((ObjectNode)JsonUtils.json(req));
            validateReq(ireq,entity,version);
            Response r=CrudManager.getMediator().insert(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("insert failure: {}",e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("insert failure: {}",e);
            return Error.get(RestCrudConstants.ERR_REST_INSERT,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @POST @Path("/save/{entity}")
    public String save(@PathParam("entity") String entity, String req) {
        return save(entity,null,req);
    }

    @POST @Path("/save/{entity}/{version}")
    public String save(@PathParam("entity") String entity,@PathParam("version") String version, String req) {
        LOGGER.debug("save: {} {}",entity,version);
        Error.reset();
        Error.push("save");
        Error.push(entity);
        try {
            SaveRequest ireq=SaveRequest.fromJson((ObjectNode)JsonUtils.json(req));
            validateReq(ireq,entity,version);
            Response r=CrudManager.getMediator().save(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("save failure: {}",e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("save failure: {}",e);
            return Error.get(RestCrudConstants.ERR_REST_SAVE,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @POST @Path("/update/{entity}")
    public String update(@PathParam("entity") String entity, String req) {
        return update(entity,null,req);
    }

    @POST @Path("/update/{entity}/{version}")
    public String update(@PathParam("entity") String entity,@PathParam("version") String version, String req) {
        LOGGER.debug("update: {} {}",entity,version);
        Error.reset();
        Error.push("update");
        Error.push(entity);
        try {
            UpdateRequest ireq=UpdateRequest.fromJson((ObjectNode)JsonUtils.json(req));
            validateReq(ireq,entity,version);
            Response r=CrudManager.getMediator().update(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("update failure: {}",e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("update failure: {}",e);
            return Error.get(RestCrudConstants.ERR_REST_UPDATE,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    @POST @Path("/delete/{entity}")
    public String delete(@PathParam("entity") String entity, String req) {
        return delete(entity,null,req);
    }

    @POST @Path("/delete/{entity}/{version}")
    public String delete(@PathParam("entity") String entity,@PathParam("version") String version, String req) {
        LOGGER.debug("delete: {} {}",entity,version);
        Error.reset();
        Error.push("delete");
        Error.push(entity);
        try {
            DeleteRequest ireq=DeleteRequest.fromJson((ObjectNode)JsonUtils.json(req));
            validateReq(ireq,entity,version);
            Response r=CrudManager.getMediator().delete(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("delete failure: {}",e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("delete failure: {}",e);
            return Error.get(RestCrudConstants.ERR_REST_DELETE,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    
    @POST @Path("/find/{entity}")
    public String find(@PathParam("entity") String entity, String req) {
        return find(entity,null,req);
    }

    @POST @Path("/find/{entity}/{version}")
    public String find(@PathParam("entity") String entity,@PathParam("version") String version, String req) {
        LOGGER.debug("find: {} {}",entity,version);
        Error.reset();
        Error.push("find");
        Error.push(entity);
        try {
            FindRequest ireq=FindRequest.fromJson((ObjectNode)JsonUtils.json(req));
            validateReq(ireq,entity,version);
            Response r=CrudManager.getMediator().find(ireq);
            return r.toJson().toString();
        } catch (Error e) {
            LOGGER.error("find failure: {}",e);
            return e.toString();
        } catch (Exception e) {
            LOGGER.error("find failure: {}",e);
            return Error.get(RestCrudConstants.ERR_REST_FIND,e.toString()).toString();
        } finally {
            Error.reset();
        }
    }

    private void validateReq(Request req,String entity,String version) {
        // If entity and/or version is not set in the request, this
        // code below sets it from the uri
        if(req.getEntityVersion()==null)  {
            req.setEntityVersion(new EntityVersion());
        }
        if(req.getEntityVersion().getEntity()==null) {
            req.getEntityVersion().setEntity(entity);
        }
        if(req.getEntityVersion().getVersion()==null) {
            req.getEntityVersion().setVersion(version);
        }
        if(!req.getEntityVersion().getEntity().equals(entity))
            throw Error.get(RestCrudConstants.ERR_NO_ENTITY_MATCH,entity);
        if(!req.getEntityVersion().getVersion().equals(version))
            throw Error.get(RestCrudConstants.ERR_NO_VERSION_MATCH,version);
    }
}
