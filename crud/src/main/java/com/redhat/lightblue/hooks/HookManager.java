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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Hook;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final List<HookDocs> queuedHooks = new ArrayList<>();

    private static final class DocHooks {
        private final JsonDoc pre;
        private final JsonDoc post;
        private final CRUDOperation op;
        private final Map<Hook, CRUDHook> hooks;

        public DocHooks(DocCtx doc, Map<Hook, CRUDHook> hooks) {
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
            } else {
                if(doc.getOriginalOutputDocument() != null) {
                    post = doc.getOriginalOutputDocument().copy();
                } else {
                    if (doc.getOriginalDocument() == doc && pre != null) {
                        post = pre;
                    } else {
                        post = doc.copy();
                    }
                }
            }
            this.hooks = hooks;
        }
    }

    private static final class HookDocs {
        private final Hook hook;
        private final CRUDHook crudHook;
        private final EntityMetadata md;
        private final List<HookDoc> docs = new ArrayList<>();

        public HookDocs(Hook hook, CRUDHook crudHook, EntityMetadata md) {
            this.hook = hook;
            this.crudHook = crudHook;
            this.md = md;
        }

        @Override
        public String toString() {
            return "HookDocs [hook=" + hook + ", crudHook=" + crudHook + ", md=" + md + ", docs=" + docs + "]";
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
        for (HookDocs hd : queuedHooks) {
            List<HookDoc> processedDocuments;
            if (hd.hook.getProjection() != null) {
                // Project the docs
                processedDocuments = new ArrayList<>(hd.docs.size());
                Projector projector = Projector.getInstance(hd.hook.getProjection(), hd.md);
                for (HookDoc doc : hd.docs) {
                    processedDocuments.add(new HookDoc(
                            doc.getEntityMetadata(),
                            project(doc.getPreDoc(), projector),
                            project(doc.getPostDoc(), projector),
                            doc.getCRUDOperation()));
                }
            } else {
                processedDocuments = hd.docs;
            }
            try {
                hd.crudHook.processHook(hd.md, hd.hook.getConfiguration(), processedDocuments);
            } catch (RuntimeException e) {
                if (e.getClass().isAnnotationPresent(StopHookProcessing.class)) {
                    throw e;
                }
            }
        }
        clear();
    }

    private void queueHooks(CRUDOperationContext ctx, boolean mediatorHooks) {
        LOGGER.debug("queueHooks start mediatorHooks={}", mediatorHooks);
        EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
        List<Hook> mdHooks = md.getHooks().getHooks();
        LOGGER.debug("There are {} hooks in metadata", mdHooks.size());
        Map<Hook, CRUDHook> hooks = new HashMap<>();
        for (Hook h : mdHooks) {
            CRUDHook crudHook = resolver.getHook(h.getName());
            if (crudHook == null) {
                throw Error.get(CrudConstants.ERR_INVALID_HOOK, h.getName());
            }
            if ((mediatorHooks && crudHook instanceof MediatorHook)
                    || (!mediatorHooks && !(crudHook instanceof MediatorHook))) {
                hooks.put(h, crudHook);
            }
        }
        LOGGER.debug("Hooks are resolved: {}", hooks);
        if (!hooks.isEmpty()) {
            List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
            LOGGER.debug("There are {} documents", documents.size());
            // We don't want to create a separate copy of every
            // document for each hook. So, we share the only copy of
            // the document between hooks.  First we create a list of
            // DocHooks. Each element in this list contains a
            // document, and all the hooks associated with that
            // document. This step also creates copies of the
            // documents. Then, we create another list, the HookDocs
            // list where each element gives a hook, and all the
            // documents that will be passed to that hook.
            List<DocHooks> docHooksList = new ArrayList<>();
            for (DocCtx doc : documents) {
                if (doc.getCRUDOperationPerformed() != null) {
                    Map<Hook, CRUDHook> hooksList = null;
                    for (Map.Entry<Hook, CRUDHook> hook : hooks.entrySet()) {
                        boolean queue;
                        switch (doc.getCRUDOperationPerformed()) {
                            case INSERT:
                                queue = hook.getKey().isInsert();
                                break;
                            case UPDATE:
                                queue = hook.getKey().isUpdate();
                                break;
                            case DELETE:
                                queue = hook.getKey().isDelete();
                                break;
                            case FIND:
                                queue = hook.getKey().isFind();
                                break;
                            default:
                                queue = false;
                                break;
                        }
                        if (queue) {
                            if (hooksList == null) {
                                hooksList = new HashMap<>();
                            }
                            hooksList.put(hook.getKey(), hook.getValue());
                        }
                    }
                    if (hooksList != null) {
                        docHooksList.add(new DocHooks(doc, hooksList));
                    }
                }
            }
            LOGGER.debug("List of docs with hooks size={}", docHooksList.size());
            // At this point, we have the list of documents, with each
            // document containing its hooks. Now we process that, and
            // create a list of hooks, each containing the documents
            // it will get.
            Map<Hook, HookDocs> hookCache = new HashMap<>();
            for (DocHooks dh : docHooksList) {
                for (Map.Entry<Hook, CRUDHook> hook : dh.hooks.entrySet()) {
                    HookDocs hd = hookCache.get(hook.getKey());
                    if (hd == null) {
                        hd = new HookDocs(hook.getKey(), hook.getValue(), md);
                        hookCache.put(hook.getKey(), hd);
                    }

                    // extract the who from the context if possible
                    String who = null;
                    if (ctx instanceof OperationContext && ((OperationContext)ctx).getRequest() != null &&
                            ((OperationContext)ctx).getRequest().getClientId() != null) {
                        who = ((OperationContext)ctx).getRequest().getClientId().getPrincipal();
                    }

                    hd.docs.add(new HookDoc(
                            hd.md,
                            dh.pre, dh.post, dh.op, who));
                }
            }
            LOGGER.debug("Queueing {} hooks", hookCache.size());
            // Queue the hooks
            queuedHooks.addAll(hookCache.values());
        }
    }

    private JsonDoc project(JsonDoc doc, Projector p) {
        if (doc == null) {
            return null;
        } else {
            return p.project(doc, factory);
        }
    }
}