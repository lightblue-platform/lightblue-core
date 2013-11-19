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

import java.io.Serializable;

import java.util.List;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.crud.Request;
import com.redhat.lightblue.crud.Response;

public class OperationContext implements Serializable {

    private static final long serialVersionUID=1;

    private final Request request;
    private final Response response;
    private final EntityMetadata metadata;
    private List<JsonDoc> docs;

    public OperationContext(Request request,
                            Response response,
                            EntityMetadata metadata) {
        this.request=request;
        this.response=response;
        this.metadata=metadata;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public EntityMetadata getMetadata() {
        return metadata;
    } 
    
    public List<JsonDoc> getDocs() {
        return docs;
    }

    public void setDocs(List<JsonDoc> docs) {
        this.docs=docs;
    }
}
