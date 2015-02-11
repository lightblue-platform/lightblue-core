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
package com.redhat.lightblue.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.metadata.constraints.IdentityConstraint;
import com.redhat.lightblue.metadata.constraints.MatchesConstraint;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.util.Path;

import java.util.*;

/**
 *
 * @author nmalik
 */
public abstract class AbstractMetadata implements Metadata {
    public static final String SEMVER_REGEX = "^\\d+\\.\\d+\\.\\d+(-.*)?$";

    protected Map<MetadataRoles, List<String>> roleMap;

    /**
     * Checks that the given version exists, raises an error if it does not.
     *
     * @param version
     * @return true if the version exists
     */
    protected abstract boolean checkVersionExists(String entityName, String version);

    protected abstract void checkDataStoreIsValid(EntityInfo md);

    /**
     * Checks that the default version on the EntityInfo exists. If no default
     * version is set then has no side effect. If the default version does not
     * exist an error is raised.
     *
     * @param ei
     */
    protected final void validateDefaultVersion(EntityInfo ei) {
        if (ei.getDefaultVersion() != null && !checkVersionExists(ei.getName(), ei.getDefaultVersion())) {
            throw com.redhat.lightblue.util.Error.get(MetadataConstants.ERR_INVALID_DEFAULT_VERSION, ei.getName() + ":" + ei.getDefaultVersion());
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
     * Add roles and paths to accessMap where accessMap = <role, <operation,
     * List<path>>>
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

    @Override
    public Map<MetadataRoles, List<String>> getMappedRoles() {
        return roleMap;
    }

    public void setRoleMap(Map<MetadataRoles, List<String>> roleMap) {
        this.roleMap = roleMap;
    }

    @Override
    public JsonNode getJSONSchema(String entityName, String version) {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        ObjectNode propertiesNode = new ObjectNode(JsonNodeFactory.instance);
        //The comments can be useful if this feature change to look like the "schema"'s json-schema
        //ObjectNode versionNode = new ObjectNode(JsonNodeFactory.instance);
        //ObjectNode statusNode = new ObjectNode(JsonNodeFactory.instance);
        EntityMetadata entityMetadata = getEntityMetadata(entityName, version);
        FieldTreeNode fieldTreeRoot = entityMetadata.getEntitySchema().getFieldTreeRoot();

        jsonNode.set("$schema", TextNode.valueOf("http://json-schema.org/draft-04/schema#"));
        jsonNode.set("type", TextNode.valueOf("object"));
        //jsonNode.set("name", TextNode.valueOf(entityMetadata.getEntitySchema().getName()));
        jsonNode.set("description", TextNode.valueOf(String.format("JSON schema for entity '%s' version '%s'", entityName, version)));
        //versionNode.set("value", TextNode.valueOf(entityMetadata.getEntitySchema().getVersion().getValue()));
        //versionNode.set("changelog", TextNode.valueOf(entityMetadata.getEntitySchema().getVersion().getChangelog()));
        //jsonNode.set("version", versionNode);
        //statusNode.set("value", TextNode.valueOf(entityMetadata.getEntitySchema().getStatus().name()));
        //jsonNode.set("status", statusNode);
        if(fieldTreeRoot.hasChildren()) {
            jsonNode.set("properties", propertiesNode);
            buildJsonNodeSchema(propertiesNode, fieldTreeRoot);
        }
        // Probably it would be helpful to have enums defined into the schema
        /*
           "enums": [
      {
        "name": "site_type_enum",
        "values": [
          "billing",
          "marketing",
          "service",
          "shipping"
        ]
      }
    ],
         */
        Enums enums = entityMetadata.getEntityInfo().getEnums();
        if(enums != null && !enums.isEmpty()) {
            ArrayNode enumsNode = new ArrayNode(JsonNodeFactory.instance);
            Set<String> keys = enums.getEnums().keySet();
            for (String key : keys) {
                Enum anEnum = enums.getEnum(key);
                ObjectNode enumNode = new ObjectNode(JsonNodeFactory.instance);
                ArrayNode valuesNode = new ArrayNode(JsonNodeFactory.instance);
                enumNode.set("name", TextNode.valueOf(anEnum.getName()));
                Iterator<String> iterator = anEnum.getValues().iterator();
                while(iterator.hasNext()){
                    String next = iterator.next();
                    valuesNode.add(next);
                }

                enumNode.set("values", valuesNode);

                enumsNode.add(enumNode);
            }
            jsonNode.set("enums", enumsNode);
        }



        return jsonNode;
    }

    private ArrayNode getRequiredFieldsArrayNode(EntityMetadata entityMetadata) {
        ArrayNode value = new ArrayNode(JsonNodeFactory.instance);
        Field[] requiredFields = entityMetadata.getEntitySchema().getRequiredFields();
        for (Field requiredField : requiredFields) {
            value.add(TextNode.valueOf(requiredField.getFullPath().toString()));
        }
        return value;
    }

    private void buildJsonNodeSchema(ObjectNode jsonNode, FieldTreeNode fieldTreeRoot) {
        TreeMap<Path, Field> fieldMap = new TreeMap<>();
        Iterator<? extends FieldTreeNode> children = fieldTreeRoot.getChildren();
        Stack<Iterator<? extends FieldTreeNode>> fieldsPending = new Stack<>();
        Stack<ObjectNode> jsonParents = new Stack<>();
        Stack<ArrayNode> requiredJsonParents = new Stack<>();
        ArrayNode requiredJsonNode = new ArrayNode(JsonNodeFactory.instance);
        do{
            FieldTreeNode fieldTreeChild = children.next();
            if (fieldTreeChild instanceof ObjectField) {
                fieldsPending.push(children);
                jsonParents.push(jsonNode);
                requiredJsonParents.push(requiredJsonNode);

                ObjectField of = (ObjectField) fieldTreeChild;
                ObjectNode child = new ObjectNode(JsonNodeFactory.instance);
                jsonNode.set(of.getName(), child);
                child.set("type",  TextNode.valueOf(of.getType().getName()));

                ObjectNode prop = new ObjectNode(JsonNodeFactory.instance);
                child.set("properties",  prop);

                ObjectNode constraintsNode = new ObjectNode(JsonNodeFactory.instance);
                for (FieldConstraint fc : of.getConstraints()) {
                    transformConstraintJsonNode(requiredJsonNode, of.getName(), child, constraintsNode, fc);
                }
                if(constraintsNode.size() > 0 ) {
                    child.set("constraints", constraintsNode);
                }

                jsonParents.push(child);
                jsonNode = prop;
                children = of.getChildren();
                requiredJsonNode = new ArrayNode(JsonNodeFactory.instance);
            }else if (fieldTreeChild instanceof SimpleField) {
                SimpleField sf = (SimpleField) fieldTreeChild;
                ObjectNode json = new ObjectNode(JsonNodeFactory.instance);
                String typeString = sf.getType().getName();
                if("uid".equals(typeString)){
                    typeString = "string";
                }
                json.set("type",  TextNode.valueOf(typeString));
                Object description = sf.getProperties().get("description");
                if(description != null) {
                    json.set("description", TextNode.valueOf(description.toString()));
                }
                ObjectNode constraintsNode = new ObjectNode(JsonNodeFactory.instance);
                for (FieldConstraint fc : sf.getConstraints()) {
                    transformConstraintJsonNode(requiredJsonNode, sf.getName(), json, constraintsNode, fc);
                }
                if(constraintsNode.size() > 0 ) {
                    json.set("constraints", constraintsNode);
                }
                jsonNode.set(sf.getName(),json);
            }
            do {
                if(!children.hasNext()){
                    if(!fieldsPending.empty()){
                        children = fieldsPending.pop();
                        jsonNode = jsonParents.pop();
                        if(requiredJsonNode.size() > 0){
                            ArrayNode required = (ArrayNode)jsonNode.get("required");
                            if(required != null){
                                ArrayNode newNode = joinJsonNodes(requiredJsonNode, required);
                                jsonNode.set("required", newNode);
                            } else {
                                jsonNode.set("required", requiredJsonNode);
                            }
                        }
                        requiredJsonNode = requiredJsonParents.pop();
                        jsonNode = jsonParents.pop();
                    } else {
                        break;
                    }
                }
            } while(!children.hasNext());

        } while (children.hasNext());
        if(requiredJsonNode.size() > 0){
            ArrayNode required = (ArrayNode)jsonNode.get("required");
            if(required != null){
                ArrayNode newNode = joinJsonNodes(requiredJsonNode, required);
                jsonNode.set("required", newNode);
            } else {
                jsonNode.set("required", requiredJsonNode);
            }
        }
    }

    private ArrayNode joinJsonNodes(ArrayNode requiredJsonNode, ArrayNode required) {
        ArrayNode newNode = new ArrayNode(JsonNodeFactory.instance);
        Iterator<JsonNode> iterator;
        Set<String> names = new TreeSet<>();

        iterator = requiredJsonNode.iterator();
        while (iterator.hasNext()) {
            JsonNode next =  iterator.next();
            newNode.add(next);
            names.add(next.asText());
        }
        iterator = required.iterator();
        while (iterator.hasNext()) {
            JsonNode next =  iterator.next();
            if (!names.contains(next.asText())) {
                newNode.add(next);
            }
        }
        return newNode;
    }

    private void transformConstraintJsonNode(ArrayNode requiredJsonNode, String name, ObjectNode json, ObjectNode constraintsNode, FieldConstraint fc) {
        if (fc instanceof IdentityConstraint || fc instanceof RequiredConstraint) {
            requiredJsonNode.add(name);
        } else if (fc instanceof MatchesConstraint) {
            json.set("pattern" , TextNode.valueOf(fc.getDescription()));
        } else {
            constraintsNode.set(fc.getType() , TextNode.valueOf(fc.getDescription()));
        }
    }
}
