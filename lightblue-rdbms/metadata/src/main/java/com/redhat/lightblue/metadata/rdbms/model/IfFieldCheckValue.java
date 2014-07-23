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
import com.redhat.lightblue.metadata.rdbms.enums.OpOperators;
import com.redhat.lightblue.metadata.rdbms.parser.RDBMSMetadataConstants;
import com.redhat.lightblue.util.Path;

public class IfFieldCheckValue extends If {
    private Path field;
    private String value;
    private String op;

    public void setField(Path field) {
        this.field = field;
    }

    public Path getField() {
        return field;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setOp(String op) {
        if (!OpOperators.check(op)) {
            throw new IllegalStateException("Not a valid op '" + op + "'. Valid OpOperators:" + OpOperators.getValues());
        }
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if (field == null || field.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No field informed");
        }
        if (op == null || op.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No op informed");
        }
        if (value == null || value.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No value informed");
        }
        T s = p.newNode();

        p.putString(s, "field", field.toString());
        p.putString(s, "op", value);
        p.putString(s, "value", op);

        if (lastArrayNode == null) {
            p.putObject(node, "$field-check-value", s);
        } else {
            T iT = p.newNode();
            p.putObject(iT, "$field-check-value", s);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }
}
