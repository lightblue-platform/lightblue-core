package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.Type;

public class ContainerTypeTest {

    Type type;

    class NewContainerType extends ContainerType {
        public static final String NAME = ObjectType.NAME;

        public NewContainerType(String name) {
            super(name);
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public boolean supportsEq() {
            return super.supportsEq();
        }

        @Override
        public boolean supportsOrdering() {
            return super.supportsOrdering();
        }

        @Override
        public int compare(Object v1, Object v2) {
            return super.compare(v1, v2);
        }

        @Override
        public Object cast(Object v) {
            return super.cast(v);
        }

        @Override
        public JsonNode toJson(JsonNodeFactory factory, Object value) {
            return super.toJson(factory, value);
        }

        @Override
        public Object fromJson(JsonNode value) {
            return super.fromJson(value);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @Before
    public void setUp() throws Exception {
        type = new NewContainerType(NewContainerType.NAME);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHashCode() {
        assertEquals(type.hashCode(), NewContainerType.NAME.hashCode());
    }

    @Test
    public void testGetName() {
        assertTrue(type.getName().equals(NewContainerType.NAME));
    }

    @Test
    public void testSupportsEq() {
        assertFalse(type.supportsEq());
    }

    @Test
    public void tettSupportsOrdering() {
        assertFalse(type.supportsOrdering());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCompare() {
        type.compare(new Object(), new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCast() {
        type.cast(new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToJson() {
        type.toJson(JsonNodeFactory.withExactBigDecimals(true), new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFromJson() {
        type.fromJson(JsonNodeFactory.withExactBigDecimals(false).arrayNode());
    }

    @Test
    public void testEqualsObject() {
        assertFalse(type.equals(new Object()));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(type.equals(null));
    }

    @Test
    public void testEqualsFalse() {
        assertTrue(type.equals(type));
    }
}
