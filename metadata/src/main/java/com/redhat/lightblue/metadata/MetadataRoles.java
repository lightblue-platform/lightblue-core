package com.redhat.lightblue.metadata;

public enum MetadataRoles {
    FIND_DEPENDENCIES("metadata.find.dependencies"),
    FIND_ROLES("metadata.find.roles"),
    FIND_ENTITY_NAMES("metadata.find.entityNames"),
    FIND_ENTITY_VERSIONS("metadata.find.entityVersions"),
    FIND_ENTITY_METADATA("metadata.find.entityMetadata"),
    INSERT("metadata.insert"),
    INSERT_SCHEMA("metadata.insert.schema"),
    UPDATE_ENTITYINFO("metadata.update.entityInfo"),
    UPDATE_ENTITY_SCHEMASTATUS("metadata.update.schemaStatus"),
    UPDATE_DEFAULTVERSION("metadata.update.defaultVersion"),
    DELETE_ENTITY("metadata.delete.entity");

    private final String jsonRepresentation;

    MetadataRoles(String jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    @Override
    public String toString() {
        return this.jsonRepresentation;
    }
}
