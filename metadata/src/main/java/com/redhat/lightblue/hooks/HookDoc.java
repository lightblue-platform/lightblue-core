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

import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Lists of HookDoc objects are passed to CRUDHooks. This object contains the
 * pre- and post- update versions of the document, and the operation performed
 * on the document. If the operation is DELETE, the post- version is null. If
 * the operation is INSERT or FIND, the pre- version is null.
 */
public class HookDoc {
    private final EntityMetadata entityMetadata;
    private final JsonDoc pre;
    private final JsonDoc post;
    private final CRUDOperation crudOperation;
    private final Date when;
    private final String who;

    /**
     * Deep copy.
     *
     * @param hd hook doc to copy
     */
    public HookDoc(HookDoc hd) {
        this(hd.getEntityMetadata(),
                hd.getPreDoc(),
                hd.getPostDoc(),
                hd.getCRUDOperation(),
                hd.when,
                hd.who);
    }

    /**
     * Constructs a hook document with the given pre- and post- update versions
     * of the document, and the operation performed.
     */
    public HookDoc(EntityMetadata entityMetadata, JsonDoc pre, JsonDoc post, CRUDOperation crudOperation) {
        this(entityMetadata, pre, post, crudOperation, GregorianCalendar.getInstance().getTime(), null);
    }

    public HookDoc(EntityMetadata entityMetadata, JsonDoc pre, JsonDoc post, CRUDOperation crudOperation, String who) {
        this(entityMetadata, pre, post, crudOperation, GregorianCalendar.getInstance().getTime(), who);
    }

    private HookDoc(EntityMetadata entityMetadata, JsonDoc pre, JsonDoc post, CRUDOperation crudOperation, Date when, String who) {
        this.entityMetadata = entityMetadata;
        this.pre = pre;
        this.post = post;
        this.crudOperation = crudOperation;
        this.when = when;
        this.who = who;
    }

    /**
     * The entity metadata for the document. Can be used to retrieve things
     * about the entity for a hook such as identifying fields.
     */
    public EntityMetadata getEntityMetadata() {
        return entityMetadata;
    }

    /**
     * The version of the document before modifications. Null if operation is
     * INSERT or FIND.
     */
    public JsonDoc getPreDoc() {
        return pre;
    }

    /**
     * The version of the document after updates. Null if operation is DELETE.
     */
    public JsonDoc getPostDoc() {
        return post;
    }

    /**
     * The operation performed on the document.
     */
    public CRUDOperation getCRUDOperation() {
        return crudOperation;
    }

    /**
     * The date/time when this object was created.
     */
    public Date getWhen() {
        return when;
    }

    /**
     * Who updated the object causing this hook to be fired.
     */
    public String getWho() {
        return who;
    }
}
