package com.redhat.lightblue.common.rdbms;

public class Parameter {
    private String name;
    private String type;
    private String pathToField;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPathToField() {
        return pathToField;
    }

    public void setPathToField(String pathToField) {
        this.pathToField = pathToField;
    }

}
