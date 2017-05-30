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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonNodeCursor;
import com.redhat.lightblue.util.Path;

/**
 * Defines a class that translates lightblue json nodes into
 * something that a specific datasource can understand.
 *
 * @author dcrissman
 *
 * @param <T> - The target entity type that the specific datastore knows how
 * to interact with.
 */
public abstract class TranslatorFromJson<T> {

    protected final EntityMetadata entityMetadata;

    public TranslatorFromJson(EntityMetadata entityMetadata){
        this.entityMetadata = entityMetadata;
    }

    protected Object fromJson(Type type, JsonNode node){
        if (node == null || node instanceof NullNode) {
            return null;
        }
        else {
            return type.fromJson(node);
        }
    }

    /**
     * Translates an entire {@link JsonDoc} to T.
     * @param document - {@link JsonDoc}
     * @param target - T
     */
    public void translate(JsonDoc document, T target){
        Error.push("translating from json");
        try{
            JsonNodeCursor cursor = document.cursor();
            if (!cursor.firstChild()) {
                //TODO throw exception?
                return;
            }

            do {
                translate(cursor, target);
            } while (cursor.nextSibling());
        }
        finally{
            Error.pop();
        }
    }

    /**
     * Uses the current position of the cursor to translate the current node and any children it may have.
     * This is ultimately the driver behind this class and may be called recursively by implementing classes to process child nodes.<br>
     * <b>NOTE:</b> Calling method is responsible for moving the cursor to where it needs to be.
     * @param cursor - {@link JsonNodeCursor}
     * @param target - T
     */
    protected void translate(JsonNodeCursor cursor, Object target) {
        JsonNode node = cursor.getCurrentNode();
        FieldTreeNode fieldNode = entityMetadata.resolve(cursor.getCurrentPath());

        Error.push(fieldNode.getFullPath().getLast());

        try{
            if (fieldNode instanceof SimpleField) {
                translate((SimpleField) fieldNode, node, target);
            }
            else if (fieldNode instanceof ObjectField) {
                translate((ObjectField) fieldNode, cursor, target);
            }
            else if (fieldNode instanceof ArrayField) {
                translate((ArrayField) fieldNode, cursor, target);
            }
            else{
                throw Error.get(CrudConstants.ERR_UNSUPPORTED_FEATURE + fieldNode.getClass().getName(), fieldNode.getFullPath().toString());
            }
        }
        finally{
            Error.pop();
        }
    }

    private void translate(ArrayField field, JsonNodeCursor cursor, Object target) {
        if(!cursor.firstChild()){
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, cursor.getCurrentPath().toString());
        }

        ArrayElement arrayElement = field.getElement();

        if (arrayElement instanceof SimpleArrayElement) {
            List<Object> items = new ArrayList<>();
            do {
                items.add(fromJson(arrayElement.getType(), cursor.getCurrentNode()));
            } while (cursor.nextSibling());
            translate(field, items, target);
        }
        else if(arrayElement instanceof ObjectArrayElement){
            List<Object> items = new ArrayList<>();
            do {
                Object item = createInstanceFor(arrayElement.getFullPath());
                translate((ObjectArrayElement) arrayElement, cursor.getCurrentNode(), item);
                items.add(item);
            } while (cursor.nextSibling());
            translate(field, items, target);
        }
        else{
            throw Error.get(CrudConstants.ERR_UNSUPPORTED_FEATURE + arrayElement.getClass().getName(), field.getFullPath().toString());
        }

        cursor.parent();
    }

    protected void translate(ObjectField field, JsonNodeCursor cursor, Object target) {
        if(!cursor.firstChild()){
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, cursor.getCurrentPath().toString());
        }

        do {
            translate(cursor, target);
        } while (cursor.nextSibling());

        cursor.parent();
    }

    protected void translate(final ObjectArrayElement objectArrayElement, JsonNode node, Object target) {
        JsonNodeCursor cursor = new JsonNodeCursor(objectArrayElement.getFullPath(), node);

        if(!cursor.firstChild()){
            throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, cursor.getCurrentPath().toString());
        }

        do {
            translate(cursor, target);
        } while (cursor.nextSibling());
    }

    protected abstract void translate(SimpleField field, JsonNode node, Object target);

    protected abstract void translate(ArrayField field, List<Object> items, Object target);

    protected abstract Object createInstanceFor(Path path);

}
