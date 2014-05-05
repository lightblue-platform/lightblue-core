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
package com.redhat.lightblue.metadata;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;

public class FieldCursorTest {

    private EntityMetadata getMD1() {
        EntityMetadata entityMetadata = new EntityMetadata("test");

        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("simpleString", StringType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.getFields().addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        objectField2.getFields().addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        //arrayField1.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        objectField2.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);

        return entityMetadata;
    }

    // @Test
    // public void sibTest() {
    //     EntityMetadata md=getMD1();
    //     FieldCursor cursor=md.getFieldCursor();
    //     cursor.firstChild();
    //     Assert.assertEquals("simpleInteger",cursor.getCurrentPath().toString());
    //     cursor.nextSibling();
    //     cursor.nextSibling();
    //     Assert.assertEquals("obj1",cursor.getCurrentPath().toString());
    //     cursor.firstChild();
    //     cursor.nextSibling();
    //     cursor.nextSibling();
    //     Assert.assertEquals("obj1.nested",cursor.getCurrentPath().toString());
    //     cursor.firstChild();
    //     cursor.nextSibling();
    //     cursor.nextSibling();
    //     Assert.assertEquals("obj1.nested.objArr",cursor.getCurrentPath().toString());
        
    // }

    @Test
    public void backtrackTest() {
        EntityMetadata md=getMD1();
        FieldCursor cursor=md.getFieldCursor();
        cursor.firstChild();
        cursor.nextSibling();
        cursor.nextSibling();
        cursor.firstChild();
        cursor.nextSibling();
        cursor.nextSibling();
        cursor.firstChild();
        cursor.nextSibling();
        cursor.nextSibling();
        Assert.assertEquals("obj1.nested.objArr",cursor.getCurrentPath().toString());
        cursor.firstChild();
        Assert.assertEquals("obj1.nested.objArr.*",cursor.getCurrentPath().toString());
        cursor.firstChild();
        Assert.assertEquals("obj1.nested.objArr.*.nestedArrObjString1",cursor.getCurrentPath().toString());
        cursor.nextSibling();
        cursor.parent();
        Assert.assertEquals("obj1.nested.objArr.*",cursor.getCurrentPath().toString());
        cursor.firstChild();
        Assert.assertEquals("obj1.nested.objArr.*.nestedArrObjString1",cursor.getCurrentPath().toString());
      
    }
}
