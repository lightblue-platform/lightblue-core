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

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Fields implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fields.class);

    private static final long serialVersionUID = 1l;

    private final Map<String, Field> fieldMap = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private FieldTreeNode parent;

    public Fields(FieldTreeNode parent) {
        this.parent = parent;
    }

    protected void setParent(FieldTreeNode parent) {
        this.parent = parent;
    }

    public int getNumChildren() {
        return fields.size();
    }

    public Field getField(int index) {
        try {
            return fields.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public Field getField(String name) {
        return fieldMap.get(name);
    }

    public Iterator<Field> getFields() {
        return fields.iterator();
    }

    public boolean has(String name) {
        return fieldMap.containsKey(name);
    }

    public void addNew(Field f) {
        String name = f.getName();
        if (has(name)) {
            throw Error.get(MetadataConstants.ERR_DUPLICATE_FIELD, name);
        }
        f.setParent(parent);
        fieldMap.put(name, f);
        fields.add(f);
    }

    public void put(Field f) {
        String name = f.getName();
        if (has(name)) {
            int n = fields.size();
            for (int i = 0; i < n; i++) {
                Field x = fields.get(i);
                if (x.getName().equals(name)) {
                    fields.set(i, f);
                    break;
                }
            }
        } else {
            fields.add(f);
        }
        f.setParent(parent);
        fieldMap.put(name, f);
    }

    public FieldTreeNode resolve(Path p) {
        return resolve(p, 0);
    }

    protected FieldTreeNode resolve(Path p, int level) {
        if (level >= p.numSegments()) {
            throw Error.get(MetadataConstants.ERR_INVALID_REDIRECTION, p.toString());
        }

        String name = p.head(level);
        Error.push(name);

        try {
            if (p.isIndex(level)) {
                throw Error.get(MetadataConstants.ERR_INVALID_ARRAY_REFERENCE, name + " in " + p.toString());
            } else if (name.equals(Path.ANY)) {
                throw Error.get(MetadataConstants.ERR_INVALID_ARRAY_REFERENCE, name + " in " + p.toString());
            } else if (name.equals(Path.THIS)) {
                if (level + 1 >= p.numSegments()) {
                    return this.parent;
                }
                return this.resolve(p, level + 1);
            } else if (name.equals(Path.PARENT)) {
                if (parent != null && !(parent instanceof EntitySchema.RootNode)) {
                    return parent.resolve(p, level); // Delegate full resolution to the parent.
                } else {
                    throw Error.get(MetadataConstants.ERR_INVALID_FIELD_REFERENCE, name + " in " + p.toString());
                }
            }

            Field field = getField(name);
            if (field == null) {
                throw Error.get(MetadataConstants.ERR_INVALID_FIELD_REFERENCE, name + " in " + p.toString());
            }
            return field.resolve(p, level + 1);

        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, e.getMessage());
        } finally {
            Error.pop();
        }
    }

}
