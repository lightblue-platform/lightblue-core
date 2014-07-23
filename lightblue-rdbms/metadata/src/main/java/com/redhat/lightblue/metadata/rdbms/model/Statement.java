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
import com.redhat.lightblue.metadata.rdbms.parser.RDBMSMetadataConstants;
import com.redhat.lightblue.metadata.rdbms.enums.TypeOperators;

public class Statement extends Expression {

    private String datasource;
    private String SQL;
    private String type;

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    public String getSQL() {
        return SQL;
    }

    public void setType(String type) {
        if (!TypeOperators.check(type)) {
            throw new IllegalStateException("Not a valid type of SQL operation '" + type + "'. Valid types:" + TypeOperators.getValues());
        }
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        if (SQL == null) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No SQL statement informed");
        }
        if (type == null) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No type informed");
        }

        T eT = p.newNode();

        if (datasource != null) {
            p.putString(eT, "datasource", datasource);
        }
        p.putString(eT, "sql", SQL);
        p.putString(eT, "type", type);

        T s = p.newNode();
        p.putObject(s, "$statement", eT);

        p.addObjectToArray(expressionsNode, s);
    }
}
