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
        IntSequence;
    }

    // defines how values are generated
    private final ValueGeneratorType valueGeneratorType;
    private final String name;
    private final Properties properties = new Properties();

    public ValueGenerator(ValueGeneratorType valueGeneratorType, String name) {
        super();
        this.valueGeneratorType = valueGeneratorType;
        this.name = name;
    }

    public ValueGeneratorType getValueGeneratorType() {
        return valueGeneratorType;
    }

    public String getName() {
        return name;
    }

    public Properties getProperties() {
        return properties;
    }

}
