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
import com.redhat.lightblue.metadata.rdbms.converter.ComplexConverter;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import com.redhat.lightblue.util.Path;

public class InOut implements ComplexConverter {
    private String column;
    private Path field;

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        boolean col = this.getColumn() == null || this.getColumn().isEmpty();
        boolean path = this.getField() == null || this.getField().toString().isEmpty();
        if (col || path) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Invalid InOut: No column or path informed");
        }
        T ioT = p.newNode();
        p.putString(ioT, "column", this.getColumn());
        p.putString(ioT, "field", this.getField().toString());
        p.addObjectToArray(lastArrayNode, ioT);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Path getField() {
        return field;
    }

    public void setField(Path field) {
        this.field = field;
    }
}
