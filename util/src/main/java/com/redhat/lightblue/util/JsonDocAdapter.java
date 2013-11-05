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

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonDocAdapter implements DocAdapter<JsonNode> {

    private static final EmptyIterator<JsonNode> EMPTY=new EmptyIterator<JsonNode>();

    @Override
    public int getNumChildren(JsonNode current) {
        return ((JsonNode)current).size();
    }

    @Override
    public JsonNode getChild(JsonNode current,int index) {
        if(current instanceof ArrayNode)
            return ((ArrayNode)current).get(index);
        else
            throw new InvalidArrayAccessException();
    }

    @Override
    public JsonNode getChild(JsonNode current,String name) {
        if(current instanceof ObjectNode)
            return ((ObjectNode)current).get(name);
        else
            throw new InvalidObjectAccessException();
    }

    @Override
    public Iterator<JsonNode> getChildren(JsonNode current) {
        if(current instanceof ObjectNode ||
           current instanceof ArrayNode)
            return ((JsonNode)current).elements();
        else
            return EMPTY;
    }

    @Override
    public boolean acceptsAny(JsonNode current) {
        return false;
    }

    @Override
    public boolean acceptsIndex(JsonNode current) {
        return current instanceof ArrayNode;
    }

    @Override
    public boolean acceptsName(JsonNode current) {
        return current instanceof ObjectNode;
    }
}
