/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferenceTest {

    Reference reference;

    public static String ENTITY_FIELD_VALUE = "entityField";
    public static String ENTITY_NAME_VALUE = "entityName";
    public static String VERSION_VALUE = "versionValue";

    @Before
    public void setUp() throws Exception {
        reference = new Reference();
        reference.setEntityField(ENTITY_FIELD_VALUE);
        reference.setEntityName(ENTITY_NAME_VALUE);
        reference.setVersionValue(VERSION_VALUE);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetEntityName() {
        assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE));
    }

    @Test
    public void testSetEntityName() {
        reference.setEntityName(ENTITY_NAME_VALUE + 1);
        assertTrue(reference.getEntityName().equals(ENTITY_NAME_VALUE + 1));
    }

    @Test
    public void testGetVersionValue() {
        assertTrue(reference.getVersionValue().equals(VERSION_VALUE));
    }

    @Test
    public void testSetVersionValue() {
        reference.setVersionValue(VERSION_VALUE + 1);
        assertTrue(reference.getVersionValue().equals(VERSION_VALUE + 1));
    }

    @Test
    public void testGetEntityField() {
        assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE));
    }

    @Test
    public void testSetEntityField() {
        reference.setEntityField(ENTITY_FIELD_VALUE + 1);
        assertTrue(reference.getEntityField().equals(ENTITY_FIELD_VALUE + 1));
    }

}
