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
package com.redhat.lightblue.crud.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

/**
 * During a save operation, the document provided by the client replaces the
 * copy in the database. If the client has a more limited view of data than is
 * already present (i.e. client using an earlier version of metadata), then
 * there may be some invisible fields, fields that are in the document, but not
 * in the metadata used by the client. To prevent overwriting those fields, we
 * perform a merge operation: all invisible fields are preserved in the updated
 * document.
 */
public final class Merge {

    private static final Logger LOGGER = LoggerFactory.getLogger(Merge.class);

    private final EntityMetadata md;

    public static final class IField {
        private final Path path;
        private final Object value;

        public IField(Path p, Object value) {
            path = p;
            this.value = value;
        }

        public Path getPath() {
            return path;
        }

        public Object getValue() {
            return value;
        }

        public String toString() {
            return path.toString() + ":" + value;
        }
    }

    private final List<IField> invisibleFields = new ArrayList<>();

    public Merge(EntityMetadata md) {
        this.md = md;
    }

    /**
     * Reset the internal state of the Merge
     */
    public void reset() {
        invisibleFields.clear();
    }

    /**
     * Attempts to copy the invisible fields in oldCopy into newCopy. If the
     * attemp is unsucessful, Error is thrown.
     */
    public void merge(DBObject oldCopy, DBObject newCopy) {
        reset();
        findInvisibleFields(oldCopy);
        if (!invisibleFields.isEmpty()) {
            mergeIn(oldCopy, newCopy);
        }
    }

    public List<IField> getInvisibleFields() {
        return (List<IField>) ((ArrayList) invisibleFields).clone();
    }

    private void mergeIn(DBObject oldCopy, DBObject newCopy) {
        // Process invisible fields one by one
        for (IField ifield : invisibleFields) {
            Path p = ifield.getPath();
            DBObject parent = findMergeParent(oldCopy, newCopy, p);
            if (parent == null) {
                throw Error.get(MongoCrudConstants.ERR_SAVE_CLOBBERS_HIDDEN_FIELDS, p.toString());
            }
            ((DBObject) parent).put(p.tail(0), ifield.getValue());
        }
    }

    /**
     * Tries to locate the parent DBObject object that will be the parent of the
     * field that needs to be added to tbe newCopy to preverse the field
     */
    private DBObject findMergeParent(DBObject oldCopy, DBObject newCopy, Path field) {
        LOGGER.debug("Attempting to merge in {}", field);
        // Descend all the way to the parent of the field. Descend on both the oldCopy and the newCopy
        int n = field.numSegments();
        DBObject ret = null;
        if (n > 1) {
            int parentLevel = n - 1;
            Object parent = newCopy;
            Object oldParent = oldCopy;
            boolean fail = false;
            for (int segment = 0; segment < parentLevel; segment++) {
                if (field.isIndex(segment)) {
                    // This is an array
                    // Fail: cannot merge
                    fail = true;
                    break;
                } else {
                    if (parent instanceof DBObject) {
                        String seg = field.head(segment);
                        // This is a nested object
                        Object x = ((DBObject) parent).get(seg);
                        // If the nested object is removed in the new copy,
                        // merging results in data loss, so we fail
                        if (x == null) {
                            // Fail: cannot merge
                            fail = true;
                            break;
                        }
                        parent = x;
                        // The following is guaranteed to succeed. We
                        // built the path from oldCopy.
                        oldParent = ((DBObject) oldParent).get(seg);
                    } else {
                        // Fail: cannot merge
                        fail = true;
                        break;
                    }
                }
            }
            if (!fail) {
                ret = (DBObject) parent;
            }
        } else {
            ret = newCopy;
        }
        return ret;
    }

    /**
     * Construct the initial state of Merge by going through all the fields in
     * the DBObject, and checking if they exist in metadata. Those fields that
     * don't exist in metadata will be store in the invisibleFields list.
     */
    public void findInvisibleFields(DBObject dbObject) {
        MutablePath mp = new MutablePath();
        findInvisibleFields_dbobj(dbObject, mp);
        LOGGER.debug("Invisible fields: {} ", invisibleFields);
    }

    private void findInvisibleFields_obj(Object object,
                                         MutablePath path) {
        if (object instanceof DBObject) {
            findInvisibleFields_dbobj((DBObject) object, path);
        } else if (object instanceof List) {
            path.push(0);
            int index = 0;
            for (Object value : (List) object) {
                path.setLast(index);
                findInvisibleFields_obj(value, path);
                index++;
            }
            path.pop();
        }
    }

    private void findInvisibleFields_dbobj(DBObject dbObject,
                                           MutablePath path) {
        Set<String> fields = dbObject.keySet();
        for (String field : fields) {
            path.push(field);
            LOGGER.debug("Processing {}", path);
            Object value = dbObject.get(field);
            try {
                md.resolve(path);
                findInvisibleFields_obj(value, path);
            } catch (Error e) {
                // Invisible field 
                LOGGER.debug("Invisible field {}", path);
                invisibleFields.add(new IField(path.immutableCopy(), value));
            }
            path.pop();
        }
    }
}
