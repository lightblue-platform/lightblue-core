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
package com.redhat.lightblue.config;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.MetadataRole;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;
import com.redhat.lightblue.metadata.parser.PropertyParser;
import com.redhat.lightblue.util.Error;

/**
 * Basic implementation of MetadataConfiguration that handles common
 * configuration needs.
 *
 * @author nmalik
 */
public abstract class AbstractMetadataConfiguration implements MetadataConfiguration {

    private final List<HookConfigurationParser> hookConfigurationParsers = new ArrayList<>();
    private final List<Map.Entry<String, DataStoreParser>> backendParsers = new ArrayList<>();
    private final List<Map.Entry<String, PropertyParser>> propertyParsers = new ArrayList<>();
    private final Map<MetadataRole, List<String>> roleMap = new HashMap<>();
    private boolean validateRequests = false;

    @Override
    public boolean isValidateRequests() {
        return validateRequests;
    }

    public void setValidateRequests(boolean b) {
        validateRequests = b;
    }

    /**
     * Register any common bits with the given Extensions instance.
     */
    protected void registerWithExtensions(Extensions ext) {
        for (HookConfigurationParser parser : hookConfigurationParsers) {
            ext.registerHookConfigurationParser(parser.getName(), parser);
        }
        for (Map.Entry<String, DataStoreParser> parser : backendParsers) {
            ext.registerDataStoreParser(parser.getKey(), parser.getValue());
        }
        for (Map.Entry<String, PropertyParser> parser : propertyParsers) {
            ext.registerPropertyParser(parser.getKey(), parser.getValue());
        }
    }

    protected Map<MetadataRole, List<String>> getMappedRoles() {
        return roleMap;
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("hookConfigurationParsers");
            if (x != null && x.isArray()) {
                // each element in array is a class
                Iterator<JsonNode> elements = ((ArrayNode) x).elements();

                while (elements.hasNext()) {
                    JsonNode e = elements.next();
                    String clazz = e.asText();

                    // instantiate the class
                    Object o;
                    try {
                        o = Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance();
                    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, ex);
                    }

                    // add to list or fail
                    if (o instanceof HookConfigurationParser) {
                        hookConfigurationParsers.add((HookConfigurationParser) o);
                    } else {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "Class not instance of HookConfigurationParser: " + clazz);
                    }
                }
            }

            ArrayNode backendParsersJs = (ArrayNode) node.get("backendParsers");
            if (backendParsersJs != null) {
                Iterator<JsonNode> bpjsItr = backendParsersJs.iterator();
                while (bpjsItr.hasNext()) {
                    JsonNode jsonNode = bpjsItr.next();
                    String name = jsonNode.get("name").asText();
                    String clazz = jsonNode.get("clazz").asText();

                    if (name == null || clazz == null) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "Backend/DataStoreParser class was not informed: name='" + name + "' clazz=" + clazz);
                    }

                    try {
                        DataStoreParser instance = (DataStoreParser) Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance();
                        AbstractMap.SimpleEntry<String, DataStoreParser> stringDataStoreParserSimpleEntry = new AbstractMap.SimpleEntry<>(name, instance);
                        backendParsers.add(stringDataStoreParserSimpleEntry);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "Class not instance of Backend/DataStoreParser: " + clazz);
                    }
                }
            }

            ArrayNode propertyParserJs = (ArrayNode) node.get("propertyParsers");
            if (propertyParserJs != null) {
                for (JsonNode jsonNode : propertyParserJs) {
                    String name = jsonNode.get("name").asText();
                    String clazz = jsonNode.get("clazz").asText();

                    if (name == null || clazz == null) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "PropertyParser Name/Class not informed: name=" + name + " clazz=" + clazz);
                    }

                    try {
                        PropertyParser instance = (PropertyParser) Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance();

                        AbstractMap.SimpleEntry<String, PropertyParser> stringPropertyParserSimpleEntry = new AbstractMap.SimpleEntry<>(name, instance);
                        propertyParsers.add(stringPropertyParserSimpleEntry);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "Class not instance of PropertyParser: " + clazz);
                    }
                }
            }

            JsonNode roleMapJs = node.get("roleMap");
            if (roleMapJs != null) {
                // If the roleMap element is defined, it is expected to have all the roles mapped
                MetadataRole[] values = MetadataRole.values();
                for (MetadataRole value : values) {
                    String name = value.toString();
                    ArrayNode rolesJs = (ArrayNode) roleMapJs.get(name);

                    if (rolesJs == null || rolesJs.size() == 0) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "roleMap missing the role \"" + name + "\"");
                    }

                    roleMap.put(value, new ArrayList<String>());
                    for (JsonNode jsonNode : rolesJs) {
                        roleMap.get(value).add(jsonNode.textValue());
                    }
                }
            }

            x = node.get("validateRequests");
            if (x != null) {
                validateRequests = x.booleanValue();
            }
        }
    }

    @Override
    public List<HookConfigurationParser> getHookConfigurationParsers() {
        return hookConfigurationParsers;
    }
}
