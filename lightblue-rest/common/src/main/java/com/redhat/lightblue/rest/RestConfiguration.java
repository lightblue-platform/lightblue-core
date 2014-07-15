/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest;

import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Moving initialization logic out of RestApplication. 
 *
 * @author nmalik
 */
public class RestConfiguration {

    private static DataSourcesConfiguration datasources;
    private static LightblueFactory factory;

    public static DataSourcesConfiguration getDatasources() {
        return datasources;
    }

    public static LightblueFactory getFactory() {
        return factory;
    }

    public static void setDatasources(DataSourcesConfiguration ds) {
        datasources=ds;
    }

    public static void setFactory(LightblueFactory f) {
        factory=f;
    }

    static {
        try {
            datasources = new DataSourcesConfiguration(JsonUtils.json(Thread.currentThread().getContextClassLoader().getResourceAsStream("datasources.json")));
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize datasources.", e);
        }
        factory = new LightblueFactory(datasources);
    }

}
