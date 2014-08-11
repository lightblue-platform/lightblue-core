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
    private final String entityName;
    private final EntityMetadata entityMetadata;
    private final JsonDoc pre;
    private final JsonDoc post;
    private final Operation op;
    private final Date when;

    /**
     * Deep copy.
     *
     * @param hd hook doc to copy
     */
    public HookDoc(HookDoc hd) {
        this(hd.getEntityName(),
                hd.getEntityMetadata(),
                hd.getPostDoc(),
                hd.getPreDoc(),
                hd.getOperation(),
                hd.when);
    }

    /**
     * Constructs a hook document with the given pre- and post- update versions
     * of the document, and the operation performed.
     */
    public HookDoc(String entityName, EntityMetadata entityMetadata, JsonDoc pre, JsonDoc post, Operation op) {
        this(entityName, entityMetadata, pre, post, op, GregorianCalendar.getInstance().getTime());
    }

    private HookDoc(String entityName, EntityMetadata entityMetadata, JsonDoc pre, JsonDoc post, Operation op, Date when) {
        this.entityName = entityName;
        this.entityMetadata = entityMetadata;
        this.pre = pre;
        this.post = post;
        this.op = op;
        this.when = when;
    }

    /**
     * The name of the entity.
     */
    public String getEntityName() {
        return entityName;
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
    public Operation getOperation() {
        return op;
    }

    /**
     * The date/time when this object was created.
     */
    public Date getWhen() {
        return when;
    }
}
