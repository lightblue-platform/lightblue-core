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
package com.redhat.lightblue.crud;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Defines a class that take a response from a datasource and translates it
 * into something Lightblue can use.
 *
 * @author dcrissman
 *
 * @param <S> - The source type that this {@link TranslatorToJson} converts
 * to json.
 */
public abstract class TranslatorToJson<S> {

    private final JsonNodeFactory factory;
    protected final EntityMetadata entityMetadata;

    public TranslatorToJson(JsonNodeFactory factory, EntityMetadata entityMetadata){
        this.factory = factory;
        this.entityMetadata = entityMetadata;
    }

    protected JsonNode toJson(Type type, Object value){
        return type.toJson(factory, value);
    }

    /**
     * Translates the source to a {@link JsonDoc}.
     * @param source - Object containing the source data.
     * @return {@link JsonDoc}
     */
    public JsonDoc translate(S source){
        Error.push("translating to json");
        try{
            FieldCursor cursor = entityMetadata.getFieldCursor();

            if (cursor.firstChild()) {
                ObjectNode node = factory.objectNode();

                iterateOverNodes(source, node, cursor);

                return new JsonDoc(node);
            }
        }
        finally{
            Error.pop();
        }

        //TODO: What to do in case of a null value here?
        return null;
    }

    private void iterateOverNodes(Object value, ContainerNode<?> targetNode, FieldCursor cursor) {
        do {
            appendToJsonNode(value, targetNode, cursor);
        } while(cursor.nextSibling());
    }

    protected void appendToJsonNode(Object value, ContainerNode<?> targetNode, FieldCursor cursor) {
        FieldTreeNode field = cursor.getCurrentNode();

        Error.push(field.getName());
        try{
            JsonNode newJsonNode = null;
            Object newValue = null;

            if (field instanceof ObjectField || field instanceof ObjectArrayElement) {
                newJsonNode = translateToObjectNode(value, cursor);
            }
            else if((newValue = getValueFor(value, cursor.getCurrentPath())) != null){
                if (field instanceof SimpleField) {
                    newJsonNode = translate((SimpleField)field, newValue);
                }
                else if (field instanceof ArrayField){
                    newJsonNode = translateToArrayNode((ArrayField) field, newValue, cursor);
                }
                else{
                    throw new UnsupportedOperationException("Unknown Field type: " + field.getClass().getName());
                }
            }

            if (targetNode instanceof ObjectNode) {
                ((ObjectNode) targetNode).set(cursor.getCurrentNode().getName(), newJsonNode);
            }
            else {
                ((ArrayNode) targetNode).add(newJsonNode);
            }
        }
        finally{
            Error.pop();
        }
    }

    ObjectNode translateToObjectNode(Object value, FieldCursor cursor) {
        if (!cursor.firstChild()) {
            //TODO: Should an exception be thrown here?
            return null;
        }

        ObjectNode node = factory.objectNode();

        iterateOverNodes(value, node, cursor);

        cursor.parent();

        return node;
    }

    ArrayNode translateToArrayNode(ArrayField field, Object value, FieldCursor cursor) {
        if (!cursor.firstChild()) {
            //TODO: Should an exception be thrown here?
            return null;
        }

        FieldTreeNode node = cursor.getCurrentNode();
        ArrayElement arrayElement = field.getElement();

        ArrayNode valueNode = factory.arrayNode();
        if (arrayElement instanceof SimpleArrayElement) {
            List<? extends Object> values = getSimpleArrayValues(value, (SimpleArrayElement) arrayElement);
            if (values != null) {
                for(Object v : values){
                    valueNode.add(toJson(node.getType(), v));
                }
            }
        }
        else if(arrayElement instanceof ObjectArrayElement){
            Iterable<?> iter;
            if(value instanceof Iterable<?>) {
                iter = (Iterable<?>) value;
            }
            else if (value.getClass().isArray()) {
                iter = Arrays.asList((Object[]) value);
            }
            else {
                throw new IllegalArgumentException("Object must be convertable to an Iterable: " + value.getClass());
            }

            for (Object o : iter) {
                FieldCursor iterFieldCursor = new FieldCursor(cursor.getCurrentPath(), field);
                if (iterFieldCursor.firstChild()) {
                    iterateOverNodes(o, valueNode, iterFieldCursor);
                }
            }
        }
        else{
            throw new UnsupportedOperationException("ArrayElement type is not supported: " + node.getClass().getName());
        }

        cursor.parent();
        return valueNode;
    }

    protected abstract Object getValueFor(Object source, Path path);
    protected abstract List<? extends Object> getSimpleArrayValues(Object value, SimpleArrayElement simpleArrayElement);

    /**
     * Converts a Object to a {@link JsonNode}. This is a default implementation, but it will likely
     * need to be overridden by implementing classes depending on the specifics of the source Object.
     * @param field - {@link SimpleField}
     * @param value - Source {@link Object} to be converted
     * @return {@link JsonNode} representation of {@link Object}.
     */
    protected JsonNode translate(SimpleField field, Object value) {
        return toJson(field.getType(), value);
    }

}
