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

import com.redhat.lightblue.metadata.HookConfiguration;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;

/**
 * This type of hook is called once after an operation is completed,
 * with the list of documents that were processed during the operation
 */
public interface BulkProcessingHook extends Hook {

    /**
     * Process the hook
     *
     * @param ctx The operation context. This contains the operation
     * performed on the entity, and the complete list of documents
     * @param cfg The hook configuration as specified in the metadata
     * @param processedDocuments Contains the list of documents that
     * are operated on. If the hook specifies a projection in
     * metadata, the input and output documents are projected.
     */
    void processHook(CRUDOperationContext ctx,
                     HookConfiguration cfg,
                     List<DocCtx> processedDocuments);
                     
}
