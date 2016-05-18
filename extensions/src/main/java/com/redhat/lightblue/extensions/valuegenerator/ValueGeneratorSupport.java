package com.redhat.lightblue.extensions.valuegenerator;

import com.redhat.lightblue.extensions.Extension;

import com.redhat.lightblue.metadata.ValueGenerator;
import com.redhat.lightblue.metadata.EntityMetadata;

/**
 * Value generation interface.
 *
 */
public interface ValueGeneratorSupport extends Extension {

    /**
     * Returns the value generators this back end supports
     */
    ValueGenerator.ValueGeneratorType[] getSupportedGeneratorTypes();

    /**
     * Generates a new value. The returned value is a Java value object.
     *
     */
    Object generateValue(EntityMetadata md, ValueGenerator generator);

}
