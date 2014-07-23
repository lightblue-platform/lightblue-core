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

import com.redhat.lightblue.metadata.rdbms.model.IfFieldCheckField;
import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfFieldCheckFieldTest {

    public IfFieldCheckFieldTest() {
    }

    @Test
    public void testGetSetPath1() {
        Path expResult = null;
        IfFieldCheckField instance = new IfFieldCheckField();
        instance.setField(expResult);
        Path result = instance.getField();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setField(expResult);
        result = instance.getField();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSetPath2() {
        Path expResult = null;
        IfFieldCheckField instance = new IfFieldCheckField();
        instance.setRfield(expResult);
        Path result = instance.getRfield();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setRfield(expResult);
        result = instance.getRfield();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSetConditional() {
        String expResult = "$lt";
        IfFieldCheckField instance = new IfFieldCheckField();
        instance.setOp(expResult);
        String result = instance.getOp();
        assertEquals(expResult, result);
        expResult = "$eq";
        instance.setOp(expResult);
        result = instance.getOp();
        assertEquals(expResult, result);
    }
}
