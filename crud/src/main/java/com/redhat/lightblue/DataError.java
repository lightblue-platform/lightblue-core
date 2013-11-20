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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

public class DataError extends JsonObject {

    private JsonNode entityData;
    private List<Error> errors;

    public JsonNode getEntityData() {
        return entityData;
    }

    public void setEntityData(JsonNode node) {
        entityData=node;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> e) {
        errors=e;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode();
        if(entityData!=null)
            node.set("data",entityData);
        if(errors!=null&&!errors.isEmpty()) {
            ArrayNode arr=factory.arrayNode();
            node.set("errors",arr);
            for(Error x:errors)
                arr.add(x.toJson());
        }
        return node;
    }
    
    public static DataError fromJson(ObjectNode node) {
        DataError error=new DataError();
        JsonNode x=node.get("data");
        if(x!=null)
            error.entityData=x;
        x=node.get("errors");
        if(x!=null&&x instanceof ArrayNode) {
            error.errors=new ArrayList<Error>();
            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                itr.hasNext();)
                error.errors.add(Error.fromJson(itr.next()));
        }
        return error;
    }       
}
