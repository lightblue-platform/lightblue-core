package com.redhat.lightblue.metadata.rdbms;

public class InOut {
    private String documentPath;
    private String columnName;
    private String variableName;
    private boolean array;
    private boolean accumulative;

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public boolean isAccumulative() {
        return accumulative;
    }

    public void setAccumulative(boolean accumulative) {
        this.accumulative = accumulative;
    }
}
