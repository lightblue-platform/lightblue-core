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

import com.redhat.lightblue.metadata.EntityConstraint;

import com.redhat.lightblue.metadata.constraints.UniqueConstraint;
import com.redhat.lightblue.metadata.constraints.ReferencesConstraint;

/**
 * Convenience class to register all predefined constraint parsers to
 * MetadataParser
 */
public class DefaultEntityConstraintParsers<NodeType> 
    implements ParserResolver<NodeType,EntityConstraint> {
    
    private final Map<String,Parser<NodeType,EntityConstraint>> map;

    public DefaultEntityConstraintParsers() {
        map=new HashMap<String,Parser<NodeType,EntityConstraint>>();
        map.put(UniqueConstraint.UNIQUE,new UniqueConstraintParser<NodeType>());
        map.put(ReferencesConstraint.REFERENCES,new ReferencesConstraintParser<NodeType>());
    }
    
    public Parser<NodeType,EntityConstraint> get(String objectName) {
        return map.get(objectName);
    }
}
