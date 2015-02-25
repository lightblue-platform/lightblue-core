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

import org.junit.AfterClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.config.DataSourcesConfiguration;
import com.redhat.lightblue.config.JsonTranslator;
import com.redhat.lightblue.config.LightblueFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;

/**
 * Testing harness for junit tests that need to stand an in-memory instance of lightblue.<br>
 * <b>NOTE:</b> This does not use the rest layer.
 *
 * <p>
 * Example Usage:<br>
 * <code>
 * Response response = lightblueFactory.getMediator().insert(
 * createRequest_FromResource(InsertionRequest.class, "./path/to/insert/metadata.json"));
 * </code></p>
 *
 * @author dcrissman
 */
public abstract class AbstractCRUDTestController {

    protected static LightblueFactory lightblueFactory;

    @AfterClass
    public static void cleanup(){
        lightblueFactory = null;
    }

    /**
     * Creates an instance of the {@link LightblueFactory}.<br>
     * <b>Must be called from implementations in a static BeforeClass method</b>
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
    protected static LightblueFactory createLightblueFactory(String datasourcesResourcePath, String... metadataResourcePaths)
            throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        lightblueFactory = new LightblueFactory(
                new DataSourcesConfiguration(loadJsonNode(datasourcesResourcePath)));

        JsonTranslator tx = lightblueFactory.getJsonTranslator();

        Metadata metadata = lightblueFactory.getMetadata();
        for(String metadataResourcePath : metadataResourcePaths){
            metadata.createNewMetadata(tx.parse(EntityMetadata.class, loadJsonNode(metadataResourcePath)));
        }

        return lightblueFactory;
    }

    /**
     * Creates and returns a {@link Request} based on the passed in <code>jsonFile</code>.
     * @param type - Request class to instantiate.
     * @param jsonFile - File containing metadata json
     * @return a {@link Request} based on the passed in <code>jsonFile</code>.
     * @throws IOException
     */
    protected static <T extends Request> T createRequest_FromResource(Class<T> type, String jsonFile)
            throws IOException{
        return createRequest(type, loadJsonNode(jsonFile));
    }

    /**
     * Creates and returns a {@link Request} based on the passed in <code>jsonString</code>.
     * @param type - Request class to instantiate.
     * @param jsonString - String of metadata json
     * @return a {@link Request} based on the passed in <code>jsonString</code>.
     * @throws IOException
     */
    protected static <T extends Request> T createRequest_FromJsonString(Class<T> type, String jsonString)
            throws IOException{
        return createRequest(type, json(jsonString));
    }

    /**
     * Creates and returns a {@link Request} based on the passed in {@link JsonNode}
     * @param type - Request class to instantiate.
     * @param node - {@link JsonNode} of actions metadata
     * @return a {@link Request} based on the passed in {@link JsonNode}
     */
    protected static <T extends Request> T createRequest(Class<T> type, JsonNode node){
        JsonTranslator tx = lightblueFactory.getJsonTranslator();
        return tx.parse(type, node);
    }

}
