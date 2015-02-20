/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.test;

import static com.redhat.lightblue.util.JsonUtils.json;
import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;

public final class LightblueUtil {

    /**
     * Creates an instance of the {@link LightblueFactory}.
     * @param datasourcesResourcePath - path to datasources.json
     * @param metadataResourcePaths - path to any and all *-metadata.json you'd like {@link LightblueFactory} to know about
     * @return an instance of {@link LightblueFactory}.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static LightblueFactory createLightblueFactory(String datasourcesResourcePath, String... metadataResourcePaths)
            throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        LightblueFactory lightblueFactory = new LightblueFactory(
                new DataSourcesConfiguration(loadJsonNode(datasourcesResourcePath)));

        JsonTranslator tx = lightblueFactory.getJsonTranslator();

        Metadata metadata = lightblueFactory.getMetadata();
        for(String metadataResourcePath : metadataResourcePaths){
            metadata.createNewMetadata(tx.parse(EntityMetadata.class, loadJsonNode(metadataResourcePath)));
        }

        return lightblueFactory;
    }

    public static <T extends Request> T createRequest_FromResource(JsonTranslator tx, Class<T> type, String jsonFile)
            throws IOException{
        return createRequest(tx, type, loadJsonNode(jsonFile));
    }

    public static <T extends Request> T createRequest_FromJsonString(JsonTranslator tx, Class<T> type, String jsonString)
            throws IOException{
        return createRequest(tx, type, json(jsonString));
    }

    public static <T extends Request> T createRequest(JsonTranslator tx, Class<T> type, JsonNode node){
        return tx.parse(type, node);
    }

    private LightblueUtil(){}
}
