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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocumentStream;
import com.redhat.lightblue.crud.RewindableDocumentStream;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Hook;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;

/**
 * This class manages hooks. As operations are performed, queueHooks() is called
 * to queue up hooks for successfully processed documents. Once operations are
 * complete and changes are committed, callQueuedHooks is called to execute all
 * hooks that were queued. The queues are executed in the order they are queued,
 * but the hooks for a single document can be executed in a non-deterministic
 * order.
 *
 * Each hook receives a list containing pre- and post- update versions of the
 * documents. If there are multiple hooks for the given operation, the hooks of
 * that operation share the document copies. Because of this, hooks must treat
 * documents as read-only.
 *
 *
 */
public class HookManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HookManager.class);

    private final HookResolver resolver;
    private final JsonNodeFactory factory;

    private final List<QueuedHook> queuedHooks = new ArrayList<>();

    private int queuedHooksSizeB = 0;
    private int maxQueuedHooksSizeB = -1, warnQueuedHooksSizeB = -1;
    private Request forRequest;
    private boolean warnThresholdBreached = false;

    public int getQueuedHooksSizeB() {
        return queuedHooksSizeB;
    }

    /**
     * Threshold is expressed in bytes. This is just an approximation, see @{link {@link JsonUtils#size(JsonNode)} for details.
     *
     * @param maxResultSetSizeB error when this threshold is breached
     * @param warnResultSetSizeB log a warning when this threshold is breached
     * @param forRequest request which resulted in this response, for logging purposes
     */
    public void setQueuedHooksSizeThresholds(int maxQueuedHooksSizeB, int warnQueuedHooksSizeB, Request forRequest) {
        this.forRequest = forRequest;
        this.maxQueuedHooksSizeB = maxQueuedHooksSizeB;
        this.warnQueuedHooksSizeB = warnQueuedHooksSizeB;
    }

    public boolean isErrorOnQueueSizeTooLarge() {
        return maxQueuedHooksSizeB > 0;
    }

    public boolean isWarnOnQueueSizeLarge() {
        return warnQueuedHooksSizeB > 0;
    }

    public boolean isCheckQueueSize() {
        return isErrorOnQueueSizeTooLarge() || isWarnOnQueueSizeLarge();
    }

    private JsonNode enforceQueueSizeLimits(JsonNode jsonNode) {

        if (isCheckQueueSize()) {
            queuedHooksSizeB += JsonUtils.size(jsonNode);
        }

        if (isErrorOnQueueSizeTooLarge() && queuedHooksSizeB >= maxQueuedHooksSizeB) {


            throw Error.get(Response.ERR_RESULT_SIZE_TOO_LARGE, queuedHooksSizeB+"B > "+maxQueuedHooksSizeB+"B");
        } else if (isWarnOnQueueSizeLarge() && !warnThresholdBreached && queuedHooksSizeB >= warnQueuedHooksSizeB) {
            LOGGER.warn("crud:ResultSizeIsLarge: request={}, responseDataSizeB={}", forRequest, queuedHooks);
            warnThresholdBreached = true;
        }

        return jsonNode;

    }

    private static final class HookDocInfo {
        private final JsonDoc pre;
        private final JsonDoc post;
        private final CRUDOperation op;

        public HookDocInfo(DocCtx doc) {
            op = doc.getCRUDOperationPerformed();
            // Create a copy of the original version of the document, if non-null
            if (op == CRUDOperation.INSERT || op == CRUDOperation.FIND) {
                pre = null;
            } else {
                JsonDoc preDoc = doc.getOriginalDocument();
                if (preDoc != null) {
                    pre = preDoc.copy();
                } else {
                    pre = null;
                }
            }
            // If we're deleting, post copy is null
            if (op == CRUDOperation.DELETE) {
                post = null;
            } else if (doc.getUpdatedDocument() != null) {
                post = doc.getUpdatedDocument().copy();
            } else if (doc.getOriginalDocument() == doc && pre != null) {
                post = pre;
            } else {
                post = doc.copy();
            }
        }
    }

    private class HookAndDocs {
        final Hook hook;
        final EntityMetadata md;
        final CRUDHook resolvedHook;
        final List<HookDocInfo> docList=new ArrayList<>();

        HookAndDocs(EntityMetadata md,
                    Hook hook,
                    CRUDHook resolvedHook) {
            this.md=md;
            this.hook=hook;
            this.resolvedHook=resolvedHook;
        }

        void call(String who) {
            List<HookDoc> processedDocuments = new ArrayList<>(docList.size());
            if (hook.getProjection() != null) {
                // Project the docs
                Projector projector = Projector.getInstance(hook.getProjection(), md);
                for (HookDocInfo doc : docList) {
                    processedDocuments.add(new HookDoc(md,
                                                       project(doc.pre, projector),
                                                       project(doc.post, projector),
                                                       doc.op,
                                                       who));
                }
            } else {
                for (HookDocInfo doc : docList) {
                    processedDocuments.add(new HookDoc(md,
                                                       doc.pre,
                                                       doc.post,
                                                       doc.op,
                                                       who));
                }
            }
            if(!processedDocuments.isEmpty()) {
                try {
                    resolvedHook.processHook(md, hook.getConfiguration(), processedDocuments);
                } catch (RuntimeException e) {
                    if (e.getClass().isAnnotationPresent(StopHookProcessing.class)) {
                        throw e;
                    } else {
                        LOGGER.error("Exception while processing hook of type: " + resolvedHook.getClass(), e);
                    }
                }
            }
        }
    }

    private class QueuedHook {
        final String who;
        final List<HookAndDocs> hooks;

        QueuedHook(String who,List<HookAndDocs> hooks) {
            this.who=who;
            this.hooks=hooks;
        }

        void call() {
            for(HookAndDocs hook:hooks) {
                hook.call(who);
            }
        }
    }

    /**
     * Construct hooks with the given hook resolver
     */
    public HookManager(HookResolver r, JsonNodeFactory factory) {
        resolver = r;
        this.factory = factory;
    }

    /**
     * Clears all queued hooks
     */
    public void clear() {
        queuedHooks.clear();

        queuedHooksSizeB = 0;
        warnQueuedHooksSizeB = -1;
        maxQueuedHooksSizeB = -1;
        forRequest = null;
        warnThresholdBreached = false;
    }

    /**
     * Returns true if there are any hooks for this operation
     */
    public boolean hasHooks(CRUDOperationContext ctx,CRUDOperation op) {
        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        List<Hook> mdHooks = md.getHooks().getHooks();
        for (Hook h : mdHooks) {
            switch (op) {
            case INSERT: if(h.isInsert()) return true;break;
            case UPDATE: if(h.isUpdate()) return true;break;
            case DELETE: if(h.isDelete()) return true;break;
            }
        }
        return false;
    }
    
    /**
     * Queues hooks for the operation represented by the operation context.
     *
     * @param ctx Operation context
     *
     * This will create copies of all the documents that has no errors in the
     * context, and save them for later hook execution.
     */
    public void queueHooks(CRUDOperationContext ctx) {
        queueHooks(ctx, false);
    }

    /**
     * Queues mediator hooks for the operation represented by the operation
     * context.
     *
     * @param ctx Operation context
     *
     * This operation will only queue mediator hooks. This will create copies of
     * all the documents that has no errors in the context, and save them for
     * later hook execution.
     */
    public void queueMediatorHooks(CRUDOperationContext ctx) {
        queueHooks(ctx, true);
    }

    /**
     * Calls all queued hooks, and then clears the queued hooks. Any hook that
     * failed will be logged, but hook execution will continue unless one of the
     * hooks throws an exception with @StopHookProcessing annotation.
     */
    public void callQueuedHooks() {
        for (QueuedHook q: queuedHooks) {
            q.call();
        }
        clear();
    }

    
    private void addDocument(List<HookAndDocs> hooks,DocCtx doc) {
        if(!doc.hasErrors()) {
            for(HookAndDocs hook:hooks) {
                boolean queue=false;
                if(doc.getCRUDOperationPerformed()!=null) {
                    switch(doc.getCRUDOperationPerformed()) {
                    case INSERT: queue=hook.hook.isInsert();break;
                    case UPDATE: queue=hook.hook.isUpdate();break;
                    case DELETE: queue=hook.hook.isDelete();break;
                    }
                    if(queue) {
                        HookDocInfo hdi = new HookDocInfo(doc);

                        if (isCheckQueueSize()) {

                            LOGGER.debug("Checking original doc size");
                            enforceQueueSizeLimits(doc.getRoot());

                            if (hdi.pre != null && doc.getRoot() != hdi.pre.getRoot()) {
                                LOGGER.debug("Checking pre copy size");
                                enforceQueueSizeLimits(hdi.pre.getRoot());
                            }

                            if (hdi.post != null && doc.getRoot() != hdi.post.getRoot()) {
                                LOGGER.debug("Checking post copy size");
                                enforceQueueSizeLimits(hdi.post.getRoot());
                            }
                        }

                        hook.docList.add(hdi);

                    }
                }
            }
        }
    }

    private void queueHooks(CRUDOperationContext ctx, boolean mediatorHooks) {
        LOGGER.debug("queueHooks start mediatorHooks={}", mediatorHooks);
        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        List<Hook> mdHooks = md.getHooks().getHooks();
        LOGGER.debug("There are {} hooks in metadata", mdHooks.size());
        List<HookAndDocs> hookList=new ArrayList<>();
        for (Hook h : mdHooks) {
            CRUDHook crudHook = resolver.getHook(h.getName());
            if (crudHook == null) {
                throw Error.get(CrudConstants.ERR_INVALID_HOOK, h.getName());
            }
            if ((mediatorHooks && crudHook instanceof MediatorHook)
                    || (!mediatorHooks && !(crudHook instanceof MediatorHook))) {
                hookList.add(new HookAndDocs(md,h,crudHook));
            }
        }
        if(!hookList.isEmpty()) {
            // extract the who from the context if possible
            String who = null;
            if (ctx instanceof OperationContext && ((OperationContext) ctx).getRequest() != null
                && ((OperationContext) ctx).getRequest().getClientId() != null) {
                who = ((OperationContext) ctx).getRequest().getClientId().getPrincipal();
            }
            
            DocumentStream<DocCtx> documents=ctx.getDocumentStream();
            if(documents instanceof RewindableDocumentStream) {
                RewindableDocumentStream<DocCtx> stream=((RewindableDocumentStream<DocCtx>)documents).rewind();
                while(stream.hasNext()) {
                    addDocument(hookList,stream.next());
                }
            } else {
                documents.addListener(d->addDocument(hookList,d));
            }
            queuedHooks.add(new QueuedHook(who,hookList));
        }        
    }

    public boolean isHookQueueEmpty() {
        return queuedHooks.isEmpty();
    }

    private  JsonDoc project(JsonDoc doc, Projector p) {
        if (doc == null) {
            return null;
        } else {
            return p.project(doc, factory);
        }
    }
}
