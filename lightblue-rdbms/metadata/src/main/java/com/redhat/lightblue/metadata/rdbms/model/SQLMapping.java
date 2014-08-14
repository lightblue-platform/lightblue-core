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
import com.redhat.lightblue.metadata.rdbms.converter.RootConverter;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lcestari
 */
public class SQLMapping implements RootConverter {

    private List<Join> joins;
    private List<ColumnToField> columnToFieldMap;

    public <T> void parse(MetadataParser<T> p, T node) {
        List<T> joinsT = p.getObjectList(node, "joins");
        List<T> ctfmT = p.getObjectList(node, "columnToFieldMap");

        List<Join> js = parseJoins(p, joinsT);
        List<ColumnToField> ctfm = parseColumnToFieldMap(p, ctfmT);

        this.joins = js;
        this.columnToFieldMap = ctfm;
    }

    private <T> List<ColumnToField> parseColumnToFieldMap(MetadataParser<T> p, List<T> ctfmT) {
        if (ctfmT == null || ctfmT.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No columnToField informed for SQLMapping");
        }
        List<ColumnToField> list = new ArrayList<>();
        for (T t : ctfmT) {
            ColumnToField c = new ColumnToField();
            c.parse(p, t);
            list.add(c);
        }
        return list;
    }

    private <T> List<Join> parseJoins(MetadataParser<T> p, List<T> joinsT) {
        if (joinsT == null || joinsT.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No joins informed for SQLMapping");
        }
        List<Join> js = new ArrayList<>();
        for (T t : joinsT) {
            Join j = new Join();
            j.parse(p, t);
            js.add(j);
        }
        return js;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, T rdbms) {
        T oT = p.newNode();
        Object js = p.newArrayField(oT, "joins");
        convertJoins(p, js);
        Object c = p.newArrayField(oT, "columnToFieldMap");
        convertColumnToFieldMap(p, c);
        p.putObject(rdbms, "SQLMapping", oT);
    }

    private <T> void convertColumnToFieldMap(MetadataParser<T> p, Object c) {
        if (this.columnToFieldMap == null || this.columnToFieldMap.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing columnToFieldMap field");
        }
        for (ColumnToField columnToField : columnToFieldMap) {
            columnToField.convert(p, c);
        }
    }

    private <T> void convertJoins(MetadataParser<T> p, Object array) {
        if (this.joins == null || this.joins.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing joins field");
        }
        for (Join j : this.joins) {
            j.convert(p, array);
        }
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public List<ColumnToField> getColumnToFieldMap() {
        return columnToFieldMap;
    }

    public void setColumnToFieldMap(List<ColumnToField> columnToFieldMap) {
        this.columnToFieldMap = columnToFieldMap;
    }
}
