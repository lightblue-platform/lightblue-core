package com.redhat.lightblue.metadata.types;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class ContainerTypeTest {

//	Type type = new NewContainerType() extends ContainerType {
//		
//	};
	
//    protected ContainerType mConnection = new NewContainerType() extends ContainerType {
//
//    };
	
	Type type;
	
    class NewContainerType extends ContainerType
    {
        public static final String NAME = "NewContainerType";

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
	public void hashCodeTest() {
		assertEquals(type.hashCode(), NewContainerType.NAME.hashCode());
	}

	
	@Test
	public void getNameTest() {
		assertTrue(type.getName().equals(NewContainerType.NAME));
	}

	@Test
	public void supportsEqTest() {
		assertFalse(type.supportsEq());
	}

	@Test
	public void supportsOrderingTest() {
		assertFalse(type.supportsOrdering());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void compareTest() {
		type.compare(new Object(), new Object());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void castTest() {
		type.cast(new Object());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void toJsonTest() {
		type.toJson(JsonNodeFactory.instance.withExactBigDecimals(true), new Object());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void fromJsonTest() {
		type.fromJson(JsonNodeFactory.withExactBigDecimals(false).textNode("test"));
	}

	@Test
	public void equalsObjectTest() {
		assertFalse(type.equals(new Object()));
	}
	
	@Test
	public void equalsNullTest() {
		assertFalse(type.equals(null));
	}

	@Test
	public void equalsFalseTest() {
		assertTrue(type.equals(type));
	}
}
