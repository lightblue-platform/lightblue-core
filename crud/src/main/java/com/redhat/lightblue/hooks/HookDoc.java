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

import com.redhat.lightblue.crud.Operation;

import com.redhat.lightblue.util.JsonDoc;

/**
 * Lists of HookDoc objects are passed to CRUDHooks. This object
 * contains the pre- and post- update versions of the document, and
 * the operation performed on the document. If the operation is
 * DELETE, the post- version is null. If the operation is INSERT or FIND, the
 * pre- version is null.
 */
public class HookDoc {

    private final JsonDoc pre;
    private final JsonDoc post;
    private final Operation op;
    
    /**
     * Constructs a hook document with the given pre- and post- update
     * versions of the document, and the operation performed.
     */
    public HookDoc(JsonDoc pre,JsonDoc post,Operation op) {
        this.pre=pre;
        this.post=post;
        this.op=op;
    }

    /**
     * The version of the document before modifications. Null if operation is INSERT or FIND
     */
    public JsonDoc getPreDoc() {
        return pre;
    }

    /**
     * The version of the document after updates. Null if operation is DELETE
     */
    public JsonDoc getPostDoc() {
        return post;
    }

    /**
     * The operation performed on the document
     */
    public Operation getOperation() {
        return op;
    }

}

