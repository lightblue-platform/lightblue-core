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

import java.util.List;
import java.util.Set;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.constraints.EnumConstraint;

public class EnumConstraintParser<T> implements FieldConstraintParser<T> {

    @Override
    public FieldConstraint parse(String name, MetadataParser<T> p, T node) {
        if (!EnumConstraint.TYPE.equals(name)) {
            throw Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
        }

        List<String> values = p.getStringList(node, EnumConstraint.TYPE);

        if (values != null) {
            EnumConstraint ret = new EnumConstraint();
            ret.setValues(values);
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, FieldConstraint object) {
        Object t = p.newArrayField(emptyNode, EnumConstraint.TYPE);
        Set<String> values = ((EnumConstraint) object).getValues();
        if (values != null) {
            for (String x : values) {
                p.addStringToArray(t, x);
            }
        }
    }
}
