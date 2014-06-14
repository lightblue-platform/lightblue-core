package com.redhat.lightblue.common.rdbms;

public class Parameter {
    private String name;
    private String type;
    private String pathToField;
    private String linkToField;

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

    public String getLinkToField() {
        return linkToField;
    }

    public void setLinkToField(String linkToField) {
        this.linkToField = linkToField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (linkToField != null ? !linkToField.equals(parameter.linkToField) : parameter.linkToField != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return linkToField != null ? linkToField.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", pathToField='" + pathToField + '\'' +
                ", linkToField='" + linkToField + '\'' +
                '}';
    }
}
