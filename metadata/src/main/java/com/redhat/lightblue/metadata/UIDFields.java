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
package com.redhat.lightblue.metadata;

import java.util.List;

import com.redhat.lightblue.metadata.constraints.IdentityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.JsonNodeCursor;
import com.redhat.lightblue.util.KeyValueCursor;

import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.types.UIDType;

/**
 * Initializes UID fields based on required/not-required status, and whether
 * they're already initialized or not.
 *
 * <ul>
 * <li>If a UID field is required, it is inserted into the document if it is not
 * already there.</li>
 * <li>If a UID field is not required and if it exists in the document with null
 * or empty content, it is initialized. Otherwise, it is not changed.</li>
 * </ul>
 */
public final class UIDFields {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIDFields.class);

    public static void initializeUIDFields(JsonNodeFactory factory, EntityMetadata md, JsonDoc doc) {
        FieldCursor cursor = md.getFieldCursor();
        while (cursor.next()) {
            FieldTreeNode node = cursor.getCurrentNode();
            // Process all UID fields
            if (node.getType().equals(UIDType.TYPE)) {
                Path p = cursor.getCurrentPath();
                LOGGER.debug("Processing UID field {}", p);
                if (node instanceof Field && required((Field) node)) {
                    LOGGER.debug("Field {} is required", p);
                    setRequiredField(factory, doc, p, 1, null);
                } else {
                    // Here, node could be a field or an array
                    LOGGER.debug("Field {} is not required", p);
                    KeyValueCursor<Path, JsonNode> nodeCursor = doc.getAllNodes(p);
                    while (nodeCursor.hasNext()) {
                        nodeCursor.next();
                        JsonNode valueNode = nodeCursor.getCurrentValue();
                        if (valueNode.isNull() || valueNode.asText().length() == 0) {
                            String value = UIDType.newValue();
                            LOGGER.debug("Setting {} to {}", nodeCursor.getCurrentKey(), value);
                            doc.modify(nodeCursor.getCurrentKey(), factory.textNode(value), true);
                        }
                    }
                }
            }
        }
    }

    private static void setRequiredField(JsonNodeFactory factory,
                                         JsonDoc doc,
                                         Path fieldPath,
                                         int startSegment,
                                         Path resolvedPath) {
        LOGGER.debug("setRequiredField: fieldPath:{} startSegment:{} resolvedPath:{}", fieldPath, startSegment, resolvedPath);
        int nSegments = fieldPath.numSegments();
        boolean array = false;
        for (int segment = startSegment; segment < nSegments; segment++) {
            if (fieldPath.head(segment).equals(Path.ANY)) {
                array = true;
                MutablePath arrPath = new MutablePath(fieldPath.prefix(segment));
                if (resolvedPath != null) {
                    arrPath.rewriteIndexes(resolvedPath);
                }
                LOGGER.debug("Processing segment {}", arrPath);
                JsonNode node = doc.get(arrPath);
                if (node != null) {
                    int size = node.size();
                    LOGGER.debug("{} size={}", arrPath, size);
                    arrPath.push(0);
                    for (int i = 0; i < size; i++) {
                        arrPath.setLast(i);
                        setRequiredField(factory, doc, fieldPath, segment + 1, arrPath.immutableCopy());
                    }
                }
                break;
            }
        }
        if (!array) {
            Path p;
            if (resolvedPath == null) {
                p = fieldPath;
            } else {
                p = new MutablePath(fieldPath).rewriteIndexes(resolvedPath);
            }
            LOGGER.debug("Setting {}", p);
            JsonNode valueNode = doc.get(p);
            if (valueNode == null || valueNode.isNull() || valueNode.asText().length() == 0) {
                String value = UIDType.newValue();
                LOGGER.debug("Setting {} to {}", p, value);
                doc.modify(p, factory.textNode(value), true);
            }
        }
    }

    private static boolean required(Field f) {
        List<FieldConstraint> constraints = f.getConstraints();
        if (constraints != null) {
            for (FieldConstraint c : constraints) {
                if (c instanceof RequiredConstraint) {
                    return ((RequiredConstraint) c).getValue();
                } else if (c instanceof IdentityConstraint) {
                    return ((IdentityConstraint) c).isValidForFieldType(f.getType());
                }
            }
        }
        return false;
    }

    private UIDFields() {
    }
}
