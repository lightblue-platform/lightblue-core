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
package com.redhat.lightblue.common.mongo;

import java.io.Serializable;

import com.redhat.lightblue.metadata.DataStore;

public class MongoDataStore implements DataStore, Serializable {

    private static final long serialVersionUID = 1l;

    private String clientJndiName;
    private String databaseName;
    private String collectionName;

    public MongoDataStore() {
    }

    public MongoDataStore(String clientJndiName,
                          String databaseName,
                          String collectionName) {
        this.clientJndiName = clientJndiName;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
    }

    @Override
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

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder(64);
        if (databaseName != null) {
            bld.append(databaseName).append(':');
        }
        bld.append(collectionName);
        if (clientJndiName != null) {
            bld.append('@').append(clientJndiName);
        }
        return bld.toString();
    }

    @Override
    public boolean equals(Object x) {
        try {
            if (x instanceof MongoDataStore) {
                MongoDataStore mds = (MongoDataStore) x;
                try {
                    return strequals(clientJndiName,mds.getClientJndiName()) &&
                        strequals(databaseName,mds.getDatabaseName()) && 
                        strequals(collectionName,mds.getCollectionName());
                } catch (ClassCastException e) {
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean strequals(String s1,String s2) {
        return (s1==null&&s2==null) ||
            (s1!=null&&s2!=null&&s1.equals(s2));
    }

    @Override
    public int hashCode() {
        return (clientJndiName == null ? 1 : clientJndiName.hashCode())
                * (databaseName == null ? 1 : databaseName.hashCode())
                * (collectionName == null ? 1 : collectionName.hashCode());
    }
}
