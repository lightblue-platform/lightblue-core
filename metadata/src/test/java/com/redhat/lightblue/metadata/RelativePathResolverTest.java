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

import com.redhat.lightblue.metadata.EntityMetadata.RootTreeNode;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.Path;

public class RelativePathResolverTest {

    private EntityMetadata getMD1() {
        EntityMetadata md = new EntityMetadata("test");
        
        md.addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        md.addNew(new SimpleField("simpleString", StringType.TYPE));
        ObjectField x = new ObjectField("obj1");
        md.addNew(x);
        x.addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        x.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField y = new ObjectField("nested");
        x.addNew(y);
        y.addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arr = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        y.addNew(arr);

        ObjectArrayElement oarr = new ObjectArrayElement();
        arr = new ArrayField("objArr", oarr);
        y.getFields().addNew(arr);
        oarr.getFields().addNew(new SimpleField("nestedArrObjString", StringType.TYPE));
        
        return md;
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNoContextRelativePathThis() throws Exception {
        getMD1().resolve(new Path("$this.nestedSimpleInteger"));
    }
    
    @Test(expected=com.redhat.lightblue.util.Error.class)
    public void testNoContextRelativePathParent() throws Exception {
        getMD1().resolve(new Path("$parent.nestedSimpleInteger"));
    }
    
    @Test
    public void testSimpleRelativePathThis() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.$this.nestedSimpleInteger")));
        Assert.assertEquals("nestedSimpleInteger", found.getName());
    }
    
    @Test
    public void testNestedRelativePathThis() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.nested.$this.doubleNestedString")));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test
    public void testNestedRelativePathDoubleThis() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.nested.$this.$this.doubleNestedString")));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test
    public void testNestedRelativePathTripleThis() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.nested.$this.$this.$this.doubleNestedString")));
        Assert.assertEquals("doubleNestedString", found.getName());
    }
    
    @Test
    public void testSimpleRelativePathParent() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.$parent.simpleInteger")));
        Assert.assertEquals("simpleInteger", found.getName());
    }
    
    @Test
    public void testNestedRelativePathParent() throws Exception {
        EntityMetadata md = getMD1();
        Field found = (Field) ((Field) md.resolve(new Path("obj1.nested.$parent.nestedSimpleInteger")));
        Assert.assertEquals("nestedSimpleInteger", found.getName());
    }
}
