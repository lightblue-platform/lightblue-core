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
import com.redhat.lightblue.util.Path;

public class IfFieldEmpty extends If {
    private Path field;

    public void setField(Path field) {
        this.field = field;
    }

    public Path getField() {
        return field;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if (field == null || field.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No field informed");
        }
        T s = p.newNode();

        p.putString(s, "field", field.toString());

        if (lastArrayNode == null) {
            p.putObject(node, "$field-empty", s);
        } else {
            T iT = p.newNode();
            p.putObject(iT, "$field-empty", s);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }
}
