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

import com.redhat.lightblue.util.DefaultResolver;

import com.redhat.lightblue.metadata.FieldConstraint;

import com.redhat.lightblue.metadata.constraints.ArraySizeConstraint;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;
import com.redhat.lightblue.metadata.constraints.MinMaxConstraint;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.constraints.StringLengthConstraint;

/**
 * Convenience class to register all predefined constraint parsers to
 * MetadataParser
 */
public class DefaultFieldConstraintParsers<NODE_TYPE> 
    extends DefaultResolver<String,Parser<NODE_TYPE,FieldConstraint>> {
    
    public DefaultFieldConstraintParsers() {
        addValue(ArraySizeConstraint.MIN,new ArraySizeConstraintParser<NODE_TYPE>());
        addValue(ArraySizeConstraint.MAX,new ArraySizeConstraintParser<NODE_TYPE>());
        addValue(EnumConstraint.TYPE,new EnumConstraintParser<NODE_TYPE>());
        addValue(MinMaxConstraint.MIN,new MinMaxConstraintParser<NODE_TYPE>());
        addValue(MinMaxConstraint.MAX,new MinMaxConstraintParser<NODE_TYPE>());
        addValue(RequiredConstraint.REQUIRED,new RequiredConstraintParser<NODE_TYPE>());
        addValue(StringLengthConstraint.MINLENGTH,new StringLengthConstraintParser<NODE_TYPE>());
        addValue(StringLengthConstraint.MAXLENGTH,new StringLengthConstraintParser<NODE_TYPE>());
        addValue(EnumConstraint.TYPE, new EnumConstraintParser<NODE_TYPE>());
    }
}
