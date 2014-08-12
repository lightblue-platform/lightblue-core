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
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import com.redhat.lightblue.util.Path;

public class IfFieldCheckField extends If<If, If> {
    private Path field;
    private Path rfield;
    private String op;

    public void setField(Path field) {
        this.field = field;
    }

    public Path getField() {
        return field;
    }

    public void setRfield(Path rfield) {
        this.rfield = rfield;
    }

    public Path getRfield() {
        return rfield;
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
        if (rfield == null || rfield.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No rfield informed");
        }
        T s = p.newNode();

        p.putString(s, "field", field.toString());
        p.putString(s, "rfield", rfield.toString());
        p.putString(s, "op", op);

        if (lastArrayNode == null) {
            p.putObject(node, "$fieldCheckField", s);
        } else {
            T iT = p.newNode();
            p.putObject(iT, "$fieldCheckField", s);
            p.addObjectToArray(lastArrayNode, iT);
        }
    }

    @Override
    public <T> If parse(MetadataParser<T> p, T ifT) {
        If x = null;
        T pathpath = p.getObjectProperty(ifT, "$fieldCheckField");
        if (pathpath != null) {
            x = new IfFieldCheckField();
            String conditional = p.getStringProperty(pathpath, "op");
            String path1 = p.getStringProperty(pathpath, "field");
            String path2 = p.getStringProperty(pathpath, "rfield");
            if (path1 == null || path1.isEmpty()) {
                throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "$fieldCheckField: field not informed");
            }
            if (path2 == null || path2.isEmpty()) {
                throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "$fieldCheckField: rfield not informed");
            }
            if (conditional == null || conditional.isEmpty()) {
                throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "$fieldCheckField: op not informed");
            }
            ((IfFieldCheckField) x).setField(new Path(path1));
            ((IfFieldCheckField) x).setRfield(new Path(path2));
            ((IfFieldCheckField) x).setOp(conditional);
        }
        return x;
    }
}
