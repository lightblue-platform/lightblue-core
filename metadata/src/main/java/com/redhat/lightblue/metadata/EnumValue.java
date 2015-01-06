package com.redhat.lightblue.metadata;

import java.io.Serializable;

public class EnumValue implements Serializable{

    private static final long serialVersionUID = -1182170538084137297L;

    private String name;
    private String description;

    public EnumValue(){}

    public EnumValue(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EnumValue other = (EnumValue) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        }
        else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EnumValue [name=" + name + ", description=" + description + "]";
    }

}
