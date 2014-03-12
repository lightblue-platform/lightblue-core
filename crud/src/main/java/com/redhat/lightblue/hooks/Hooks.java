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
import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Hook;

import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

public class Hooks {

    private final HookResolver resolver;
    private final JsonNodeFactory factory;
    
    private final List<HookDocs> queuedHooks=new ArrayList<>();

    private static final class DocHooks {
        private final DocCtx doc;
        private final JsonDoc pre;
        private final JsonDoc post;
        private final Operation op;
        private final List<Hook> hooks;

        public DocHooks(DocCtx doc,List<Hook> hooks) {
            this.doc=doc;
            op=doc.getOperationPerformed();
            // Create a copy of the original version of the document, if non-null
            if(op==Operation.INSERT||op==Operation.FIND) {
                pre=null;
            } else {
                JsonDoc preDoc=doc.getOriginalDocument();
                if(preDoc!=null)
                    pre=preDoc.copy();
                else
                    pre=null;
            }
            // If original copy is the same instance as the document
            // copy, use the same pre value as post value otherwise,
            // copy the doc and use that as the post value
            // If we're deleting, post copy is null
            if(op==Operation.DELETE) {
                post=null;
            } else {
                if(doc.getOriginalDocument()==doc)
                    post=pre;
                else
                    post=doc.copy();
            }
            this.hooks=hooks;
        }
    }

    private static final class HookDocs {
        private final Hook hook;
        private final EntityMetadata md;
        private final List<HookDoc> docs=new ArrayList<>();

        public HookDocs(Hook hook,EntityMetadata md) {
            this.hook=hook;
            this.md=md;
        }
    }

    /**
     * Construct hooks with the given hook resolver
     */
    public Hooks(HookResolver r,
                 JsonNodeFactory factory) {
        resolver=r;
        this.factory=factory;
    }

    public void clear() {
        queuedHooks.clear();
    }


    public void queueHooks(CRUDOperationContext ctx) {
        EntityMetadata md=ctx.getEntityMetadata(ctx.getEntityName());
        List<Hook> hooks=md.getHooks().getHooks();
        if(!hooks.isEmpty()) {
            List<DocCtx> documents=ctx.getDocumentsWithoutErrors();
            // We don't want to create a separate copy of every
            // document for each hook. So, we share the only copy of
            // the document between hooks.  First we create a list of
            // DocHooks. Each element in this list contains a
            // document, and all the hooks associated with that
            // document. This step also creates copies of the
            // documents. Then, we create another list, the HookDocs
            // list where each element gives a hook, and all the
            // documents that will be passed to that hook.
            List<DocHooks> docHooksList=new ArrayList<>();
            for(DocCtx doc:documents) {
                if(doc.getOperationPerformed()!=null) {
                    List<Hook> hooksList=null;
                    for(Hook hook:hooks) {
                        boolean queue=false;
                        switch(doc.getOperationPerformed()) {
                        case INSERT: queue=hook.isInsert();break;
                        case UPDATE: queue=hook.isUpdate();break;
                        case DELETE: queue=hook.isDelete();break;
                        case FIND: queue=hook.isFind();break;
                        }
                        if(queue) {
                            if(hooksList==null)
                                hooksList=new ArrayList<>();
                            hooksList.add(hook);
                        }
                    }
                    if(hooksList!=null) {
                        docHooksList.add(new DocHooks(doc,hooksList));
                    }
                }
            }
            // At this point, we have the list of documents, with each
            // document containing its hooks. Now we process that, and
            // create a list of hooks, each containing the documents
            // it will get.
            Map<Hook,HookDocs> hookCache=new HashMap<>();
            for(DocHooks dh:docHooksList) {
                for(Hook hook:dh.hooks) {
                    HookDocs hd=hookCache.get(hook);
                    if(hd==null)
                        hookCache.put(hook,hd=new HookDocs(hook,md));
                    hd.docs.add(new HookDoc(dh.pre,dh.post,dh.op));
                }
            }
            // Queue the hooks
            queuedHooks.addAll(hookCache.values());
        }
    }

    public void callQueuedHooks() {
        for(HookDocs hd:queuedHooks) {
            CRUDHook crudHook=resolver.getHook(hd.hook.getName());
            if(crudHook==null)
                throw Error.get(CrudConstants.ERR_INVALID_HOOK,hd.hook.getName());
            List<HookDoc> processedDocuments;
            if(hd.hook.getProjection()!=null) {
                // Project the docs
                processedDocuments=new ArrayList<>(hd.docs.size());
                Projector projector=Projector.getInstance(hd.hook.getProjection(),hd.md);
                for(HookDoc doc:hd.docs) {
                    processedDocuments.add(new HookDoc(project(doc.getPreDoc(),projector),
                                                       project(doc.getPostDoc(),projector),
                                                       doc.getOperation()));
                }
            } else {
                processedDocuments=hd.docs;
            }
            crudHook.processHook(hd.md,hd.hook.getConfiguration(),processedDocuments);
        }
    }

    private JsonDoc project(JsonDoc doc,Projector p) {
        if(doc==null) {
            return null;
        } else {
            return p.project(doc,factory,new QueryEvaluationContext(doc.getRoot()));
        }
    }
                        
}
