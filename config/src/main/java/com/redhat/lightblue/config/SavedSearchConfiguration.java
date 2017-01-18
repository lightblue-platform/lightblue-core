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

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonInitializable;

/**
 * Saves search configuration
 * <ul>
 *   <li>entity: Saved search entity name</li>
 *   <li>entityVersion: The saved search entity version, or null if default</li>
 *   <li>cacheConfig: Guava cache spec @see <a href="http://google.github.io/guava/releases/21.0/api/docs/com/google/common/cache/CacheBuilderSpec.html"> </li>
 * </ul>
 */
public class SavedSearchConfiguration implements JsonInitializable, Serializable {

    private static final long serialVersionUID = 1l;

    private String entity="savedSearch";
    private String entityVersion;
    private String cacheConfig;

    public String getEntity() {
        return entity;
    }

    public String getEntityVersion() {
        return entityVersion;
    }

    public String getCacheConfig() {
        return cacheConfig;
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("entity");
            if(x!=null) {
                entity=x.asText();
                x=node.get("entityVersion");
                if(x!=null)
                    entityVersion=x.asText();
            }
            
            x=node.get("cache");
            if(x!=null) {
                cacheConfig=x.asText();
            }
        }
    }
}
