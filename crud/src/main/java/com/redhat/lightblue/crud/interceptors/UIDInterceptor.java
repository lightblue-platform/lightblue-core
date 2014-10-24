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
package com.redhat.lightblue.crud.interceptors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.mediator.OperationContext;

import com.redhat.lightblue.metadata.UIDFields;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.interceptor.CRUDDocInterceptor;
import com.redhat.lightblue.interceptor.MediatorInterceptor;
import com.redhat.lightblue.interceptor.InterceptorManager;
import com.redhat.lightblue.interceptor.InterceptPoint;

public class UIDInterceptor implements CRUDDocInterceptor, MediatorInterceptor {

    public static final int UID_SEQ = 500;

    public void register(InterceptorManager mgr) {
        mgr.registerInterceptor(UID_SEQ, this, InterceptPoint.PRE_CRUD_INSERT_DOC,
                                InterceptPoint.PRE_MEDIATOR_SAVE,
                InterceptPoint.PRE_CRUD_UPDATE_DOC,
                InterceptPoint.PRE_MEDIATOR_INSERT);
    }

    @Override
    public void run(OperationContext ctx) {
        JsonNodeFactory nodeFactory = ctx.getFactory().getNodeFactory();
        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        for (DocCtx doc : ctx.getDocuments()) {
            UIDFields.initializeUIDFields(nodeFactory, md, doc);
        }
    }

    @Override
    public void run(CRUDOperationContext ctx, DocCtx doc) {
        UIDFields.initializeUIDFields(ctx.getFactory().getNodeFactory(), ctx.getEntityMetadata(ctx.getEntityName()), doc);
    }
}
