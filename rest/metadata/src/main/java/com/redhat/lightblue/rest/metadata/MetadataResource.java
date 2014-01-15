package com.redhat.lightblue.rest.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.util.metrics.ServiceMetrics;
import com.redhat.lightblue.util.metrics.ServoServiceMetrics;
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
@Path("/metadata")
public class MetadataResource {

    private static final ServiceMetrics metrics = ServoServiceMetrics.getInstance();

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
            EntityMetadata em = MetadataManager.getMetadata().getEntityMetadata(entityName, entityVersion);

            // convert to json and return
            return MetadataManager.getJSONParser().convert(em).toString();
        } catch (Error e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }

    @GET
    @Path("/names")
    public String getEntityNames() {
        try {
            // get the data
            String[] names = MetadataManager.getMetadata().getEntityNames();

            // convert to json and return
            return new Gson().toJson(names);
        } catch (Error e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }

    @GET
    @Path("/{entity}/versions")
    public String getMetadataVersions(@PathParam("entity") String entityName) {
        try {
            if (entityName == null) {
                throw Error.get(ERR_REST_ERROR, "Entity name not provided");
            }

            // get the data
            Version[] versions = MetadataManager.getMetadata().getEntityVersions(entityName);

            // convert to json and return
            // bit of a hack, will wrap each individual converted version with an array
            StringBuilder buff = new StringBuilder("{\"versions\":[");
            Iterator<Version> itr = Arrays.asList(versions).iterator();
            while (itr.hasNext()) {
                JsonNode jn = MetadataManager.getJSONParser().convert(itr.next());
                buff.append("({").append(jn.toString()).append("}");
                if (itr.hasNext()) {
                    buff.append(",");
                }
            }
            buff.append("]}");
            return buff.toString();
        } catch (Error e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }

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
    public String createMetadata(@PathParam("entity") String entityName, @PathParam("version") String entityVersion, String metadata) {
        try {
            if (entityName == null) {
                throw Error.get(ERR_REST_ERROR, "Entity name not provided");
            }
            if (entityVersion == null) {
                throw Error.get(ERR_REST_ERROR, "Entity version not provided");
            }

            // convert to object
            EntityMetadata em = MetadataManager.getJSONParser().parseEntityMetadata(JsonUtils.json(metadata));

            // verify entity name and version
            if (!entityName.equals(em.getName())) {
                throw Error.get(ERR_REST_ERROR, "Entity name in query does not match metadata");
            }
            if (!entityVersion.equals(em.getVersion().getValue())) {
                throw Error.get(ERR_REST_ERROR, "Entity version in query does not match metadata");
            }

            // execute creation
            MetadataManager.getMetadata().createNewMetadata(em);

            // if successful fetch the metadata back out of the database and return it (simply reuse the existing rest api to do it)
            return getMetadata(entityName, entityVersion);
        } catch (Error e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }

    /**
     * Body is optional comment.
     *
     * @param entityName
     * @param entityVersion
     * @param status
     * @param comment
     * @return the metadata updated from this request
     */
    @PUT
    @Path("/{entity}/{version}/{status}")
    public String updateMetadataStatus(@PathParam("entity") String entityName, @PathParam("version") String entityVersion, @PathParam("status") String status, String comment) {
        try {
            if (entityName == null) {
                throw Error.get(ERR_REST_ERROR, "Entity name not provided");
            }
            if (entityVersion == null) {
                throw Error.get(ERR_REST_ERROR, "Entity version not provided");
            }
            if (status == null) {
                throw Error.get(ERR_REST_ERROR, "Entity status not provided");
            }

            MetadataStatus ms = MetadataStatus.valueOf(status);

            MetadataManager.getMetadata().setMetadataStatus(entityName, entityVersion, ms, comment);

            // if successful fetch the metadata back out of the database and return it (simply reuse the existing rest api to do it)
            return getMetadata(entityName, entityVersion);
        } catch (Error e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return e.toJson().toString();
        } catch (Exception e) {
            Logger.getLogger(MetadataResource.class.getName()).log(Level.SEVERE, null, e);
            return Error.get(ERR_REST_ERROR).toJson().toString();
        }
    }
}
