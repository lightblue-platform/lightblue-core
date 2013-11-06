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

import org.junit.Test;
import org.junit.Assert;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class PathResolverTest {
    
    private EntityMetadata getMD1() {
        EntityMetadata md=new EntityMetadata("test");
        md.getFields().addNew(new SimpleField("simpleInteger","integer"));
        md.getFields().addNew(new SimpleField("simpleString","string"));
        ObjectField x=new ObjectField("obj1");
        md.getFields().addNew(x);
        x.getFields().addNew(new SimpleField("nestedSimpleInteger","integer"));
        x.getFields().addNew(new SimpleField("nestedSimpleString","string"));
        ObjectField y=new ObjectField("nested");
        x.getFields().addNew(y);
        y.getFields().addNew(new SimpleField("doubleNestedString","string"));
        ArrayField arr=new ArrayField("simpleArr",new SimpleArrayElement("string"));
        y.getFields().addNew(arr);
        
        ObjectArrayElement oarr=new ObjectArrayElement();
        arr=new ArrayField("objArr",oarr);
        y.getFields().addNew(arr);
        oarr.getFields().addNew(new SimpleField("nestedArrObjString","string"));

        return md;
    }

    @Test
    public void testSimplePath() throws Exception {
        EntityMetadata md=getMD1();
        Assert.assertEquals("simpleInteger",((Field)md.resolve(new Path("simpleInteger"))).getName());
        Assert.assertEquals("simpleString",((Field)md.resolve(new Path("simpleString"))).getName());
    }

    @Test
    public void testObjects() throws Exception {
        EntityMetadata md=getMD1();
        Assert.assertTrue(md.resolve(new Path("obj1")) instanceof ObjectField);
        Assert.assertEquals("nestedSimpleInteger",((Field)md.resolve(new Path("obj1.nestedSimpleInteger"))).getName());
        Assert.assertEquals("nestedSimpleString",((Field)md.resolve(new Path("obj1.nestedSimpleString"))).getName());
    }

    @Test
    public void testNested() throws Exception {
        EntityMetadata md=getMD1();
        Assert.assertTrue(md.resolve(new Path("obj1.nested")) instanceof ObjectField);
        Assert.assertTrue(md.resolve(new Path("obj1.nested.simpleArr")) instanceof ArrayField);
        Assert.assertEquals("doubleNestedString",((Field)md.resolve(new Path("obj1.nested.doubleNestedString"))).getName());
        Assert.assertTrue(md.resolve(new Path("obj1.nested.simpleArr.1")) instanceof SimpleArrayElement);
        Assert.assertTrue(md.resolve(new Path("obj1.nested.simpleArr.*")) instanceof SimpleArrayElement);
    }

    @Test
    public void testNestedArrObj() throws Exception {
        EntityMetadata md=getMD1();
        Assert.assertTrue(md.resolve(new Path("obj1.nested.objArr.1")) instanceof ObjectArrayElement);
        Assert.assertTrue(md.resolve(new Path("obj1.nested.objArr.*")) instanceof ObjectArrayElement);
        Assert.assertEquals("nestedArrObjString",((Field)md.resolve(new Path("obj1.nested.objArr.1.nestedArrObjString"))).getName());
    }

    @Test
    public void testInvalids() throws Exception {
        EntityMetadata md=getMD1();
        invalid(md,"simpleInteger.blah",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"blah",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"simpleInteger.1",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"simpleInteger.*",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"obj1.1",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"obj1.*",Constants.ERR_INVALID_FIELD_REFERENCE);
        invalid(md,"obj1.blah",Constants.ERR_INVALID_FIELD_REFERENCE);
    }

    private void invalid(EntityMetadata md,String p,String errCode) {
        try {
            md.resolve(new Path(p));
            Assert.fail(p);
        } catch (Error x) {
            Assert.assertEquals(p,errCode,x.getErrorCode());
            System.out.println(x.toString());
        }
    }
}
