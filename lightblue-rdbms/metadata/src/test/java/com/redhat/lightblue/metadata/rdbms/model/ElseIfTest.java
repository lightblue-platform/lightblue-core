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
package com.redhat.lightblue.metadata.rdbms.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ElseIfTest {

    @Test
    public void testGetSetIf() {
        If expResult = null;
        ElseIf instance = new ElseIf();
        instance.setIf(expResult);
        If result = instance.getIf();
        assertEquals(expResult, result);
        expResult = new IfFieldEmpty();
        instance.setIf(expResult);
        result = instance.getIf();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSetThen() {
        System.out.println("setThen");
        Then expResult = null;
        ElseIf instance = new ElseIf();
        instance.setThen(expResult);
        Then result = instance.getThen();
        assertEquals(expResult, result);
        expResult = new Then();
        instance.setThen(expResult);
        result = instance.getThen();
        assertEquals(expResult, result);
    }
}
