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

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferencesConstraintTest {

    ReferencesConstraint constraint;

    @Before
    public void setUp() throws Exception {
        constraint = new ReferencesConstraint();
        Reference reference = new Reference();
        reference.setEntityField(ReferenceTest.ENTITY_FIELD_VALUE);
        reference.setEntityName(ReferenceTest.ENTITY_NAME_VALUE);
        reference.setVersionValue(ReferenceTest.VERSION_VALUE);
        constraint.setReference(reference);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetType() {
        assertTrue(constraint.getType().equals(ReferencesConstraint.REFERENCES));
    }


    @Test
    public void testSetReference() {
        Reference reference = new Reference();
        reference.setEntityField(ReferenceTest.ENTITY_FIELD_VALUE + 1);
        reference.setEntityName(ReferenceTest.ENTITY_NAME_VALUE + 1);
        reference.setVersionValue(ReferenceTest.VERSION_VALUE + 1);
        constraint.setReference(reference);
        assertTrue(constraint.getReference().equals(reference));
    }

}
