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
        IntSequence,
        UUID,
        CurrentTime
    }

    // defines how values are generated
    private final ValueGeneratorType valueGeneratorType;
    private final Properties properties = new Properties();
    private boolean overwrite = false;

    public ValueGenerator(ValueGeneratorType valueGeneratorType) {
        super();
        this.valueGeneratorType = valueGeneratorType;
    }

    public ValueGeneratorType getValueGeneratorType() {
        return valueGeneratorType;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean b) {
        overwrite = b;
    }
}
