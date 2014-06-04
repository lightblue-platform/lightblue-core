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
package com.redhat.lightblue.rest.crud;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.crud.CrudManager;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.util.JsonUtils;

public class RestApplication extends Application {

    private static DataSourcesConfiguration datasources;
    private static MetadataManager metadataMgr;
    private static CrudManager crudMgr;

    public static DataSourcesConfiguration getDatasources() {
        return datasources;
    }

    public static void setDatasources(DataSourcesConfiguration datasources) {
        RestApplication.datasources = datasources;
    }

    public static MetadataManager getMetadataMgr() {
        return metadataMgr;
    }

    public static void setMetadataMgr(MetadataManager metadataMgr) {
        RestApplication.metadataMgr = metadataMgr;
    }

    public static CrudManager getCrudMgr() {
        return crudMgr;
    }

    public static void setCrudMgr(CrudManager crudMgr) {
        RestApplication.crudMgr = crudMgr;
    }

    static {
        try {
            datasources=new DataSourcesConfiguration(JsonUtils.json(Thread.currentThread().getContextClassLoader().getResourceAsStream("datasources.json")));
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize datasources.",e);
        }
        metadataMgr=new MetadataManager(datasources);
        crudMgr=new CrudManager(datasources,metadataMgr);
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set=new HashSet<>();
        set.add(CrudResource.class);
        return set;
    }
}
