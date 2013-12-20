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

import com.redhat.lightblue.controller.Factory;
import com.redhat.lightblue.controller.ConstraintValidator;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;

import com.redhat.lightblue.InsertionRequest;
import com.redhat.lightblue.SaveRequest;
import com.redhat.lightblue.UpdateRequest;
import com.redhat.lightblue.FindRequest;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.DeleteRequest;
import com.redhat.lightblue.OperationStatus;

/**
 * The mediator looks at a request, performs basic validation, and
 * passes the operation to one or more of the controllers based on the
 * request attributes.
 */
public class Mediator {

    public static final String ERR_CRUD="CRUD";

    private static final Logger logger=LoggerFactory.getLogger(Mediator.class);

    private static final JsonNodeFactory nodeFactory=JsonNodeFactory.withExactBigDecimals(true);

    private final Metadata metadata;
    private final Factory factory;

    public Mediator(Metadata md,
                    Factory factory) {
        this.metadata=md;
        this.factory=factory;
    }

    /**
     * Inserts data
     *
     * @param req Insertion request
     *
     * First, the mediator performs constraint validation. All the
     * constraints that can be validated at this level without the
     * knowledge of the underlying backed are validated. Then the
     * request is transferred to the CRUD controller for the entity.
     */
    public Response insert(InsertionRequest req) {
        logger.debug("insert {}",req.getEntity());
        Error.push("insert("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,req.getEntityData(),Operation.INSERT);
            
            EntityMetadata md=ctx.getEntityMetadata(req.getEntity().getEntity());
            logger.debug("Constraint validation");
            ConstraintValidator constraintValidator=factory.getConstraintValidator(md);
            constraintValidator.validateDocs(ctx.getDocs());
            if(!constraintValidator.hasErrors()) {
                logger.debug("Constraint validation has no errors");
                CRUDController controller=factory.getCRUDController(md);
                logger.debug("CRUD controller={}",controller.getClass().getName());
                CRUDInsertionResponse insertionResponse=controller.insert(ctx,ctx.getDocs(),req.getReturnFields());
                response.setEntityData(toJsonDocList(insertionResponse.getDocuments(),nodeFactory));
                if(insertionResponse.getDataErrors()!=null)
                    response.getDataErrors().addAll(insertionResponse.getDataErrors());
                if(insertionResponse.getErrors()!=null)
                    response.getErrors().addAll(insertionResponse.getErrors());
                response.setModifiedCount(insertionResponse.getDocuments().size());
                if(insertionResponse.getDocuments().size()==ctx.getDocs().size())
                    response.setStatus(OperationStatus.COMPLETE);
                else if(!insertionResponse.getDocuments().isEmpty())
                    response.setStatus(OperationStatus.PARTIAL);
                else
                    response.setStatus(OperationStatus.ERROR);
           }
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        }  finally {
            Error.pop();
        }
        return response;
    }

    public Response save(SaveRequest req) {
        logger.debug("save {}",req.getEntity());
        Error.push("save("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,req.getEntityData(),Operation.SAVE);

            ConstraintValidator constraintValidator=factory.getConstraintValidator(ctx.getEntityMetadata(req.getEntity().getEntity()));
            constraintValidator.validateDocs(ctx.getDocs());
            if(!constraintValidator.hasErrors()) {
            }
            
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    public Response update(UpdateRequest req) {
        logger.debug("update {}",req.getEntity());
        Error.push("update("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,null,Operation.UPDATE);

            
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    public Response delete(DeleteRequest req) {
        logger.debug("delete {}",req.getEntity());
        Error.push("delete("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,null,Operation.DELETE);

            
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
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
        logger.debug("find {}",req.getEntity());
        Error.push("find("+req.getEntity().toString()+")");
        Response response=new Response();
        response.setStatus(OperationStatus.ERROR);
        try {
            OperationContext ctx=getOperationContext(req,response,null,Operation.FIND);
            EntityMetadata md=ctx.getEntityMetadata(req.getEntity().getEntity());
            CRUDController controller=factory.getCRUDController(md);
            logger.debug("CRUD controller={}",controller.getClass().getName());
            CRUDFindResponse result=controller.find(ctx,
                                                    req.getEntity().getEntity(),
                                                    req.getQuery(),
                                                    req.getProjection(),
                                                    req.getSort(),
                                                    req.getFrom(),
                                                    req.getTo());
            response.setStatus(OperationStatus.COMPLETE);
            response.setMatchCount(result.getSize());
            response.setEntityData(toJsonDocList(result.getResults(),nodeFactory));
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    private List<JsonDoc> fromJsonDocList(JsonNode data) {
        ArrayList<JsonDoc> docs=null;
        if(data!=null) {
            if(data instanceof ArrayNode) {
                docs=new ArrayList<JsonDoc>(((ArrayNode)data).size());
                for(Iterator<JsonNode> itr=((ArrayNode)data).elements();
                    itr.hasNext();)
                    docs.add(new JsonDoc(itr.next()));
            } else if(data instanceof ObjectNode) {
                docs=new ArrayList<JsonDoc>(1);
                docs.add(new JsonDoc(data));
            } 
        }
        return docs;
    }

    private JsonNode toJsonDocList(List<JsonDoc> docs,JsonNodeFactory nodeFactory) {
        if(docs==null)
            return null;
        else if(docs.isEmpty())
            return nodeFactory.arrayNode();
        else if(docs.size()==1) 
            return docs.get(0).getRoot();
        else {
            ArrayNode node=nodeFactory.arrayNode();
            for(JsonDoc doc:docs)
                node.add(doc.getRoot());
            return node;
        }
    }

    private OperationContext getOperationContext(Request req,
                                                 Response resp,
                                                 JsonNode entityData,
                                                 Operation op) {
        logger.debug("getOperationContext start");
        OperationContext ctx=
            new OperationContext(req,resp,
                                 metadata.
                                 getEntityMetadata(req.getEntity().getEntity(),
                                                   req.getEntity().getVersion()),
                                 metadata,
                                 factory);
        ctx.setOperation(op);
        logger.debug("metadata retrieved for {}",req.getEntity());
        ctx.setDocs(fromJsonDocList(entityData));
        if(ctx.getDocs()!=null)
            logger.debug("There are {} docs in request",ctx.getDocs().size());
        logger.debug("getOperationContext return");
        return ctx;
    }
}
