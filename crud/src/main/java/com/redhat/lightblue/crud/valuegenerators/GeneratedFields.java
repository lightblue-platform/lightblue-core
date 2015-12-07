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
package com.redhat.lightblue.crud.valuegenerators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.ValueGenerator;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.constraints.IdentityConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Initializes generated fields if they are not already initialized.
 *
 * If a generated field is required but it is not in the document, or
 * it is in the document with a null value, it is inserted into the
 * document and initialized.
 *
 * If a generated field is not required, but it exists in the document
 * with null value, it is initialized.
 *
 */
public final class GeneratedFields {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratedFields.class);

    public static void initializeGeneratedFields(Factory factory, EntityMetadata md, JsonDoc doc) {
        FieldCursor cursor = md.getFieldCursor();
        while (cursor.next()) {
            FieldTreeNode node = cursor.getCurrentNode();
            // Process all generated fields
            if(node instanceof SimpleField) {
                SimpleField field=(SimpleField)node;
                ValueGenerator generator=field.getValueGenerator();
                if(generator!=null) {
                    Path p = cursor.getCurrentPath();
                    LOGGER.debug("Processing generated field {}", p);
                    if (required(field)) {
                        LOGGER.debug("Field {} is required", p);
                        setRequiredField(factory, doc, field, p, 1, null,md,generator.isOverwrite());
                    } else {
                        LOGGER.debug("Field {} is not required", p);
                        KeyValueCursor<Path, JsonNode> nodeCursor = doc.getAllNodes(p);
                        while (nodeCursor.hasNext()) {
                            nodeCursor.next();
                            JsonNode valueNode = nodeCursor.getCurrentValue();
                            if (valueNode.isNull()||generator.isOverwrite()) {
                                JsonNode value=generate(factory,
                                                        field,
                                                        md);
                                LOGGER.debug("Setting {} to {}", nodeCursor.getCurrentKey(), value);
                                doc.modify(nodeCursor.getCurrentKey(), value, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setRequiredField(Factory factory,
                                         JsonDoc doc,
                                         SimpleField field,
                                         Path fieldPath,
                                         int startSegment,
                                         Path resolvedPath,
                                         EntityMetadata md,
                                         boolean overwrite) {
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
                if (node != null&&!(node instanceof NullNode)) {
                    int size = node.size();
                    LOGGER.debug("{} size={}", arrPath, size);
                    arrPath.push(0);
                    for (int i = 0; i < size; i++) {
                        arrPath.setLast(i);
                        setRequiredField(factory, doc, field, fieldPath, segment + 1, arrPath.immutableCopy(),md,overwrite);
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
            // Make sure the parent node exists, and not a null node
            boolean nullParent=false;
            if(p.numSegments()>1) {
                JsonNode parentNode=doc.get(p.prefix(-1));
                if(parentNode==null||parentNode instanceof NullNode)
                    nullParent=true;
            }
            if(!nullParent) {
                LOGGER.debug("Setting {}", p);
                JsonNode valueNode = doc.get(p);
                if (overwrite||(valueNode == null || valueNode.isNull())) {
                    JsonNode value=generate(factory,
                                            field,
                                            md);
                    LOGGER.debug("Setting {} to {}", p, value);
                    doc.modify(p, value, true);
                }
            }
        }
    }

    private static JsonNode generate(Factory factory,
                                     SimpleField field,
                                     EntityMetadata md) {
        ValueGeneratorSupport vgs=factory.getValueGenerator(field.getValueGenerator(),md.getDataStore().getBackend());
        if(vgs==null)
            throw new IllegalArgumentException("Cannot generate value for "+field.getFullPath());
        Object value=vgs.generateValue(md,field.getValueGenerator());
        return field.getType().toJson(factory.getNodeFactory(),value);
    }

    private static boolean required(SimpleField f) {
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

    private GeneratedFields() {
    }
}
