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
package com.redhat.lightblue.metadata.rdbms.impl;

import com.redhat.lightblue.common.rdbms.RDBMSDataStore;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.common.rdbms.RDBMSConstants;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

/**
 *
 * @author lcestari
 */
public class RDBMSDataStoreParser<T> implements DataStoreParser<T> {

    public static final String COLLECTION_REQUIRED = "COLLECTION_REQUIRED";

    public static final String NAME = "rdbms";

    @Override
    public DataStore parse(String name, MetadataParser<T> p, T node) {
        if (!NAME.equals(name)) {
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_ILL_FORMED_METADATA, name);
        }
        RDBMSDataStore ds = new RDBMSDataStore();
        ds.setDatabaseName(p.getStringProperty(node, "database"));
        ds.setDatasourceName(p.getStringProperty(node, "datasource"));
        return ds;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, DataStore object) {
        RDBMSDataStore ds = (RDBMSDataStore) object;
        if (ds.getDatabaseName() != null) {
            p.putString(emptyNode, "database", ds.getDatabaseName());
        }
        if (ds.getDatasourceName() != null) {
            p.putString(emptyNode, "datasource", ds.getDatasourceName());
        }
    }

    @Override
    public String getDefaultName() {
        return NAME;
    }
}
