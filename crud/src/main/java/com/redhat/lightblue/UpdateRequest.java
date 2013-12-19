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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UpdateExpression;

/**
 * Request to update documents based on a query
 */
public class UpdateRequest extends Request {

    private QueryExpression query;
    private UpdateExpression updateExpression;
    private Projection returnFields;

    /**
     * The fields to return from the updated documents
     */
    public Projection getReturnFields() {
        return returnFields;
    }

    /**
     * The fields to return from the updated documents
     */
    public void setReturnFields(Projection p) {
        returnFields=p;
    }

    /**
     * The expression specifying how to modify the documents
     */
    public UpdateExpression getUpdateExpression() {
        return updateExpression;
    }

    /**
     * The expression specifying how to modify the documents
     */
    public void setUpdateExpression(UpdateExpression x) {
        updateExpression=x;
    }

    /**
     * The query specifying which documents to be updated
     */
    public QueryExpression getQuery() {
        return query;
    }

    /**
     * The query specifying which documents to be updated
     */
    public void setQuery(QueryExpression q) {
        query=q;
    }  
      
    /**
     * Returns a json representation of this
     */
    public JsonNode toJson() {
        ObjectNode node=(ObjectNode)super.toJson();
        if(query!=null)
            node.set("query",query.toJson());
        if(updateExpression!=null)
            node.set("update",updateExpression.toJson());
        if(returnFields!=null)
            node.set("returning",returnFields.toJson());
        return node;
    }

    /**
     * Parses an update request from a Json object
     */
    public static UpdateRequest fromJson(ObjectNode node) {
        UpdateRequest req=new UpdateRequest();
        req.parse(node);
        JsonNode x=node.get("query");
        if(x!=null)
            req.query=QueryExpression.fromJson(x);
        x=node.get("update");
        if(x!=null)
            req.updateExpression=UpdateExpression.fromJson(x);
        x=node.get("returning");
        if(x!=null)
            req.returnFields=Projection.fromJson(x);
        return req;
    }
}
