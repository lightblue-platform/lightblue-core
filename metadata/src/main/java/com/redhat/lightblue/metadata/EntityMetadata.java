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
import java.util.List;

public class EntityMetadata implements Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private String extendsFrom;
    private MetadataStatus status;
    private final ArrayList<StatusChange> statusChangeLog=new ArrayList<StatusChange>();
    private Version version;
    private final EntityAccess access = new EntityAccess();
    private final ArrayList<EntityConstraint> constraints = new ArrayList<EntityConstraint>();
    private DataStore dataStore;
    private final Fields fields = new Fields();

    public EntityMetadata(String name) {
        this.name = name;
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
     * Gets the value of extendsFrom
     *
     * @return the value of extendsFrom
     */
    public String getExtendsFrom() {
        return this.extendsFrom;
    }

    /**
     * Sets the value of extendsFrom
     *
     * @param argExtendsFrom Value to assign to this.extendsFrom
     */
    public void setExtendsFrom(String argExtendsFrom) {
        this.extendsFrom = argExtendsFrom;
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
    public List<StatusChange> getStatusChangeLog() {
        return (List<StatusChange>)statusChangeLog.clone();
    }

    /**
     * Sets the status change log
     */
    public void setStatusChangeLog(Collection<StatusChange> log) {
        statusChangeLog.clear();
        if(log!=null)
            statusChangeLog.addAll(log);
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
     * @param argVersion
     *            Value to assign to this.version
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
     * Returns a list of constraints
     */
    public List<EntityConstraint> getConstraints() {
        return (List<EntityConstraint>) constraints.clone();
    }

    /**
     * Sets the constraints
     */
    public void setConstraints(Collection<EntityConstraint> constraints) {
        this.constraints.clear();
        if (constraints != null)
            this.constraints.addAll(constraints);
    }

    /**
     * Gets the value of dataStore
     * 
     * @return the value of dataStore
     */
    public DataStore getDataStore() {
        return this.dataStore;
    }

    /**
     * Sets the value of dataStore
     * 
     * @param argDataStore
     *            Value to assign to this.dataStore
     */
    public void setDataStore(DataStore argDataStore) {
        this.dataStore = argDataStore;
    }

    /**
     * Gets the value of fields
     * 
     * @return the value of fields
     */
    public Fields getFields() {
        return this.fields;
    }

}
