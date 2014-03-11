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
package com.redhat.lightblue.hooks;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Hook;

import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.util.Error;

public class Hooks {

    private final HookResolver resolver;

    /**
     * Construct hooks with the given hook resolver
     */
    public Hooks(HookResolver r) {
        resolver=r;
    }


    // public void queueHooks(CRUDOperationContext ctx) {
    //     EntityMetadata md=ctx.getEntityMetadata(ctx.getEntityName());
    //     List<Hook> hooks=md.getHooks().getHooks();
    //     Map<String,CRUDHook> hookCache=new HashMap<String,CRUDHook>();
    //     for(Hook h:hooks) {
    //         Projector projector=null;
    //         for(DocCtx doc:ctx.getDocuments()) {
    //             if(!doc.hasErrors()) {
    //                 boolean queueThisDoc=false;
    //                 switch(doc.getOperationPerformed()) {
    //                 case INSERT: queueThisDoc=h.isInsert();break;
    //                 case UPDATE: queueThisDoc=h.isUpdate();break;
    //                 case DELETE: queueThisDoc=h.isDelete();break;
    //                 case FIND: queueThisDoc=h.isFind();break;
    //                 }
    //                 if(queueThisDoc) {
    //                     if(projector==null&&h.getProjection()!=null)
    //                         projector=Projector.getInstance(h.getProjection(),md);
    //                     JsonDoc inputDoc;
    //                     JsonDoc outputDoc;
    //                     switch(doc.getOperationPerformed()) {
    //                     case INSERT: 
    //                         if(projector!=null)
    //                             inputDoc=outputDoc=projector.project(doc,nodeFactory);
    //                         else
    //                             inputDoc=outputDoc=doc;
    //                         break;
    //                     case UPDATE:
    //                         if(projector!=null) {
    //                             inputDoc=projector.project(doc,nodeFactory);
    //                             outputDoc=projector.project(doc.getOutputDocument(),nodeFactory);
    //                         }


    //                     String hookName=h.getName();
    //                     CRUDHook ch=hookCache.get(hookName);
    //                     if(ch==null) {
    //                         ch=resolver.getHook(hookName);
    //                         if(ch==null)
    //                             throw Error.get(CrudConstants.ERR_INVALID_HOOK,hookName);
    //                         hookCache.put(hookName,ch);
    //                     }
    //                     queue(doc,h.getProjection(),h.getConfiguration(),ch);
    //                 }
    //             }
    //         }
    //     }
    // }
    
    // private void queue(DocCtx doc,
    //                    Projection projection,
    //                    HookConfiguration cfg,
    //                    CRUDHook hook) {
        
    // }
}
