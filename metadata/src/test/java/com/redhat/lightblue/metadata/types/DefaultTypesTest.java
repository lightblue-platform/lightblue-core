package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultTypesTest {

    DefaultTypes defaultTypes;

    @Before
    public void setUp() throws Exception {
        defaultTypes = new DefaultTypes();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetType() {
        Type type = defaultTypes.getType(BooleanType.NAME);
        assertTrue(type instanceof BooleanType);
    }

    @Test
    public void testDefaultTypes() {
        assertNotNull(defaultTypes);
    }

}
