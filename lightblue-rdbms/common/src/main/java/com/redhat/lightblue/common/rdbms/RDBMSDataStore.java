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
