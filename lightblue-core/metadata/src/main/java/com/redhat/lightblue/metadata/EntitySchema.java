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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

/**
 * Version specific bits of metadata.
 *
 * @author nmalik
 */
public class EntitySchema implements Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private Version version;
    private MetadataStatus status;
    private final ArrayList<StatusChange> statusChangeLog = new ArrayList<>();
    //hooks
    private final EntityAccess access = new EntityAccess();
    private final ArrayList<EntityConstraint> constraints = new ArrayList<>();
    private final Fields fields;
    private final FieldTreeNode fieldRoot;
    private final Map<String,Object> properties=new HashMap<>();

    private class RootNode implements FieldTreeNode, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Type getType() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            return true;
        }

        @Override
        public Iterator<? extends FieldTreeNode> getChildren() {
            return fields.getFields();
        }

        @Override
        public FieldTreeNode resolve(Path p) {
            return fields.resolve(p);
        }

        @Override
        public FieldTreeNode resolve(Path p, int level) {
            return fields.resolve(p, level);
        }

        @Override
        public FieldTreeNode getParent() {
            return null;
        }

        @Override
        public Path getFullPath() {
            return Path.EMPTY;
        }

        @Override
        public MutablePath getFullPath(MutablePath mp) {
            return Path.EMPTY.mutableCopy();
        }
    };

    public EntitySchema(String name) {
        this.name = name;
        this.fieldRoot = new RootNode();
        this.fields = new Fields(fieldRoot);
    }

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the status of this particular version of the entity
     */
    public MetadataStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of this particular version of the entity
     */
    public void setStatus(MetadataStatus status) {
        this.status = status;
    }

    /**
     * Returns the status change log
     */
    @SuppressWarnings("unchecked")
    public List<StatusChange> getStatusChangeLog() {
        return (List<StatusChange>) statusChangeLog.clone();
    }

    /**
     * Sets the status change log
     */
    public void setStatusChangeLog(Collection<StatusChange> log) {
        statusChangeLog.clear();
        if (log != null) {
            statusChangeLog.addAll(log);
        }
    }

    /**
     * Gets the value of version
     *
     * @return the value of version
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * Sets the value of version
     *
     * @param argVersion Value to assign to this.version
     */
    public void setVersion(Version argVersion) {
        this.version = argVersion;
    }

    /**
     * Gets the value of access
     *
     * @return the value of access
     */
    public EntityAccess getAccess() {
        return this.access;
    }

    /**
     * Returns a deep copy list of constraints
     */
    @SuppressWarnings("unchecked")
    public List<EntityConstraint> getConstraints() {
        return (List<EntityConstraint>) constraints.clone();
    }

    /**
     * Sets the constraints
     */
    public void setConstraints(Collection<EntityConstraint> constraints) {
        this.constraints.clear();
        if (constraints != null) {
            this.constraints.addAll(constraints);
        }
    }

    /**
     * Gets the value of fields
     *
     * @return the value of fields
     */
    public Fields getFields() {
        return this.fields;
    }

    public FieldTreeNode getFieldTreeRoot() {
        return fieldRoot;
    }

    public FieldCursor getFieldCursor() {
        return new FieldCursor(new Path(), getFieldTreeRoot());
    }

    public FieldCursor getFieldCursor(Path p) {
        if (p.numSegments() == 0) {
            return getFieldCursor();
        } else {
            FieldTreeNode tn = resolve(p);
            if (tn != null) {
                return new FieldCursor(p, tn);
            } else {
                return null;
            }
        }
    }

    public FieldTreeNode resolve(Path p) {
        Error.push(name);
        try {
            return fields.resolve(p);
        } finally {
            Error.pop();
        }
    }

    public Map<String,Object> getProperties() {
        return properties;
    }
}
