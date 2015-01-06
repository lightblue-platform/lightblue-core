package com.redhat.lightblue.test.metadata;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.AbstractMetadata;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.VersionInfo;

/**
 * A simple implementation of {@link AbstractMetadata} for testing purposes.
 */
public class FakeMetadata extends AbstractMetadata {

    private static final long serialVersionUID = 1L;

    private final Map<String, EntityInfo> entityInfoMap = new HashMap<String, EntityInfo>();
    private final Map<EntityInfo, Map<String, Data>> dataMap = new HashMap<EntityInfo, Map<String,Data>>();

    public void setDependencies(String entityName, String version, Response dependencies){
        Data data = getOrCreateDataForVersion(entityName, version, true);
        data.setDependencies(dependencies);
    }

    @Override
    public Response getDependencies(String entityName, String version) {
        Data data = getOrCreateDataForVersion(entityName, version, false);
        if(data == null){
            return null;
        }
        return data.getDependency();
    }

    public void setAccess(String entityName, String version, Response access){
        Data data = getOrCreateDataForVersion(entityName, version, true);
        data.setAccess(access);
    }

    @Override
    public Response getAccess(String entityName, String version) {
        Data data = getOrCreateDataForVersion(entityName, version, false);
        if(data == null){
            return null;
        }
        return data.getAccess();
    }

    public void setEntityMetadata(String entityName, String version, EntityMetadata entityMetadata){
        Data data = getOrCreateDataForVersion(entityName, version, true);
        data.setEntityMetadata(entityMetadata);
    }

    @Override
    public EntityMetadata getEntityMetadata(String entityName, String version) {
        Data data = getOrCreateDataForVersion(entityName, version, false);
        if(data == null){
            return null;
        }
        return data.getEntityMetadata();
    }

    public void setEntityInfo(EntityInfo info){
        entityInfoMap.put(info.getName(), info);
    }

    @Override
    public EntityInfo getEntityInfo(String entityName) {
        return entityInfoMap.get(entityName);
    }

    @Override
    public String[] getEntityNames(MetadataStatus... statuses) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VersionInfo[] getEntityVersions(String entityName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createNewMetadata(EntityMetadata md) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createNewSchema(EntityMetadata md) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateEntityInfo(EntityInfo info) {
        if(entityInfoMap.get(info.getName()) == null){
            throw new IllegalStateException("No EntityInfo currently exists with name: " + info.getName());
        }

        entityInfoMap.put(info.getName(), info);
    }

    @Override
    public void setMetadataStatus(String entityName, String version,
            MetadataStatus newStatus, String comment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeEntity(String entityName) {
        EntityInfo info = entityInfoMap.get(entityName);
        if(info == null){
            return;
        }

        dataMap.remove(info);
        entityInfoMap.remove(entityName);
    }

    @Override
    protected boolean checkVersionExists(String entityName, String version) {
        EntityInfo info = entityInfoMap.get(entityName);
        if(info == null){
            return false;
        }

        if(dataMap.get(info) == null){
            return false;
        }
        return dataMap.get(info).containsKey(version);
    }

    @Override
    protected void checkDataStoreIsValid(EntityInfo md) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Gets or creates a new {@link Data} for the passed in name and version.
     * @param entityName - entity name
     * @param version - version
     * @param createIfNull - <code>true</code> if a new {@link Data} should be created and returned,
     * otherwise <code>false</code> and <code>null</code> will be returned.
     * @return the {@link Data} node for the passed in name and version.
     */
    private Data getOrCreateDataForVersion(String entityName, String version, boolean createIfNull){
        Map<String, Data> versionedData = getOrCreateVersionedData(entityName, createIfNull);
        if(versionedData.get(version) == null){
            if(createIfNull){
                versionedData.put(version, new Data());
            }
            else{
                return null;
            }
        }
        return versionedData.get(version);
    }

    /**
     * Gets or creates the mapped data for the passed in name
     * @param entityName - entity name
     * @param createIfNull - <code>true</code> if a new map should be created and returned,
     * otherwise <code>false</code> and <code>null</code> will be returned.
     * @return the mapped data for the passed in entity name.
     */
    private Map<String, Data> getOrCreateVersionedData(String entityName, boolean createIfNull){
        EntityInfo info = entityInfoMap.get(entityName);
        if(info == null){
            throw new IllegalStateException("EntityInfo has not yet been set.");
        }

        if (dataMap.get(info) == null){
            if(createIfNull){
                dataMap.put(info, new HashMap<String, Data>());
            }
            else{
                return null;
            }
        }
        return dataMap.get(info);
    }

    private static class Data implements Serializable {
        private Response dependencies;
        private Response access;
        private EntityMetadata entityMetadata;

        public Response getDependency() {
            return dependencies;
        }

        public void setDependencies(Response dependencies) {
            this.dependencies = dependencies;
        }

        public Response getAccess() {
            return access;
        }

        public void setAccess(Response access) {
            this.access = access;
        }

        public EntityMetadata getEntityMetadata() {
            return entityMetadata;
        }

        public void setEntityMetadata(EntityMetadata entityMetadata) {
            this.entityMetadata = entityMetadata;
        }
    }

}
