/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata;

import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Moving initialization logic out of RestApplication. TODO probably needs refactored, not doing now because previous
 * location was a hack too.
 *
 * @author nmalik
 */
public class MetadataRestConfiguration {

    private static MetadataManager metadataManager;
    private static DataSourcesConfiguration datasources;

    public static DataSourcesConfiguration getDatasources() {
        return datasources;
    }

    public static void setDatasources(DataSourcesConfiguration dsc) {
        datasources = dsc;
    }

    static {
        try {
            datasources = new DataSourcesConfiguration(JsonUtils.json(Thread.currentThread().getContextClassLoader().getResourceAsStream("datasources.json")));
            metadataManager = new MetadataManager(datasources);
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize:" + e);
        }
    }

    public static MetadataManager getMetadataManager() {
        return metadataManager;
    }

    public static void setMetadataManager(MetadataManager mm) {
        metadataManager = mm;
    }

}
