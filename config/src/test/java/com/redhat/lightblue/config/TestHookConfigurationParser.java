package com.redhat.lightblue.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

/**
 * Dummy hook config parser for testing.
 * 
 * @author nmalik
 */
public class TestHookConfigurationParser implements HookConfigurationParser<JsonNode>{

    public static final String HOOK_NAME = "testHook";
    
    @Override
    public String getName() {
        return HOOK_NAME;
    }

    @Override
    public HookConfiguration parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, HookConfiguration object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
