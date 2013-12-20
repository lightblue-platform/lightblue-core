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

package com.redhat.lightblue.metadata.mongo;

import java.io.Serializable;

import com.redhat.lightblue.metadata.DataStore;

public class MongoDataStore implements DataStore, Serializable {

    private static final long serialVersionUID=1l;

    private String clientJndiName;
    private String databaseName;
    private String collectionName;

    public MongoDataStore() {}

    public MongoDataStore(String clientJndiName,
                          String databaseName,
                          String collectionName) {
        this.clientJndiName=clientJndiName;
        this.databaseName=databaseName;
        this.collectionName=collectionName;
    }

    public String getType() {
        return "mongo";
    }
    
    /**
     * Gets the value of clientJndiName
     *
     * @return the value of clientJndiName
     */
    public String getClientJndiName() {
        return this.clientJndiName;
    }

    /**
     * Sets the value of clientJndiName
     *
     * @param argClientJndiName Value to assign to this.clientJndiName
     */
    public void setClientJndiName(String argClientJndiName) {
        this.clientJndiName = argClientJndiName;
    }

    /**
     * Gets the value of databaseName
     *
     * @return the value of databaseName
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Sets the value of databaseName
     *
     * @param argDatabaseName Value to assign to this.databaseName
     */
    public void setDatabaseName(String argDatabaseName) {
        this.databaseName = argDatabaseName;
    }

    /**
     * Gets the value of collectionName
     *
     * @return the value of collectionName
     */
    public String getCollectionName() {
        return this.collectionName;
    }

    /**
     * Sets the value of collectionName
     *
     * @param argCollectionName Value to assign to this.collectionName
     */
    public void setCollectionName(String argCollectionName) {
        this.collectionName = argCollectionName;
    }

    public String toString() {
        StringBuilder bld=new StringBuilder(64);
        if(databaseName!=null)
            bld.append(databaseName).append(':');
        bld.append(collectionName);
        if(clientJndiName!=null)
            bld.append('@').append(clientJndiName);
        return bld.toString();
    }
    
    public boolean equals(Object x) {
        try {
            return equals((MongoDataStore)x);
        } catch (Exception e) {}
        return false;
    }

    public boolean equals(MongoDataStore x) {
        if(x!=null)
            try {
                return ((x.clientJndiName==null&&clientJndiName==null)||
                        (x.clientJndiName!=null&&clientJndiName!=null&&x.clientJndiName.equals(clientJndiName))) &&
                    ( (x.databaseName==null&&databaseName==null)||
                      (x.databaseName!=null&&databaseName!=null&&x.databaseName.equals(databaseName)) ) &&
                    ( (x.collectionName==null&&collectionName==null) ||
                      (x.collectionName!=null&&collectionName!=null&&x.collectionName.equals(collectionName)) );
            } catch (ClassCastException e) {}
        return false;
    }

    public int hashCode() {
        return (clientJndiName==null?1:clientJndiName.hashCode())*
            (databaseName==null?1:databaseName.hashCode())*
            (collectionName==null?1:collectionName.hashCode());
    }
}

