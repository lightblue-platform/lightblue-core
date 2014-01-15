package com.redhat.lightblue.rest.crud;

import com.redhat.lightblue.crud.CrudManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Simple service to test out NewRelic custom metrics.
 *
 * @author nmalik
 */
@Path("/crud")
public class CrudResource {
    public static final String ERR_REST_ERROR = "REST_ERROR";

    @GET
    @Path("/{entity}/{version}")
    public String getMetadata(@PathParam("entity") String entityName, @PathParam("version") String entityVersion) {
        try {
            if (entityName == null) {
                throw Error.get(ERR_REST_ERROR, "Entity name not provided");
            }
            if (entityVersion == null) {
                throw Error.get(ERR_REST_ERROR, "Entity version not provided");
            }

            // get the data
            EntityMetadata em = CrudManager.getMetadata().getEntityMetadata(entityName, entityVersion);

            // convert to json and return
            return CrudManager.getJSONParser().convert(em).toString();
        } catch (Error e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(CrudResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }

}
