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
import com.redhat.lightblue.metadata.types.DateType;
import com.redhat.lightblue.metadata.constraints.UniqueConstraint;
import com.redhat.lightblue.util.Path;

public class PredefinedFieldsTest {
    
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
        entityMetadata.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);

        return entityMetadata;
    }

    private EntityMetadata getMD2() {
        EntityMetadata entityMetadata = new EntityMetadata("test");
        
        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("simpleString", StringType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("object_type",StringType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("_id",IntegerType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.getFields().addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        objectField2.getFields().addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        //arrayField1.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        entityMetadata.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);
        objectField2.getFields().addNew(new SimpleField("objArr#",IntegerType.TYPE));

        return entityMetadata;
    }


    @Test
    public void testAddPredef() throws Exception {
        EntityMetadata md=getMD1();
        try {
            md.resolve(new Path("_id"));
            Assert.fail();
        } catch (Exception e) {}
        try {
            md.resolve(new Path("object_type"));
            Assert.fail();
        } catch (Exception e) {}
        try {
            md.resolve(new Path("simpleArr#"));
        } catch (Exception e) {}
        try {
            md.resolve(new Path("nested.objArr#"));
        } catch (Exception e) {}

        PredefinedFields.ensurePredefinedFields(md);

        Field f=(SimpleField)md.resolve(new Path("_id"));
        Assert.assertEquals(StringType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));
        Assert.assertTrue(md.getConstraints().get(0) instanceof UniqueConstraint);

        f=(SimpleField)md.resolve(new Path("object_type"));
        Assert.assertEquals(StringType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));

        f=(SimpleField)md.resolve(new Path("simpleArr#"));
        Assert.assertEquals(IntegerType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));

        f=(SimpleField)md.resolve(new Path("obj1.nested.objArr#"));
        Assert.assertEquals(IntegerType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));
    }

    @Test
    public void testModPredef() throws Exception {
        EntityMetadata md=getMD2();
        
        PredefinedFields.ensurePredefinedFields(md);

        Field f=(SimpleField)md.resolve(new Path("_id"));
        Assert.assertEquals(IntegerType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));
        Assert.assertTrue(md.getConstraints().get(0) instanceof UniqueConstraint);

        f=(SimpleField)md.resolve(new Path("object_type"));
        Assert.assertEquals(StringType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));

        f=(SimpleField)md.resolve(new Path("simpleArr#"));
        Assert.assertEquals(IntegerType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));

        f=(SimpleField)md.resolve(new Path("obj1.nested.objArr#"));
        Assert.assertEquals(IntegerType.TYPE,f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(Constants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(Constants.ROLE_NOONE));
   }
    
    @Test
    public  void testInvalidId() throws Exception {
        EntityMetadata md= new EntityMetadata("test");
        md.getFields().addNew(new SimpleField("_id", DateType.TYPE));
        try {
            PredefinedFields.ensurePredefinedFields(md);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public  void testInvalidObjectType() throws Exception {
        EntityMetadata md= new EntityMetadata("test");
        md.getFields().addNew(new SimpleField("object_type", IntegerType.TYPE));
        try {
            PredefinedFields.ensurePredefinedFields(md);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public  void testInvalidArr() throws Exception {
        EntityMetadata md= new EntityMetadata("test");
        md.getFields().addNew(new ArrayField("x", new SimpleArrayElement(IntegerType.TYPE)));
        md.getFields().addNew(new SimpleField("x#",StringType.TYPE));
        try {
            PredefinedFields.ensurePredefinedFields(md);
            Assert.fail();
        } catch (Exception e) {}
    }
 }
