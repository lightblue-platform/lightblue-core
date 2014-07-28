package com.redhat.lightblue.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.redhat.lightblue.util.Error;

/**
 * Basic implementation of MetadataConfiguration that handles common
 * configuration needs.
 *
 * @author nmalik
 */
public abstract class AbstractMetadataConfiguration implements MetadataConfiguration {

    private final List<HookConfigurationParser> hookConfigurationParsers = new ArrayList<>();
    
    /**
     * Register any common bits with the given Extensions instance.
     */
    protected void registerWithExtensions(Extensions ext) {
        for (HookConfigurationParser parser: hookConfigurationParsers) {
            ext.registerHookConfigurationParser(parser.getName(), parser);
        }
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
                    Object o = null;
                    try {
                        o = Class.forName(clazz).newInstance();
                    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, ex.getMessage());
                    }

                    // add to list or fail
                    if (o instanceof HookConfigurationParser) {
                        hookConfigurationParsers.add((HookConfigurationParser) o);
                    } else {
                        throw Error.get(MetadataConstants.ERR_CONFIG_NOT_VALID, "Class not instance of HookConfigurationParser: " + clazz);
                    }
                }
            }
        }
    }
}
