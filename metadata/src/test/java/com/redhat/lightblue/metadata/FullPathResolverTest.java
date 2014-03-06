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
import com.redhat.lightblue.util.Path;

public class FullPathResolverTest {

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

    @Test
    public void testGetFullPathSimpleField() {
        Path fullPath = getMD1().resolve(new Path("obj1.nestedSimpleInteger")).getFullPath();
        Assert.assertEquals(new Path("obj1.nestedSimpleInteger"), fullPath);
    }

    @Test
    public void testGetFullPathNestedField() {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.doubleNestedString")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.doubleNestedString"), fullPath);
    }

    @Test
    public void testSimpleRelativePathThis() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.$this.nestedSimpleInteger")).getFullPath();
        Assert.assertEquals(new Path("obj1.nestedSimpleInteger"), fullPath);
    }

    @Test
    public void testNestedRelativePathDoubleThis() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.$this.$this.doubleNestedString")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.doubleNestedString"), fullPath);
    }

    @Test
    public void testNestedRelativePathTripleThis() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.$this.$this.$this.doubleNestedString")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.doubleNestedString"), fullPath);
    }

    @Test
    public void testSimpleRelativePathParent() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.$parent.nestedSimpleInteger")).getFullPath();
        Assert.assertEquals(new Path("obj1.nestedSimpleInteger"), fullPath);
    }

    @Test
    public void testNestedRelativePathDoubleParent() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.doubleNestedString.$parent.$parent.nestedSimpleInteger")).getFullPath();
        Assert.assertEquals(new Path("obj1.nestedSimpleInteger"), fullPath);
    }

    @Test
    public void testNestedRelativePathTripleParent() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.doubleNestedString.$parent.$parent.$parent.simpleInteger")).getFullPath();
        Assert.assertEquals(new Path("simpleInteger"), fullPath);
    }

    @Test
    public void testNestedRelativePathThisSimpleArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.$this.simpleArr")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.simpleArr"), fullPath);
    }

    @Test
    public void testNestedRelativePathThisArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.1.$this.nestedArrObjString1")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.objArr.*.nestedArrObjString1"), fullPath);
    }

    @Test
    public void testNestedRelativePathDoubleThisArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.1.$this.$this.nestedArrObjString1")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.objArr.*.nestedArrObjString1"), fullPath);
    }

    @Test
    public void testNestedRelativePathTripleThisArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.1.$this.$this.$this.nestedArrObjString1")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.objArr.*.nestedArrObjString1"), fullPath);
    }

    @Test
    public void testNestedRelativePathParentArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.$parent.doubleNestedString")).getFullPath();
        Assert.assertEquals(new Path("obj1.nested.doubleNestedString"), fullPath);
    }

    @Test
    public void testNestedRelativePathDoubleParentArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.$parent.$parent.nestedSimpleInteger")).getFullPath();
        Assert.assertEquals(new Path("obj1.nestedSimpleInteger"), fullPath);
    }

    @Test
    public void testNestedRelativePathTripleParentArray() throws Exception {
        Path fullPath = getMD1().resolve(new Path("obj1.nested.objArr.$parent.$parent.$parent.simpleInteger")).getFullPath();
        Assert.assertEquals(new Path("simpleInteger"), fullPath);
    }

}
