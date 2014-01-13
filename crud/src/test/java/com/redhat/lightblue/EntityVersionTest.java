package com.redhat.lightblue;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.EntityVersion;

public class EntityVersionTest {

	EntityVersion entityVersion;
	
	String entity;
	String version;
	
	@Before
	public void setUp() throws Exception {
		entity = "entity";
		version = "version";
		entityVersion = new EntityVersion(entity, version);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHashCode() {
		assertEquals(entityVersion.hashCode(), entity.hashCode() * version.hashCode());
	}
	
	@Test
	public void testHashCodeNull() {
		entityVersion = new EntityVersion();
		assertEquals(entityVersion.hashCode(), 1);
	}

	@Test
	public void testEntityVersion() {
		entityVersion = new EntityVersion();
		assertNull(entityVersion.getEntity());
		assertNull(entityVersion.getVersion());
	}

	@Test
	public void testEntityVersionStringString() {
		entityVersion = new EntityVersion(entity, version);
		assertEquals(entityVersion.getEntity(), entity);
		assertEquals(entityVersion.getVersion(), version);
	}

	@Test
	public void testGetEntity() {
		assertTrue(entityVersion.getEntity().equals(entity));
	}

	@Test
	public void testSetEntity() {
		entityVersion.setEntity(entity+1);
		assertTrue(entityVersion.getEntity().equals(entity+1));
	}

	@Test
	public void testGetVersion() {
		assertTrue(entityVersion.getVersion().equals(version));
	}

	@Test
	public void testSetVersion() {
		entityVersion.setVersion(version+1);
		assertTrue(entityVersion.getVersion().equals(version+1));
	}

	@Test
	public void testEquals() {
		assertTrue(entityVersion.equals(new EntityVersion(entity, version)));
	}

	@Test
	public void testEqualsEmptyObjects() {
		entityVersion = new EntityVersion();
		assertTrue(entityVersion.equals(new EntityVersion()));
	}
	
	@Test
	public void testEqualsNullParam() {
		assertFalse(entityVersion.equals(null));
	}
	
	@Test
	public void testEqualsNullEntity() {
		entityVersion = new EntityVersion(null, version);
		assertTrue(entityVersion.equals(new EntityVersion(null, version)));
	}
	
	@Test
	public void testEqualsNullVersion() {
		entityVersion = new EntityVersion(entity, null);
		assertTrue(entityVersion.equals(new EntityVersion(entity, null)));
	}
	
	@Test
	public void testEqualsEntityDifferent() {
		assertFalse(entityVersion.equals(new EntityVersion("", version)));
	}
	
	@Test
	public void testEqualsVersionDifferent() {
		assertFalse(entityVersion.equals(new EntityVersion(entity, "")));
	}

	@Test
	public void testEqualsVersion() {
		assertTrue(entityVersion.equalsVersion(new EntityVersion(entity, version)));
	}

	@Test
	public void testEqualsVersionNullObject() {
		assertFalse(entityVersion.equalsVersion(null));
	}
	
	@Test
	public void testToStringEntityNull() {
		entityVersion = new EntityVersion(null, version);
		assertTrue(entityVersion.toString().contains(version));
	}
	
	@Test
	public void testToStringVersionNull() {
		entityVersion = new EntityVersion(entity, null);
		assertTrue(entityVersion.toString().contains(entity));
	}
}
