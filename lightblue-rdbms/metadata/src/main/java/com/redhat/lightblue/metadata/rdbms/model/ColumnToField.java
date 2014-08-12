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
import com.redhat.lightblue.metadata.rdbms.converter.SimpleConverter;

import java.util.List;

/**
 * Created by lcestari on 8/8/14.
 */
public class ColumnToField implements SimpleConverter {

    private String table;
    private String column;
    private String field;

    public <T> void parse(MetadataParser<T> p, T t) {
        String ta = p.getStringProperty(t, "table");
        String co = p.getStringProperty(t, "column");
        String fi = p.getStringProperty(t, "field");

        this.table = ta;
        this.column = co;
        this.field = fi;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();

        p.putString(eT, "table", table);
        p.putString(eT, "column", column);
        p.putString(eT, "field", field);

        p.addObjectToArray(expressionsNode, eT);
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
