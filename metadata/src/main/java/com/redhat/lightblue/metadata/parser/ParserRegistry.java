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

package com.redhat.lightblue.metadata.parser;

import java.util.Map;
import java.util.HashMap;

/**
 * Allows parser extensions to be registered with the metadata parser
 */
public class ParserRegistry<NodeType,ObjType> {

    private final Map<String,Parser<NodeType,ObjType>> parserMap=
        new HashMap<String,Parser<NodeType,ObjType>>();
    
    public synchronized void add(String objectName,Parser<NodeType,ObjType> parser) {
        parserMap.put(objectName,parser);
    }

    public Parser<NodeType,ObjType> get(String objectName) {
        return parserMap.get(objectName);
    }
}
