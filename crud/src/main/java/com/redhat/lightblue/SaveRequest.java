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

public class SaveRequest extends Request {

    private JsonNode entityData;
    private Projection returnFields;
    private boolean upsert;

    public JsonNode getEntityData() {
        return entityData;
    }

    public void setEntityData(JsonNode data) {
        this.entityData=data;
    }

    public Projection getReturnFields() {
        return returnFields;
    }

    public void setReturnFields(Projection p) {
        returnFields=p;
    }

    public boolean isUpsert() {
        return upsert;
    }

    public void setUpsert(boolean b) {
        upsert=b;
    }

    public JsonNode toJson() {
        ObjectNode node=(ObjectNode)super.toJson();
        if(entityData!=null)
            node.set("data",entityData);
        if(returnFields!=null)
            node.set("returning",returnFields.toJson());
        node.put("upsert",upsert);
        return node;
    }

    public static SaveRequest fromJson(ObjectNode node) {
        SaveRequest req=new SaveRequest();
        req.parse(node);
        req.entityData=node.get("data");
        JsonNode x=node.get("returning");
        if(x!=null)
            req.returnFields=Projection.fromJson(x);
        x=node.get("upsert");
        if(x!=null)
            req.upsert=x.asBoolean();
        return req;
    }
}
