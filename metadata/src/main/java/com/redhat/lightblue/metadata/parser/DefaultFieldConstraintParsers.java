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

import com.redhat.lightblue.metadata.FieldConstraint;

import com.redhat.lightblue.metadata.constraints.ArraySizeConstraint;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;

public class DefaultFieldConstraintParsers<NodeType> implements ParserResolver<NodeType,FieldConstraint> {
    
    private final Map<String,Parser<NodeType,FieldConstraint>> map;

    public DefaultFieldConstraintParsers() {
        map=new HashMap<String,Parser<NodeType,FieldConstraint>>();
        map.put(ArraySizeConstraint.MIN,new ArraySizeConstraintParser<NodeType>());
        map.put(ArraySizeConstraint.MAX,new ArraySizeConstraintParser<NodeType>());
        map.put(EnumConstraint.TYPE,new EnumConstraintParser<NodeType>());
        map.put(MinMaxConstraint.MIN,new MinMaxConstraintParser<NodeType>());
        map.put(MinMaxConstraint.MAX,new MinMaxConstraintParser<NodeType>());
        map.put(RequiredConstraint.REQUIRED,new RequiredConstraintParser<NodeType>());
        map.put(StringLengthConstraint.MINLENGTH,new StringLengthConstraintParser<NodeType>());
        map.put(StringLengthConstraint.MAXLENGTH,new StringLengthConstraintParser<NodeType>());
    }
    
    public Parser<NodeType,FieldConstraint> get(String objectName) {
        return map.get(objectName);
    }
}
