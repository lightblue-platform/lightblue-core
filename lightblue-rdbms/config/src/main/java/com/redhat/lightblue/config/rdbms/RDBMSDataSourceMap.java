/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.config.rdbms;

import com.redhat.lightblue.common.rdbms.RDBMSDataStore;
import com.redhat.lightblue.config.DataSourceConfiguration;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
public class RDBMSDataSourceMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSDataSourceMap.class);

    private final Map<String, DataSourceConfiguration> datasources;
    private final Map<String, DataSourceConfiguration> databases;
    private final Map<String, DataSource> dbMap = new HashMap<>();
    private final Map<String, DataSource> dsMap = new HashMap<>();

    public RDBMSDataSourceMap(DataSourcesConfiguration ds) {
        datasources = ds.getDataSourcesByType(RDBMSDataSourceConfiguration.class);
        databases = new HashMap<>();
        for (DataSourceConfiguration cfg : datasources.values()) {
            databases.put(((RDBMSDataSourceConfiguration) cfg).getDatabaseName(), cfg);
        }
    }

    public DataSource get(RDBMSDataStore store) {
        LOGGER.debug("Returning DataSource for {}", store);
        DataSource ds = null;
        try {
            if (store.getDatasourceName() != null) {
                LOGGER.debug("datasource:{}", store.getDatasourceName());
                getAndPut(dsMap, datasources,store.getDatasourceName());

            } else if (store.getDatabaseName() != null) {
                LOGGER.debug("databaseName:{}", store.getDatabaseName());
                getAndPut(dbMap, databases,store.getDatabaseName());
            }
        } catch (RuntimeException re) {
            LOGGER.error("Cannot get {}:{}", store, re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Cannot get {}:{}", store, e);
            throw new IllegalArgumentException(e);
        }
        if (ds == null) {
            throw new IllegalArgumentException("Cannot find DataSource for  " + store);
        }
        LOGGER.debug("Returning {} for {}", ds, store);
        return ds;
    }
    
    private DataSource getAndPut(Map<String, DataSource> mds, Map<String, DataSourceConfiguration> mdsc,  String name){
        DataSource ds = mds.get(name);
        if (ds == null) {
            RDBMSDataSourceConfiguration cfg = (RDBMSDataSourceConfiguration) mdsc.get(name);
            if (cfg == null) {
                throw new IllegalArgumentException("No datasource/database for " + name);
            }
            ds = cfg.getDataSource(name);
            mds.put(name, ds);
        }
        return ds;
    }
    
}
