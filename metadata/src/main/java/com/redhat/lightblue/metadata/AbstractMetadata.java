/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nmalik
 */
public abstract class AbstractMetadata implements Metadata {
    public static final String SEMVER_REGEX = "^\\d+\\.\\d+\\.\\d+(-.*)?$";

    /**
     * Checks that the given version exists, raises an error if it does not.
     *
     * @param version
     * @return true if the version exists
     */
    protected abstract boolean checkVersionExists(String entityName, String version);

    protected abstract void checkDataStoreIsValid(EntityInfo md);

    /**
     * Checks that the default version on the EntityInfo exists. If no default version is set then has no side effect.
     * If the default version does not exist an error is raised.
     *
     * @param ei
     */
    protected final void validateDefaultVersion(EntityInfo ei) {
        if (ei.getDefaultVersion() != null) {
            if (!checkVersionExists(ei.getName(), ei.getDefaultVersion())) {
                throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_INVALID_DEFAULT_VERSION, ei.getName() + ":" + ei.getDefaultVersion());
            }
        }
    }

    protected final Version checkVersionIsValid(EntityMetadata md) {
        return checkVersionIsValid(md.getEntitySchema().getVersion());
    }

    protected final Version checkVersionIsValid(EntitySchema md) {
        return checkVersionIsValid(md.getVersion());
    }

    protected final Version checkVersionIsValid(Version ver) {
        if (ver == null || ver.getValue() == null || ver.getValue().length() == 0) {
            throw new IllegalArgumentException(MetadataConstants.ERR_INVALID_VERSION);
        }
        String value = ver.getValue();
        if (!value.matches(SEMVER_REGEX)) {
            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_INVALID_VERSION_NUMBER, ver.getValue());
        }
        return ver;
    }

    protected final void checkMetadataHasName(EntityMetadata md) {
        checkMetadataHasName(md.getEntityInfo());
    }

    protected final void checkMetadataHasName(EntityInfo md) {
        if (md.getName() == null || md.getName().length() == 0) {
            throw new IllegalArgumentException(MetadataConstants.ERR_EMPTY_METADATA_NAME);
        }
    }

    protected final void checkMetadataHasFields(EntityMetadata md) {
        checkMetadataHasFields(md.getEntitySchema());
    }

    protected final void checkDataStoreIsValid(EntityMetadata md) {
        checkDataStoreIsValid(md.getEntityInfo());
    }

    protected final void checkMetadataHasFields(EntitySchema md) {
        if (md.getFields().getNumChildren() <= 0) {
            throw new IllegalArgumentException(MetadataConstants.ERR_METADATA_WITH_NO_FIELDS);
        }
    }

    /**
     * Add roles and paths to accessMap where accessMap = <role, <operation, List<path>>>
     *
     * @param roles
     * @param operation
     * @param path
     * @param accessMap
     */
    protected final void helperAddRoles(Collection<String> roles, String operation, String path, Map<String, Map<String, List<String>>> accessMap) {
        for (String role : roles) {
            if (!accessMap.containsKey(role)) {
                accessMap.put(role, new HashMap<String, List<String>>());
            }
            if (!accessMap.get(role).containsKey(operation)) {
                accessMap.get(role).put(operation, new ArrayList<String>());
            }
            accessMap.get(role).get(operation).add(path);
        }
    }
}
