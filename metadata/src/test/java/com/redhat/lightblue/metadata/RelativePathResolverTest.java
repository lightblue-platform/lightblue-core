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

public class RelativePathResolverTest {

    private EntityMetadata getMD1() {
        EntityMetadata entityMetadata = new EntityMetadata("test");
        
        entityMetadata.addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.addNew(new SimpleField("simpleString", StringType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.addNew(objectField1);
        objectField1.addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.addNew(objectField2);
        objectField2.addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        objectField2.addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        //objectArrayElement.setParent(objectField2);
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.addNew(arrayField2);
        objectArrayElement.addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        
        return entityMetadata;
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNoContextRelativePathThis() throws Exception {
        getMD1().resolve(new Path("$this.nestedSimpleInteger"));
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNoContextRelativePathParent() throws Exception {
        getMD1().resolve(new Path("$parent.nestedSimpleInteger"));
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testSimpleRelativePathThisNotFound() throws Exception {
        getMD1().resolve(new Path("obj1.$this.nonExistantField"));
    }
    
    @Test
    public void testSimpleRelativePathThis() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.$this.nestedSimpleInteger"));
        Assert.assertEquals("nestedSimpleInteger", found.getName());
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNestedRelativePathThisNotFound() throws Exception {
        getMD1().resolve(new Path("obj1.nested.$this.nonExistantField"));
    }
    
    @Test
    public void testNestedRelativePathThis() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$this.doubleNestedString"));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test
    public void testNestedRelativePathDoubleThis() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$this.$this.doubleNestedString"));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test
    public void testNestedRelativePathTripleThis() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$this.$this.$this.doubleNestedString"));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testSimpleRelativePathParentNotFound() throws Exception {
         getMD1().resolve(new Path("obj1.$parent.nonExistantField"));
    }
    
    @Test
    public void testSimpleRelativePathParent() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$parent.nestedSimpleInteger"));
        Assert.assertEquals("nestedSimpleInteger", found.getName());
    }
     
    @Test
    public void testSimpleRelativePathDoubleParent() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$parent.$parent.simpleInteger"));
        Assert.assertEquals("simpleInteger", found.getName());
    }
    
    @Test
    public void testNestedRelativePathParent() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$parent.nestedSimpleInteger"));
        Assert.assertEquals("nestedSimpleInteger", found.getName());
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNestedRelativePathParentNotFound() throws Exception {
        getMD1().resolve(new Path("obj1.nested.$parent.nonExistantField"));
    }
    
    @Test
    public void testNestedRelativePathDoubleParent() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$parent.$parent.simpleInteger"));
        Assert.assertEquals("simpleInteger", found.getName());
    }
    
    @Test
    public void testNestedRelativePathTripleParent() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$parent.$parent.$parent.simpleInteger"));
        Assert.assertEquals("simpleInteger", found.getName());
    }
    
    @Test
    public void testNestedRelativePathThisArray() throws Exception {
        FieldTreeNode found = getMD1().resolve(new Path("obj1.nested.$this.simpleArr"));
        Assert.assertEquals("simpleArr", found.getName());
    }

}
