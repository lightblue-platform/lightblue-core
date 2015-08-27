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
 * <p>
 * Testing harness for junit tests that need to stand an in-memory instance of
 * lightblue. The assumption is made that one {@link LightblueFactory} will be
 * used per test suite.</p>
 * <p>
 * For initialization code that needs to be executed prior to the
 * {@link #AbstractCRUDTestController()}, use the {@literal}@BeforeClass
 * annotation.
 * <b>NOTE:</b> This does not use the rest layer.
 *
 * <p>
 * <b>Example Usage:</b><br>
 * <code>
 * Response response = lightblueFactory.getMediator().insert(
 * createRequest_FromResource(InsertionRequest.class, "./path/to/insert/metadata.json"));
 * </code></p>
 *
 * @author dcrissman
 */
public abstract class AbstractCRUDTestController {

    private static LightblueFactory lightblueFactory;

    @AfterClass
    public static void cleanup() {
        lightblueFactory = null;
    }

    /**
     * @return the lightblueFactory
     */
    protected static LightblueFactory getLightblueFactory() {
        return lightblueFactory;
    }

    /**
     * Defaults to statically loading lightblue.
     */
    public AbstractCRUDTestController() throws Exception {
        this(true);
    }

    /**
     * Creates an instance of the {@link LightblueFactory}.
     *
     * @param loadStatically - <code>true</code> loads lightblue statically for the duration of the suite,
     * otherwise <code>false</code> will load lightblue for each test.
     * @return an instance of {@link LightblueFactory}.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public AbstractCRUDTestController(boolean loadStatically) throws Exception {
        if (!loadStatically || lightblueFactory == null) {
            lightblueFactory = new LightblueFactory(
                    new DataSourcesConfiguration(getDatasourcesJson()),
                    getLightblueCrudJson(),
                    getLightblueMetadataJson());

            JsonTranslator tx = lightblueFactory.getJsonTranslator();

            Metadata metadata = lightblueFactory.getMetadata();
            for (JsonNode metadataJson : getMetadataJsonNodes()) {
                metadata.createNewMetadata(tx.parse(EntityMetadata.class, metadataJson));
            }
        }
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-crud.json. If not set by a subclass, then the default settings will be used.
     * @return the {@link JsonNode} to use when configuring the lightblue crud controllers, or <code>null</code> to use
     * the default {@link LightblueFactory} setting.
     */
    protected JsonNode getLightblueCrudJson() throws Exception {
        return null;
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-metadata.json. If not set by a subclass, then the default settings will be used.
     * @return the {@link JsonNode} to use when configuring the lightblue metadata, or <code>null</code> to use
     * the default {@link LightblueFactory} setting.
     */
    protected JsonNode getLightblueMetadataJson() throws Exception {
        return null;
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-datasources.json.
     *
     * @return the resource name for the json file that contains the datasources. If null,
     * embedded configuration is used.
     */
    protected abstract JsonNode getDatasourcesJson() throws Exception;

    /**
     * Create and returns an array of {@link JsonNode}s from which to load the
     * {@link LightblueFactory} with. These {@link EntityMetadata} instances
     * will be available to all tests in the suite.
     *
     * @return an array of {@link JsonNode}s from which to load the
     * {@link LightblueFactory} with.
     */
    protected abstract JsonNode[] getMetadataJsonNodes() throws Exception;

    /**
     * Creates and returns a {@link Request} based on the passed in
     * <code>jsonFile</code>.
     *
     * @param type - Request class to instantiate.
     * @param jsonFile - File containing metadata json
     * @return a {@link Request} based on the passed in <code>jsonFile</code>.
     * @throws IOException
     */
    protected static <T extends Request> T createRequest_FromResource(Class<T> type, String jsonFile)
            throws IOException {
        return createRequest(type, loadJsonNode(jsonFile));
    }

    /**
     * Creates and returns a {@link Request} based on the passed in
     * <code>jsonString</code>.
     *
     * @param type - Request class to instantiate.
     * @param jsonString - String of metadata json
     * @return a {@link Request} based on the passed in <code>jsonString</code>.
     * @throws IOException
     */
    protected static <T extends Request> T createRequest_FromJsonString(Class<T> type, String jsonString)
            throws IOException {
        return createRequest(type, json(jsonString, true));
    }

    /**
     * Creates and returns a {@link Request} based on the passed in
     * {@link JsonNode}
     *
     * @param type - Request class to instantiate.
     * @param node - {@link JsonNode} of actions metadata
     * @return a {@link Request} based on the passed in {@link JsonNode}
     */
    protected static <T extends Request> T createRequest(Class<T> type, JsonNode node) {
        JsonTranslator tx = getLightblueFactory().getJsonTranslator();
        return tx.parse(type, node);
    }

}
