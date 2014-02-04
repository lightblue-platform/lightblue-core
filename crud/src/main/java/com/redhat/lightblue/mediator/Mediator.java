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
package com.redhat.lightblue.mediator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CRUDDeleteResponse;

import com.redhat.lightblue.InsertionRequest;
import com.redhat.lightblue.SaveRequest;
import com.redhat.lightblue.UpdateRequest;
import com.redhat.lightblue.FindRequest;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.DeleteRequest;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.DataError;

/**
 * The mediator looks at a request, performs basic validation, and passes the operation to one or more of the
 * controllers based on the request attributes.
 */
public class Mediator {

    public static final String ERR_CRUD = "CRUD";

    public static final String CRUD_MSG_PREFIX = "CRUD controller={}";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Mediator.class);

    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private final Metadata metadata;
    private final Factory factory;

    public Mediator(Metadata md,
                    Factory factory) {
        this.metadata = md;
        this.factory = factory;
    }

    /**
     * Inserts data
     *
     * @param req Insertion request
     *
     * Mediator performs constraint validation, and passes documents that pass the validation to the CRUD implementation
     * for that entity.
     */
    public Response insert(InsertionRequest req) {
        LOGGER.debug("insert {}", req.getEntity());
        Error.push("insert(" + req.getEntity().toString() + ")");
        Response response = new Response();
        try {
            OperationContext ctx = getOperationContext(req, response, req.getEntityData(), Operation.INSERT);
            EntityMetadata md = ctx.getEntityMetadata(req.getEntity().getEntity());
            updatePredefinedFields(ctx.getDocs());
            List<JsonDoc> docsWithoutErrors = runBulkConstraintValidation(md, ctx);
            if (response.getErrors().isEmpty() && !docsWithoutErrors.isEmpty()) {
                CRUDController controller = factory.getCRUDController(md);
                LOGGER.debug(CRUD_MSG_PREFIX, controller.getClass().getName());
                CRUDInsertionResponse insertionResponse = controller.insert(ctx, docsWithoutErrors, req.getReturnFields());
                response.setEntityData(toJsonDocList(insertionResponse.getDocuments(), NODE_FACTORY));
                mergeDataErrors(insertionResponse.getDataErrors(), response);
                mergeErrors(insertionResponse.getErrors(), response);
                response.setModifiedCount(insertionResponse.getDocuments().size());
                if (insertionResponse.getDocuments().size() == ctx.getDocs().size()) {
                    response.setStatus(OperationStatus.COMPLETE);
                } else if (!insertionResponse.getDocuments().isEmpty()) {
                    response.setStatus(OperationStatus.PARTIAL);
                } else {
                    response.setStatus(OperationStatus.ERROR);
                }
            } else {
                response.setStatus(OperationStatus.ERROR);
            }
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD, e.toString()));
        } finally {
            Error.pop();
        }

        return response;
    }

    /**
     * Saves data. Documents in the DB that match the ID of the documents in the request are rewritten. If a document
     * does not exist in the DB and upsert=true, the document is inserted.
     *
     * @param req Save request
     *
     * Mediator performs constraint validation, and passes documents that pass the validation to the CRUD implementation
     * for that entity.
     */
    public Response save(SaveRequest req) {
        LOGGER.debug("save {}", req.getEntity());
        Error.push("save(" + req.getEntity().toString() + ")");
        Response response = new Response();
        try {
            OperationContext ctx = getOperationContext(req, response, req.getEntityData(), Operation.SAVE);
            EntityMetadata md = ctx.getEntityMetadata(req.getEntity().getEntity());
            updatePredefinedFields(ctx.getDocs());
            List<JsonDoc> docsWithoutErrors = runBulkConstraintValidation(md, ctx);
            if (response.getErrors().isEmpty() && !docsWithoutErrors.isEmpty()) {
                CRUDController controller = factory.getCRUDController(md);
                LOGGER.debug(CRUD_MSG_PREFIX, controller.getClass().getName());
                CRUDSaveResponse saveResponse = controller.save(ctx, docsWithoutErrors, req.isUpsert(), req.getReturnFields());
                response.setEntityData(toJsonDocList(saveResponse.getDocuments(), NODE_FACTORY));
                mergeDataErrors(saveResponse.getDataErrors(), response);
                mergeErrors(saveResponse.getErrors(), response);
                response.setModifiedCount(saveResponse.getDocuments().size());
                if (saveResponse.getDocuments().size() == ctx.getDocs().size()) {
                    response.setStatus(OperationStatus.COMPLETE);
                } else if (!saveResponse.getDocuments().isEmpty()) {
                    response.setStatus(OperationStatus.PARTIAL);
                } else {
                    response.setStatus(OperationStatus.ERROR);
                }
            }
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD, e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    /**
     * Updates documents that match the given search criteria
     *
     * @param req Update request
     *
     * All documents matching the search criteria are updated using the update expression given in the request. Then,
     * the updated document is projected and returneed in the response.
     */
    public Response update(UpdateRequest req) {
        LOGGER.debug("update {}", req.getEntity());
        Error.push("update(" + req.getEntity().toString() + ")");
        Response response = new Response();
        try {
            OperationContext ctx = getOperationContext(req, response, null, Operation.UPDATE);
            EntityMetadata md = ctx.getEntityMetadata(req.getEntity().getEntity());
            CRUDController controller = factory.getCRUDController(md);
            LOGGER.debug(CRUD_MSG_PREFIX, controller.getClass().getName());
            CRUDUpdateResponse updateResponse = controller.update(ctx,
                    req.getEntity().getEntity(),
                    req.getQuery(),
                    req.getUpdateExpression(),
                    req.getReturnFields());
            LOGGER.debug("# Updated", updateResponse.getNumUpdated());
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD, e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    public Response delete(DeleteRequest req) {
        LOGGER.debug("delete {}", req.getEntity());
        Error.push("delete(" + req.getEntity().toString() + ")");
        Response response = new Response();
        try {
            OperationContext ctx = getOperationContext(req, response, null, Operation.DELETE);
            EntityMetadata md = ctx.getEntityMetadata(req.getEntity().getEntity());
            CRUDController controller = factory.getCRUDController(md);
            LOGGER.debug(CRUD_MSG_PREFIX, controller.getClass().getName());
            CRUDDeleteResponse result = controller.delete(ctx,
                                                          req.getEntity().getEntity(),
                                                          req.getQuery());
            response.setModifiedCount(result.getNumDeleted());
            List<Error> errors=result.getErrors();
            if(errors!=null&&!errors.isEmpty()) {
                response.getErrors().addAll(result.getErrors());
                response.setStatus(OperationStatus.ERROR);
            } else {
                response.setStatus(OperationStatus.COMPLETE);
            }
        } catch (Error e) {
            response.getErrors().add(e);
            response.setStatus(OperationStatus.ERROR);
       } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD, e.toString()));
            response.setStatus(OperationStatus.ERROR);
       } finally {
            Error.pop();
        }
        return response;
    }

    /**
     * Finds documents
     *
     * @param req Find request
     *
     * The implementation passes the request to the back-end.
     */
    public Response find(FindRequest req) {
        LOGGER.debug("find {}", req.getEntity());
        Error.push("find(" + req.getEntity().toString() + ")");
        Response response = new Response();
        response.setStatus(OperationStatus.ERROR);
        try {
            OperationContext ctx = getOperationContext(req, response, null, Operation.FIND);
            EntityMetadata md = ctx.getEntityMetadata(req.getEntity().getEntity());
            CRUDController controller = factory.getCRUDController(md);
            LOGGER.debug(CRUD_MSG_PREFIX, controller.getClass().getName());
            CRUDFindResponse result = controller.find(ctx,
                    req.getEntity().getEntity(),
                    req.getQuery(),
                    req.getProjection(),
                    req.getSort(),
                    req.getFrom(),
                    req.getTo());
            response.setStatus(OperationStatus.COMPLETE);
            response.setMatchCount(result.getSize());
            response.setEntityData(toJsonDocList(result.getResults(), NODE_FACTORY));
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD, e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    /**
     * Runs constraint violation and returns the documents that has no constraint errors
     */
    private List<JsonDoc> runBulkConstraintValidation(EntityMetadata md,
                                                      OperationContext ctx) {
        LOGGER.debug("Bulk constraint validation");
        ConstraintValidator constraintValidator = factory.getConstraintValidator(md);
        constraintValidator.validateDocs(ctx.getDocs());
        Map<JsonDoc, List<Error>> docErrors = constraintValidator.getDocErrors();
        List<JsonDoc> docsWithoutError = new ArrayList<>(ctx.getDocs().size());
        for (JsonDoc doc : ctx.getDocs()) {
            List<Error> err = docErrors.get(doc);
            if (err != null && !err.isEmpty()) {
                addDataErrors(doc, err, ctx.getResponse().getDataErrors());
            } else {
                docsWithoutError.add(doc);
            }
        }
        List<Error> errors = constraintValidator.getErrors();
        if (errors != null && !errors.isEmpty()) {
            ctx.getResponse().getErrors().addAll(errors);
        }
        LOGGER.debug("There are {} documents to process after constraint validation", docsWithoutError.size());
        return docsWithoutError;
    }

    private void updatePredefinedFields(List<JsonDoc> docs) {
        for(JsonDoc doc:docs) {
            PredefinedFields.updateArraySizes(NODE_FACTORY,doc);
        }
    }

    private void mergeErrors(List<Error> errors,
                             Response resp) {
        if (errors != null) {
            resp.getErrors().addAll(errors);
        }
    }

    private void mergeDataErrors(List<DataError> dataErrors,
                                 Response resp) {
        if (dataErrors != null && !dataErrors.isEmpty()) {
            for (DataError x : dataErrors) {
                DataError err = DataError.findErrorForDoc(resp.getDataErrors(), x.getEntityData());
                if (err != null) {
                    err.getErrors().addAll(x.getErrors());
                } else {
                    resp.getDataErrors().add(x);
                }
            }
        }
    }

    private void addDataErrors(JsonDoc doc,
                               List<Error> errors,
                               List<DataError> dest) {
        if (errors != null && !errors.isEmpty()) {
            DataError err = DataError.findErrorForDoc(dest, doc.getRoot());
            if (err == null) {
                err = new DataError(doc.getRoot(), errors);
                dest.add(err);
            } else {
                if (err.getErrors() == null) {
                    err.setErrors(new ArrayList<Error>());
                }
                err.getErrors().addAll(errors);
            }
        }
    }

    private List<JsonDoc> fromJsonDocList(JsonNode data) {
        ArrayList<JsonDoc> docs = null;
        if (data != null) {
            if (data instanceof ArrayNode) {
                docs = new ArrayList<>(((ArrayNode) data).size());
                for (Iterator<JsonNode> itr = ((ArrayNode) data).elements();
                        itr.hasNext();) {
                    docs.add(new JsonDoc(itr.next()));
                }
            } else if (data instanceof ObjectNode) {
                docs = new ArrayList<>(1);
                docs.add(new JsonDoc(data));
            }
        }
        return docs;
    }

    private JsonNode toJsonDocList(List<JsonDoc> docs, JsonNodeFactory nodeFactory) {
        if (docs == null) {
            return null;
        } else if (docs.isEmpty()) {
            return nodeFactory.arrayNode();
        } else if (docs.size() == 1) {
            return docs.get(0).getRoot();
        } else {
            ArrayNode node = nodeFactory.arrayNode();
            for (JsonDoc doc : docs) {
                node.add(doc.getRoot());
            }
            return node;
        }
    }

    private OperationContext getOperationContext(Request req,
                                                 Response resp,
                                                 JsonNode entityData,
                                                 Operation op) {
        LOGGER.debug("getOperationContext start");
        OperationContext ctx
                = new OperationContext(req, resp,
                        metadata.
                        getEntityMetadata(req.getEntity().getEntity(),
                                req.getEntity().getVersion()),
                        metadata,
                        factory);
        ctx.setOperation(op);
        LOGGER.debug("metadata retrieved for {}", req.getEntity());
        ctx.setDocs(fromJsonDocList(entityData));
        if (ctx.getDocs() != null) {
            LOGGER.debug("There are {} docs in request", ctx.getDocs().size());
        }
        LOGGER.debug("getOperationContext return");
        return ctx;
    }
}
