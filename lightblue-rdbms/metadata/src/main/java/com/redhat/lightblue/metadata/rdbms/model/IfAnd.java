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
package com.redhat.lightblue.metadata.rdbms.model;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.List;

public class IfAnd extends If<If, If> {
    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if (getConditions() == null || getConditions().size() < 2) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "$and/$all doesn't have enough conditionals");
        }
        Object eT;
        if (lastArrayNode == null) {
            eT = p.newArrayField(node, "$and");
        } else {
            T iT = p.newNode();
            eT = p.newArrayField(iT, "$and");
            p.addObjectToArray(lastArrayNode, iT);
        }
        for (If i : getConditions()) {
            i.convert(p, eT, node);
        }
    }

    @Override
    public <T> If parse(MetadataParser<T> p, T ifT) {
        If x = null;
        List<T> andArray = p.getObjectList(ifT, "$and");
        if (andArray != null) {
            x = parseIfs(p, andArray, new IfAnd());
        } else {
            List<T> allArray = p.getObjectList(ifT, "$all");
            if (allArray != null) {
                x = parseIfs(p, allArray, new IfAnd());
            }
        }
        return x;
    }
}
