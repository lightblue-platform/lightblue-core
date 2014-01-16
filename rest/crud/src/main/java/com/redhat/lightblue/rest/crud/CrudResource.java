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
    public static final String ERR_REST_ERROR = "REST_ERROR";

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
            return Error.get(ERR_REST_ERROR).toJson().toString();
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
            return Error.get(ERR_REST_ERROR).toJson().toString();
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
            return Error.get(ERR_REST_ERROR).toJson().toString();
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
            return Error.get(ERR_REST_ERROR).toJson().toString();
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
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }
}
