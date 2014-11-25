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
package com.redhat.lightblue.metadata;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import java.io.Serializable;

/**
 * Convenience class to extract document ids from a document. Construct this
 * class once using the metadata, and then use it for the documents of that
 * metadata to extract DocId ojbects. The extracted DocId objects contain the
 * object type, and all the identity fields.
 */
public final class DocIdExtractor implements Serializable {

    private static final long serialVersionUID = 1l;

    private Path[] identityFields;
    private int objectTypeIx;

    public DocIdExtractor(Path[] identityFields) {
        init(identityFields);
    }

    /**
     * Creates a document ID extractfor with the given identity fields
     */
    public DocIdExtractor(Field[] identityFields) {
        if (identityFields == null || identityFields.length == 0) {
            throw new IllegalArgumentException("Empty identity fields");
        }
        Path[] f = new Path[identityFields.length];
        for (int i = 0; i < f.length; i++) {
            f[i] = identityFields[i].getFullPath();
        }
        init(f);
    }

    /**
     * Creates a document ID extractor for the given schema
     */
    public DocIdExtractor(EntitySchema sch) {
        this(sch.getIdentityFields());
    }

    /**
     * Creates a document ID extractor for the given entity
     */
    public DocIdExtractor(EntityMetadata md) {
        this(md.getEntitySchema());
    }

    /**
     * Gets the unique ID of the document
     */
    public DocId getDocId(JsonDoc doc) {
        Object[] values = new Object[identityFields.length];
        int i = 0;
        for (Path x : identityFields) {
            values[i++] = doc.get(x);
        }
        return new DocId(values, objectTypeIx);
    }

    private void init(Path[] f) {
        if (f == null || f.length == 0) {
            throw new IllegalArgumentException("Empty identity fields");
        }
        boolean objectTypePresent = false;
        for (int i = 0; i < f.length; i++) {
            if (f[i].equals(PredefinedFields.OBJECTTYPE_PATH)) {
                objectTypePresent = true;
                objectTypeIx = i;
                break;
            }
        }
        if (objectTypePresent) {
            this.identityFields = f.clone();
        } else {
            objectTypeIx = f.length;
            this.identityFields = new Path[f.length + 1];
            System.arraycopy(f, 0, this.identityFields, 0, f.length);
            this.identityFields[this.identityFields.length - 1] = PredefinedFields.OBJECTTYPE_PATH;
        }
    }
}
