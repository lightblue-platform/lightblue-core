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

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.parser.HookConfigurationParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

/**
 * Dummy hook config parser for testing.
 *
 * @author nmalik
 */
public class TestHookConfigurationParser implements HookConfigurationParser<JsonNode> {

    public static final String HOOK_NAME = "testHook";

    @Override
    public String getName() {
        return HOOK_NAME;
    }

    @Override
    public CRUDHook getCRUDHook() {
        return null;
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
