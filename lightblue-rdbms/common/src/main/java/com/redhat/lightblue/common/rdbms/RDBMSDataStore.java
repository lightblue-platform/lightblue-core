/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.common.rdbms;

import com.redhat.lightblue.metadata.DataStore;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author lcestari
 */
public class RDBMSDataStore implements DataStore, Serializable {

    private static final long serialVersionUID = 1l;

    private String datasourceName;
    private String databaseName;

    public RDBMSDataStore() {
    }

    public RDBMSDataStore(String databaseName,
                          String datasourceName) {
        this.databaseName = databaseName;
        this.datasourceName = datasourceName;
    }

    @Override
    public String getBackend() {
        return "rdbms";
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.datasourceName);
        hash = 17 * hash + Objects.hashCode(this.databaseName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RDBMSDataStore other = (RDBMSDataStore) obj;
        if (!Objects.equals(this.datasourceName, other.datasourceName)) {
            return false;
        }
        if (!Objects.equals(this.databaseName, other.databaseName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RDBMSDataStore{" + "datasourceName=" + datasourceName + ", databaseName=" + databaseName + '}';
    }
}
