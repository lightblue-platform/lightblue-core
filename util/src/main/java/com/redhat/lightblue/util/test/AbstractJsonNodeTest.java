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
package com.redhat.lightblue.util.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.JsonUtils;

public abstract class AbstractJsonNodeTest {
    /**
     * Load resource as json document.
     *
     * @param resourceName
     * @return the root json node
     * @throws IOException
     */
    public static final JsonNode loadJsonNode(String resourceName) throws IOException {
        return JsonUtils.json(resourceName);
    }

    /**
     * Load contents of resource on classpath as String.
     *
     * @param resourceName
     * @return the resource as a String
     * @throws IOException
     */
    public static final String loadResource(String resourceName) throws IOException {
        StringBuilder buff = new StringBuilder();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
                InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buff.append(line).append("\n");
            }
        }

        return buff.toString();
    }
}
