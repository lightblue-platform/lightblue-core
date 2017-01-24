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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.JsonObject;

/**
 * Contains result metadata returned for each document in the resultset
 */
public class ResultMetadata extends JsonObject {

    /**
     * Metadata property denoting the field should receive its contents from result metadata.
     * The field must be an object or any, and it is not persisted.
     */
    public static final String MD_PROPERTY_RESULT_METADATA="resultMetadata";

    /**
     * Metadata property denoting the field should receive its contents from document version
     * The field must be a string, and it is not persisted.
     */
    public static final String MD_PROPERTY_DOCVER="documentVersion";

    private String documentVersion;

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String s) {
        documentVersion=s;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=JsonNodeFactory.instance.objectNode();
        node.set("documentVersion",documentVersion==null?JsonNodeFactory.instance.nullNode():
                 JsonNodeFactory.instance.textNode(documentVersion));
        return node;
    }    
}

