/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class RDBMSDataStoreParser <T> implements DataStoreParser<T> {

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
