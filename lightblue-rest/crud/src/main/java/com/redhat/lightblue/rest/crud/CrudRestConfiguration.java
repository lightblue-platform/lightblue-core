package com.redhat.lightblue.rest.crud;

import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.crud.CrudManager;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.util.JsonUtils;

/**
 *
 * @author nmalik
 */
public class CrudRestConfiguration {

    private static DataSourcesConfiguration datasources;
    private static MetadataManager metadataMgr;
    private static CrudManager crudMgr;

    public static DataSourcesConfiguration getDatasources() {
        return datasources;
    }

    public static void setDatasources(DataSourcesConfiguration dsc) {
        datasources = dsc;
    }

    public static MetadataManager getMetadataMgr() {
        return metadataMgr;
    }

    public static void setMetadataMgr(MetadataManager mm) {
        metadataMgr = mm;
    }

    public static CrudManager getCrudMgr() {
        return crudMgr;
    }

    public static void setCrudMgr(CrudManager cm) {
        crudMgr = cm;
    }

    static {
        try {
            datasources = new DataSourcesConfiguration(JsonUtils.json(Thread.currentThread().getContextClassLoader().getResourceAsStream("datasources.json")));
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize datasources.", e);
        }
        metadataMgr = new MetadataManager(datasources);
        crudMgr = new CrudManager(datasources, metadataMgr);
    }
}
