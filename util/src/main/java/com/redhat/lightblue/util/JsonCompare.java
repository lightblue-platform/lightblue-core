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
package com.redhat.lightblue.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Compares two json documents and builds an list of all changes
 *
 * Json containers (objects and arrays) are compared recursively. The
 * comparison algorithm works like this:
 *
 * Objects: A field-by-field comparison is done. If a field exists in
 * the first document but not in the second, that field is removed. If
 * a field exists in the second document but not the first, that field
 * is added. If a field exists in both documents with different
 * values, that field is modified.
 *
 * Arrays: There are two possible algorithms to compare arrays. If
 * array elements contain a unique identifier (which is defined by the
 * caller), then array elelements of the first and the second document
 * are matched using the unique identifiers of array elements. Then
 * each matching array element is compared to generate the detailed
 * difference. If array elements don't have unique identifiers, then
 * each element of the first array is compared to each element of the
 * second array, and the elements with minimal number of changes are
 * associated. Elements that are too different from each other are not
 * associated.
 *
 * Differences: 
 *
 * An Addition denotes a new field or array element. Addition.field1
 * is null, meaning the field does not exist in document1, and
 * Addition.field2 denotes the new field, or array element.
 *
 * A Removal denotes a removed field or array element. Removal.field1
 * denotes the element in document1, and Removal.field2 is null.
 *
 * A Modification denotes a content modification of a field, or array
 * element. Both field1 and field2 are non-null, and set to the name
 * of the modified field.
 *
 * A Move denotes an array element move. field1 denotes the old index
 * of the array element, and field2 denotes the new index.
 *
 * If new elements are added to an array, or existing elements are
 * removed, the addition and removal appear as diff, and any node that
 * shifted during the operation appears within a Move.
 */
public class JsonCompare extends DocComparator<JsonNode,ValueNode,ObjectNode,ArrayNode> {

    public static class DefaultIdentityExtractor implements IdentityExtractor<JsonNode> {
        private final Path[] fields;

        public DefaultIdentityExtractor(ArrayIdentityFields fields) {
            this.fields=fields.getFields();
        }

        @Override
        public Object getIdentity(JsonNode element) {
            JsonNode[] nodes=new JsonNode[fields.length];
            for(int i=0;i<fields.length;i++) {
                nodes[i]=JsonDoc.get(element,fields[i]);
            }
            return new DefaultIdentity(nodes);
        }
    }

    @Override
    protected boolean isValue(JsonNode value) {
        return value instanceof ValueNode;
    }

    @Override
    protected boolean isArray(JsonNode value) {
        return value instanceof ArrayNode;
    }

    @Override
    protected boolean isObject(JsonNode value) {
        return value instanceof ObjectNode;
    }

    @Override
    protected boolean isNull(JsonNode value) {
        return value==null||value instanceof NullNode;
    }

    @Override
    protected ValueNode asValue(JsonNode value) {
        return (ValueNode)value;
    }

    @Override
    protected ArrayNode asArray(JsonNode value) {
        return (ArrayNode)value;
    }

    @Override
    protected ObjectNode asObject(JsonNode value) {
        return (ObjectNode)value;
    }

    @Override
    protected boolean equals(ValueNode value1,ValueNode value2) {
        if(value1.isNumber()&&value2.isNumber()) {
            return value1.asText().equals(value2.asText());
        } else {
            return value1.equals(value2);
        }
    }

    @Override
    protected Iterator<Map.Entry<String,JsonNode>> getFields(ObjectNode node) {
        return node.fields();
    }

    @Override
    protected boolean hasField(ObjectNode value,String field) {
        return value.has(field);
    }

    @Override
    protected JsonNode getField(ObjectNode value,String field) {
        return value.get(field);
    }

    @Override
    protected IdentityExtractor getArrayIdentityExtractorImpl(ArrayIdentityFields fields) {
        return new DefaultIdentityExtractor(fields);
    }

    @Override
    protected JsonNode getElement(ArrayNode value,int index) {
        return value.get(index);
    }

    @Override
    protected int size(ArrayNode value) {
        return value.size();
    }
}
