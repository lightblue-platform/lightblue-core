package com.redhat.lightblue.metadata;

import java.io.Serializable;
import java.util.Properties;

/**
 * Represents value generator definition located in entityInfo.
 *
 * @author mpatercz
 *
 */
public class ValueGenerator implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ValueGeneratorType {
        IntSequance;
    }

    private ValueGeneratorType type;
    private String name;
    private Properties properties = new Properties();

    public ValueGenerator(ValueGeneratorType type, String name) {
        super();
        this.type = type;
        this.name = name;
    }

    public ValueGeneratorType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Properties getProperties() {
        return properties;
    }

}
