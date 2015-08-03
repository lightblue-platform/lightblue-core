package com.redhat.lightblue.metadata;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A map of {@link ValueGenerator}s. The name is the map's key and it has to be unique in entity's scope.
 *
 * @author mpatercz
 *
 */
public class ValueGenerators implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String,ValueGenerator> valueGenerators = new HashMap<>();

    public void addValueGenerator(ValueGenerator valueGenerator) {
        if (valueGenerators.containsKey(valueGenerator.getName())) {
            throw new IllegalArgumentException("Generator name has to be unique: "+valueGenerator.getName());
        }

        valueGenerators.put(valueGenerator.getName(), valueGenerator);
    }

    public void setValueGenerators(Collection<ValueGenerator> valueGenerators) {
        if (valueGenerators == null)
            return;

        this.valueGenerators.clear();

        for (ValueGenerator g: valueGenerators)
            addValueGenerator(g);
    }

    public ValueGenerator getValueGenerator(String name) {
        return valueGenerators.get(name);
    }

}
