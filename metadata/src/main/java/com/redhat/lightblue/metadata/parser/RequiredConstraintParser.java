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

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.MetadataParser;

import com.redhat.lightblue.metadata.constraints.RequiredConstraint;

public class RequiredConstraintParser<T> implements FieldConstraintParser<T> {

    @Override
    public FieldConstraint parse(MetadataParser<T> p, T node) {
        Object value = p.getValueProperty(node, RequiredConstraint.REQUIRED);
        RequiredConstraint ret = new RequiredConstraint();
        if (value instanceof Boolean) {
            ret.setValue((Boolean) value);
        } else {
            throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, ret.getType());
        }
        return ret;
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, FieldConstraint object) {
        p.putValue(emptyNode, object.getType(), ((RequiredConstraint) object).getValue());
    }
}
