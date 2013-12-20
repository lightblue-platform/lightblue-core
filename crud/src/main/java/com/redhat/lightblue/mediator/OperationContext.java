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

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ReferenceField;

import com.redhat.lightblue.controller.Factory;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.Response;

public class OperationContext implements MetadataResolver, Serializable {

    private static final long serialVersionUID=1;

    private final Request request;
    private final Response response;
    private final Factory factory;
    private final Metadata md;
    private final Map<String,EntityMetadata> metadata=new HashMap<String,EntityMetadata>();
    private List<JsonDoc> docs;
    private Operation operation;

    public OperationContext(Request request,
                            Response response,
                            EntityMetadata metadata,
                            Metadata md,
                            Factory factory) {
        this.request=request;
        this.response=response;
        this.md=md;
        this.factory=factory;
        initMetadata(metadata);
    }

    public Metadata getMd() {
        return md;
    }

    public Factory getFactory() {
        return factory;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation op) {
        operation=op;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public EntityMetadata getEntityMetadata(String entityName) {
        return metadata.get(entityName);
    } 
    
    public List<JsonDoc> getDocs() {
        return docs;
    }

    public void setDocs(List<JsonDoc> docs) {
        this.docs=docs;
    }

    private void initMetadata(EntityMetadata m) {
        EntityMetadata x=metadata.get(m.getName());
        if(x!=null) {
            if(!x.getVersion().getValue().equals(m.getVersion().getValue()))
                throw new IllegalArgumentException("Metadata "+m.getName()+" appears with both "+m.getVersion().getValue()+
                                                   " and "+x.getVersion().getValue());
        } else {
            metadata.put(m.getName(),m);
            FieldCursor c=m.getFieldCursor();
            while(c.next()) {
                FieldTreeNode node=c.getCurrentNode();
                if(node instanceof ReferenceField) {
                    String name=((ReferenceField)node).getEntityName();
                    String version=((ReferenceField)node).getVersionValue();
                    EntityMetadata y=md.getEntityMetadata(name,version);
                    if(y==null)
                        throw new IllegalArgumentException("Cannot load metadata for "+name+":"+version);
                    initMetadata(y);
                }
            }
        }
    }
}
