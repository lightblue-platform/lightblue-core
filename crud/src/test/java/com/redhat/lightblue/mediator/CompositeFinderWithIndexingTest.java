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
package com.redhat.lightblue.mediator;

import org.junit.After;
import org.junit.Before;

import com.redhat.lightblue.assoc.ep.Assemble;

public class CompositeFinderWithIndexingTest extends CompositeFinderTest {

    @Before
    public void setup() {
        // Force use of mem index for all cases
        Assemble.MEM_INDEX_THRESHOLD=0;
    }
    @After
    public void restore() {
        // Force use of mem index for all cases
        Assemble.MEM_INDEX_THRESHOLD=16;
    }
}
