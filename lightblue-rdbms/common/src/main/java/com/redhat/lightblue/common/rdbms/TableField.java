package com.redhat.lightblue.common.rdbms;

import java.util.List;

public class TableField {
    private String tableName;
    private String columnName;
    private String fieldName;
    private String nullable;
    private String type;
    private String tablePKName;
    private List<String> tableFKNames;
    private String pathToField;
    private String linkToParameter;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTablePKName() {
        return tablePKName;
    }

    public void setTablePKName(String tablePKName) {
        this.tablePKName = tablePKName;
    }

    public List<String> getTableFKNames() {
        return tableFKNames;
    }

    public void setTableFKNames(List<String> tableFKNames) {
        this.tableFKNames = tableFKNames;
    }

    public String getPathToField() {
        return pathToField;
    }

    public void setPathToField(String pathToField) {
        this.pathToField = pathToField;
    }

    public String getLinkToParameter() {
        return linkToParameter;
    }

    public void setLinkToParameter(String linkToParameter) {
        this.linkToParameter = linkToParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableField that = (TableField) o;

        if (linkToParameter != null ? !linkToParameter.equals(that.linkToParameter) : that.linkToParameter != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return linkToParameter != null ? linkToParameter.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TableField{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", nullable='" + nullable + '\'' +
                ", type='" + type + '\'' +
                ", tablePKName='" + tablePKName + '\'' +
                ", tableFKNames=" + tableFKNames +
                ", pathToField='" + pathToField + '\'' +
                ", linkToParameter='" + linkToParameter + '\'' +
                '}';
    }
}
