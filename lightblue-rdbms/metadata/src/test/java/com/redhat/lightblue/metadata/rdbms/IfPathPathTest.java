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
package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfPathPathTest {

    public IfPathPathTest() {
    }

    @Test
    public void testGetSetPath1() {
        Path expResult = null;
        IfPathPath instance = new IfPathPath();
        instance.setPath1(expResult);
        Path result = instance.getPath1();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setPath1(expResult);
        result = instance.getPath1();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSetPath2() {
        Path expResult = null;
        IfPathPath instance = new IfPathPath();
        instance.setPath2(expResult);
        Path result = instance.getPath2();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setPath2(expResult);
        result = instance.getPath2();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSetConditional() {
        String expResult = "lessThan";
        IfPathPath instance = new IfPathPath();
        instance.setConditional(expResult);
        String result = instance.getConditional();
        assertEquals(expResult, result);
        expResult = "equalTo";
        instance.setConditional(expResult);
        result = instance.getConditional();
        assertEquals(expResult, result);
    }
}
