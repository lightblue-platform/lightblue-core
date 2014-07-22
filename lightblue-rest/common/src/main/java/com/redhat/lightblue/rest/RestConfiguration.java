/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
