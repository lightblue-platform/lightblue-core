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

import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonCompare;

import java.util.Collection;
import java.util.List;

/**
 * Container for info and schema metadata, gives details of a single version of
 * metadata. Implementation is a facade on top of EntityInfo and EntitySchema.
 *
 * @author nmalik
 */
public class EntityMetadata extends MetadataObject {

    private final EntityInfo info;
    private final EntitySchema schema;

    public EntityMetadata(String name) {
        this(new EntityInfo(name), new EntitySchema(name));
    }

    public EntityMetadata(EntityInfo info, EntitySchema schema) {
        this.info = info;
        this.schema = schema;
    }

    public EntityInfo getEntityInfo() {
        return info;
    }

    public EntitySchema getEntitySchema() {
        return schema;
    }

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return this.info.getName();
    }

    /**
     * Return the status of this particular version of the entity
     */
    public MetadataStatus getStatus() {
        return schema.getStatus();
    }

    /**
     * Sets the status of this particular version of the entity
     */
    public void setStatus(MetadataStatus status) {
        schema.setStatus(status);
    }

    /**
     * Returns the status change log
     */
    public List<StatusChange> getStatusChangeLog() {
        return schema.getStatusChangeLog();
    }

    /**
     * Sets the status change log
     */
    public void setStatusChangeLog(Collection<StatusChange> log) {
        schema.setStatusChangeLog(log);
    }

    /**
     * Gets the value of version
     *
     * @return the value of version
     */
    public Version getVersion() {
        return schema.getVersion();
    }

    /**
     * Sets the value of version
     *
     * @param argVersion Value to assign to this.version
     */
    public void setVersion(Version argVersion) {
        schema.setVersion(argVersion);
    }

    /**
     * Gets the value of access
     *
     * @return the value of access
     */
    public EntityAccess getAccess() {
        return schema.getAccess();
    }

    /**
     * Returns a deep copy list of constraints
     */
    public List<EntityConstraint> getConstraints() {
        return schema.getConstraints();
    }

    /**
     * Sets the constraints
     */
    public void setConstraints(Collection<EntityConstraint> constraints) {
        schema.setConstraints(constraints);
    }

    /**
     * Gets the value of datastore
     *
     * @return the value of datastore
     */
    public DataStore getDataStore() {
        return info.getDataStore();
    }

    /**
     * Sets the value of datastore
     *
     * @param argDataStore Value to assign to this.datastore
     */
    public void setDataStore(DataStore argDataStore) {
        info.setDataStore(argDataStore);
    }

    /**
     * Returns hooks
     */
    public Hooks getHooks() {
        return info.getHooks();
    }

    /**
     * Gets the value of fields
     *
     * @return the value of fields
     */
    public Fields getFields() {
        return schema.getFields();
    }

    public FieldTreeNode getFieldTreeRoot() {
        return schema.getFieldTreeRoot();
    }

    public FieldCursor getFieldCursor() {
        return schema.getFieldCursor();
    }

    public FieldCursor getFieldCursor(Path p) {
        return schema.getFieldCursor(p);
    }

    public FieldTreeNode resolve(Path p) {
        return schema.resolve(p);
    }

    /**
     * Verifies that the entity info and entity schema as a whole are valid. For
     * example, are all enum constraints in entity schema referencing an enum
     * defined in entity info? Execution is intended to be generic but could be
     * extended if necessary.
     *
     * @throws Error on any validation errors
     */
    public void validate() {
        // Check enum in schema against entity info
        FieldCursor cursor = getEntitySchema().getFieldCursor();
        while (cursor.next()) {
            FieldTreeNode node = cursor.getCurrentNode();
            if (node instanceof Field) {
                Field field = (Field) node;
                for (FieldConstraint fc : field.getConstraints()) {
                    if (fc instanceof EnumConstraint) {
                        // check that this field's enum name is valid
                        String enumName = ((EnumConstraint) fc).getName();
                        if (getEntityInfo().getEnums().getEnum(enumName) == null) {
                            throw Error.get(MetadataConstants.ERR_INVALID_ENUM, enumName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds a document comparator for comparing documents of this type. That
     * involves registering all array element identities with the comparator so
     * array comparisons can be done corectly and efficiently.
     */
    public JsonCompare getDocComparator() {
        return schema.getDocComparator();
    }
}
