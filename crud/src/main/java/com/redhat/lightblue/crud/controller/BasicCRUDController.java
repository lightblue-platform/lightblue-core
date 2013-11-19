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
package com.redhat.lightblue.crud.controller;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.Metadata;

import com.redhat.lightblue.crud.CRUDApi;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.Response;
import com.redhat.lightblue.crud.Request;
import com.redhat.lightblue.crud.DeleteRequest;

public class BasicCRUDController implements CRUDApi {

    private static final Logger logger=LoggerFactory.getLogger(BasicCRUDController.class);

    private final Metadata metadata;

    public BasicCRUDController(Metadata md) {
        this.metadata=md;
    }

    public Response insert(InsertionRequest req) {
        logger.debug("insert {}",req.getEntity());
        Error.push("insert("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,req.getEntityData());
            
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
            OperationContext ctx=getOperationContext(req,response,req.getEntityData());

            
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
            OperationContext ctx=getOperationContext(req,response,null);

            
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
            OperationContext ctx=getOperationContext(req,response,null);

            
        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    public Response find(FindRequest req) {
        logger.debug("find {}",req.getEntity());
        Error.push("find("+req.getEntity().toString()+")");
        Response response=new Response();
        try {
            OperationContext ctx=getOperationContext(req,response,null);

        } catch (Error e) {
            response.getErrors().add(e);
        } catch (Exception e) {
            response.getErrors().add(Error.get(ERR_CRUD,e.toString()));
        } finally {
            Error.pop();
        }
        return response;
    }

    private OperationContext getOperationContext(Request req,
                                                 Response resp,
                                                 JsonNode entityData) {
        logger.debug("getOperationContext start");
        OperationContext ctx=
            new OperationContext(req,resp,
                                 metadata.
                                 getEntityMetadata(req.getEntity().getEntity(),
                                                   req.getEntity().getVersion()));
        logger.debug("metadata retrieved for {}",req.getEntity());
        if(entityData!=null) {
            ArrayList<JsonDoc> docs;
            if(entityData instanceof ArrayNode) {
                docs=new ArrayList<JsonDoc>(((ArrayNode)entityData).size());
                for(Iterator<JsonNode> itr=((ArrayNode)entityData).elements();
                    itr.hasNext();)
                    docs.add(new JsonDoc(itr.next()));
            } else if(entityData instanceof ObjectNode) {
                docs=new ArrayList<JsonDoc>(1);
                docs.add(new JsonDoc(entityData));
            }  else
                docs=null;
            ctx.setDocs(docs);
        }
        logger.debug("getOperationContext return");
        return ctx;
    }
}
