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


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ReferenceField;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.CRUDOperationContext;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.DocRequest;
import com.redhat.lightblue.OperationStatus;

public class OperationContext extends CRUDOperationContext {

    private final Request request;
    private final Metadata md;
    private final Map<String, EntityMetadata> metadata = new HashMap<>();
    private final Operation operation;
    private OperationStatus status=OperationStatus.COMPLETE;

    /**
     * Construct operation context
     *
     * @param request The top-level request 
     * @param md Metadata manager
     * @param factory The factory to get validators and controllers
     * @param roles Roles of the current caller
     * @param docs The documents in the call. Can be null
     * @param operation The operation in progress
     */
    public OperationContext(Request request,
                            Metadata md,
                            Factory factory,
                            Set<String> roles,
                            List<JsonDoc> docs,
                            Operation operation) {
        super(request.getEntity().getEntity(),factory,roles,docs);
        this.request = request;
        this.md = md;
        this.operation=operation;
        initMetadata(request.getEntity().getEntity(),request.getEntity().getVersion());
    }

    /**
     * Constructs an operation context
     *
     * @param req The request
     * @param md Metsdata manager
     * @param factory The factory to get validators and controllers
     * @param op The operation in progress
     */
    public static OperationContext getInstance(Request req,Metadata md,Factory factory,Operation op) {
        String[] callerRoles=req.getClientId()==null?null:req.getClientId().getCallerRoles();
        Set<String> roles=new HashSet<String>();
        if(callerRoles!=null) {
            for(String x:callerRoles) {
                roles.add(x);
            }
        }
        return new OperationContext(req,md,factory,roles,req instanceof DocRequest?
                                    JsonDoc.docList(((DocRequest)req).getEntityData()):null,op);
    }

    /**
     * Returns the top level entity name
     */
    public String getTopLevelEntityName() {
        return request.getEntity().getEntity();
    }

    /**
     * Returns the top level entity version
     */
    public String getTopLevelEntityVersion() {
        return request.getEntity().getVersion();
    }

    /**
     * Returns the top level entity metadata
     */
    public EntityMetadata getTopLevelEntityMD() {
        return getEntityMetadata(getTopLevelEntityName());
    }

    /**
     * Returns the metadata manager
     */
    public Metadata getMd() {
        return md;
    }

    /**
     * Returns the current operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Returns the top level request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the entitye metadata with the version used in this call
     */
    @Override
    public EntityMetadata getEntityMetadata(String entityName) {
        return metadata.get(entityName);
    }

    /**
     * The operation status
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * The operation status
     */
    public void setStatus(OperationStatus status) {
        this.status=status;
    }

    private void initMetadata(String name,String version) {
        EntityMetadata x = metadata.get(name);
        if (x != null) {
            if (!x.getVersion().getValue().equals(version)) {
                throw new IllegalArgumentException("Metadata " + name + " appears with both " + version
                        + " and " + x.getVersion().getValue());
            }
        } else {
            x=md.getEntityMetadata(name,version);
            if(x==null)
                throw new IllegalArgumentException("Unknown entity:"+name+":"+version);
            metadata.put(x.getName(), x);
            FieldCursor c = x.getFieldCursor();
            while (c.next()) {
                FieldTreeNode node = c.getCurrentNode();
                if (node instanceof ReferenceField) {
                    String refName = ((ReferenceField) node).getEntityName();
                    String refVersion = ((ReferenceField) node).getVersionValue();
                    initMetadata(refName,refVersion);
                }
            }
        }
    }
}
