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
import java.util.ArrayList;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.MetadataParser;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.metadata.constraints.UniqueConstraint;

/**
 * Parses a Unique constraint
 */
public class UniqueConstraintParser<T> implements EntityConstraintParser<T> {

    /**
     * Parses a UniqueConstraint at the given node
     */
    @Override
    public EntityConstraint parse(String name, MetadataParser<T> p, T node) {
        if (!UniqueConstraint.UNIQUE.equals(name)) {
            Error.get(MetadataParser.ERR_ILL_FORMED_METADATA, name);
        }

        UniqueConstraint ret = new UniqueConstraint();
        List<String> values = p.getStringList(node, UniqueConstraint.UNIQUE);
        ArrayList<Path> l = new ArrayList<>();
        
        for (String x : values) {
            l.add(new Path(x));
        }
        
        ret.setFields(l);
        return ret;
    }

    /**
     * Converts a unique constraint into document
     */
    @Override
    public void convert(MetadataParser<T> p, T emptyNode, EntityConstraint object) {
        Object arr = p.newArrayField(emptyNode, UniqueConstraint.UNIQUE);
        for (Path x : ((UniqueConstraint) object).getFields()) {
            p.addStringToArray(arr, x.toString());
        }
    }
}
