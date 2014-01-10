package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ArrayTypeTest {

    @Test
    public void testIsAContainerType() {
        assertTrue(ContainerType.class.isAssignableFrom(ArrayType.class));
    }

}
