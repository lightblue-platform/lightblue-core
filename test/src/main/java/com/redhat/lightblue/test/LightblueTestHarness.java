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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.AfterClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
public abstract class LightblueTestHarness {

    public static final String REMOVE_ALL_HOOKS = "ALL";

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
    public LightblueTestHarness() throws Exception {
        this(true);
    }

    /**
     * Creates an instance of the {@link LightblueFactory}.
     *
     * @param loadStatically - <code>true</code> loads lightblue statically for
     * the duration of the suite, otherwise <code>false</code> will load
     * lightblue for each test.
     * @return an instance of {@link LightblueFactory}.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public LightblueTestHarness(boolean loadStatically) throws Exception {
        if (!loadStatically || lightblueFactory == null) {
            lightblueFactory = new LightblueFactory(
                    new DataSourcesConfiguration(getDatasourcesJson()),
                    getLightblueCrudJson(),
                    getLightblueMetadataJson());

            JsonTranslator tx = lightblueFactory.getJsonTranslator();

            Metadata metadata = lightblueFactory.getMetadata();

            String datasource = getDatasource();
            JsonNode[] metadataNodes = getMetadataJsonNodes();
            if (metadataNodes != null) {
                for (JsonNode metadataJson : metadataNodes) {
                    stripHooks(metadataJson, getHooksToRemove());
                    if (datasource != null) {
                        ensureDatasource(metadataJson, datasource);
                    }
                    if (isGrantAnyoneAccess()) {
                        grantAnyoneAccess(metadataJson);
                    }

                    EntityMetadata entityMetadata = tx.parse(EntityMetadata.class, metadataJson);

                    //TODO: Handle versions and the latest's entityInfo winning. Versions are a string and do not have to follow a format.
                    /*
                     * If entityInfo already exists, lightblue will throw a duplicate exception.
                     * No need to handle here.
                     */
                    if (metadata.getEntityInfo(entityMetadata.getName()) == null) {
                        metadata.createNewMetadata(entityMetadata);
                    }
                    else {
                        metadata.createNewSchema(entityMetadata);
                    }
                }
            }
        }
    }

    /**
     * Deep dives to set all access levels to 'anyone'.
     *
     * @param node - root {@link JsonNode}
     */
    public static void grantAnyoneAccess(JsonNode node) {
        doGrantAnyoneAccess(node.get("schema"));
    }

    /**
     * <p>
     * Recursive Method!!</p>
     * <p>
     * Iterates over the {@link JsonNode} to determine if it, or any of it's
     * sub-documents, has an access node. If so, all access settings will be
     * changed to 'anyone'</p>
     *
     * @param node - {@link JsonNode} to set the access to anyone on.
     */
    private static void doGrantAnyoneAccess(JsonNode node) {
        if (node.has("fields")) {
            JsonNode fieldsNode = node.get("fields");
            Iterator<JsonNode> fieldNodes = fieldsNode.iterator();
            while (fieldNodes.hasNext()) {
                doGrantAnyoneAccess(fieldNodes.next());
            }
        }

        if (node.has("items")) {
            doGrantAnyoneAccess(node.get("items"));
        }

        if (node.has("access")) {
            JsonNode accessNode = node.get("access");
            Iterator<JsonNode> accessNodes = accessNode.iterator();
            while (accessNodes.hasNext()) {
                ArrayNode child = (ArrayNode) accessNodes.next();
                child.removeAll();
                child.add("anyone");
            }
        }
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-crud.json. If not set by a subclass, then the default
     * settings will be used.
     *
     * @return the {@link JsonNode} to use when configuring the lightblue crud
     * controllers, or <code>null</code> to use the default
     * {@link LightblueFactory} setting.
     */
    protected JsonNode getLightblueCrudJson() throws Exception {
        return null;
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-metadata.json. If not set by a subclass, then the
     * default settings will be used.
     *
     * @return the {@link JsonNode} to use when configuring the lightblue
     * metadata, or <code>null</code> to use the default
     * {@link LightblueFactory} setting.
     */
    protected JsonNode getLightblueMetadataJson() throws Exception {
        return null;
    }

    /**
     * Creates and returns an instance of {@link JsonNode} that represents the
     * relevant lightblue-datasources.json.
     *
     * @return the resource name for the json file that contains the
     * datasources. If null, embedded configuration is used.
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

    public static void ensureDatasource(JsonNode node, String datasource) {
        ObjectNode datastoreNode = (ObjectNode) node.get("entityInfo").get("datastore");
        datastoreNode.replace("datasource", new TextNode(datasource));
    }

    /**
     * Override to set the datasource values for all metadata to the returned
     * value of this method. By default <code>null</code> will be returned
     * disabling this feature.
     *
     * @return String name of datasource to use in all metadata.
     */
    protected String getDatasource() {
        return null;
    }

    /**
     * Strips out the specified hooks. Pass in 'ALL' or
     * {@link #REMOVE_ALL_HOOKS} to remove all the hooks on the node.
     *
     * @param node - root {@link JsonNode}
     * @param hooksToRemove - {@link Set} of hook names to remove from
     * entityInfo.
     */
    public static void stripHooks(JsonNode node, Set<String> hooksToRemove) {
        ObjectNode entityInfoNode = (ObjectNode) node.get("entityInfo");

        if (!entityInfoNode.has("hooks")) {
            return;
        }

        if (hooksToRemove == null) {
            return;
        } else if (hooksToRemove.contains(REMOVE_ALL_HOOKS)) {
            entityInfoNode.remove("hooks");
        } else {
            ArrayNode hooksNode = (ArrayNode) entityInfoNode.get("hooks");
            for (int x = hooksNode.size(); x > 0; x--) {
                JsonNode hookNode = hooksNode.get(x - 1);
                if (hooksToRemove.contains(hookNode.get("name").textValue())) {
                    hooksNode.remove(x - 1);
                }
            }
        }
    }

    /**
     * @return {@link Set} of hook names to remove. By default returns
     * {@link #REMOVE_ALL_HOOKS}, which will remove all hooks.
     */
    public Set<String> getHooksToRemove() {
        return new HashSet<>(Arrays.asList(REMOVE_ALL_HOOKS));
    }

    /**
     * @return <code>true</code> if access settings on metadata should be
     * altered to 'anyone', otherwise <code>false</code>. Defaults to
     * <code>true</code>.
     */
    public boolean isGrantAnyoneAccess() {
        return true;
    }

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
