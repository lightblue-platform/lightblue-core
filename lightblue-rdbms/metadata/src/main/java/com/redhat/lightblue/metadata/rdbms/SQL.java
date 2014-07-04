package com.redhat.lightblue.metadata.rdbms;

public class SQL {
    private String SQL;
    private Boolean iterateOverRows;
    private String type;
    private String datasource;

    public void setSQL(String SQL) {
        this.SQL = SQL;
    }

    public String getSQL() {
        return SQL;
    }

    public void setIterateOverRows(Boolean iterateOverRows) {
        this.iterateOverRows = iterateOverRows;
    }

    public Boolean getIterateOverRows() {
        return iterateOverRows;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatasource() {
        return datasource;
    }
}
