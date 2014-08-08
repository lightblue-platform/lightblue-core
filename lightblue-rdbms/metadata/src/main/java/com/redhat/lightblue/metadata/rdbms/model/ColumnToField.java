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
