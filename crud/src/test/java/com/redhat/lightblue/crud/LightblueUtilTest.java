/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.SimpleField;

public class LightblueUtilTest {

    @Test
    public void testIsFieldObjectType_True(){
        assertTrue(LightblueUtil.isFieldObjectType(LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldObjectType_False(){
        assertFalse(LightblueUtil.isFieldObjectType("NOT " + LightblueUtil.FIELD_OBJECT_TYPE));
    }

    @Test
    public void testIsFieldObjectType_NullValue(){
        assertFalse(LightblueUtil.isFieldObjectType(null));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_True(){
        assertTrue(LightblueUtil.doesFieldNameMatchArrayCountPattern("somearray" + LightblueUtil.FIELD_ARRAY_COUNT_POSTFIX));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_NullValue(){
        assertFalse(LightblueUtil.doesFieldNameMatchArrayCountPattern(null));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_False(){
        assertFalse(LightblueUtil.doesFieldNameMatchArrayCountPattern("somearray"));
    }

    @Test
    public void testCreateArrayCountFieldName(){
        assertEquals("somearray#", LightblueUtil.createArrayCountFieldName("somearray"));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_NullValue(){
        assertNull(LightblueUtil.createArrayFieldNameFromCountField(null));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_NotArrayCountField(){
        String nonArrayCountFieldName = "notarraycount";
        assertEquals(nonArrayCountFieldName, LightblueUtil.createArrayFieldNameFromCountField(nonArrayCountFieldName));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_ArrayCountField(){
        String arrayFieldName = "arrayFieldName";
        String arrayCountFieldName = arrayFieldName + LightblueUtil.FIELD_ARRAY_COUNT_POSTFIX;
        assertEquals(arrayFieldName, LightblueUtil.createArrayFieldNameFromCountField(arrayCountFieldName));
    }

    @Test
    public void testIsFieldAnArrayCount_DoesNotMatchArrayCountPattern(){
        assertFalse(LightblueUtil.isFieldAnArrayCount("someField", null));
    }

    @Test
    public void testIsFieldAnArrayCount_DoesNotHaveMatchingArrayField(){
        assertFalse(LightblueUtil.isFieldAnArrayCount("someField" + LightblueUtil.FIELD_ARRAY_COUNT_POSTFIX, new Fields(null)));
    }

    @Test
    public void testIsFieldAnArrayCount_True(){
        String arrayFieldName = "arrayField";

        Fields fields = new Fields(null);
        fields.addNew(new ArrayField(arrayFieldName));

        assertTrue(LightblueUtil.isFieldAnArrayCount(arrayFieldName + LightblueUtil.FIELD_ARRAY_COUNT_POSTFIX, fields));
    }

    @Test
    public void testIsFieldAnArrayCount_Exists_ButNotArrayField(){
        String arrayFieldName = "arrayField";

        Fields fields = new Fields(null);
        fields.addNew(new SimpleField(arrayFieldName));

        assertFalse(LightblueUtil.isFieldAnArrayCount(arrayFieldName + LightblueUtil.FIELD_ARRAY_COUNT_POSTFIX, fields));
    }

}
