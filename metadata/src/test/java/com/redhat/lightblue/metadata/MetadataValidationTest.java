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

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.redhat.lightblue.metadata.types.*;
import com.redhat.lightblue.metadata.constraints.*;

import com.redhat.lightblue.util.Error;

import org.junit.Assert;
import org.junit.Test;

public class MetadataValidationTest {

    @Test
    public void valid() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        com.redhat.lightblue.metadata.Enum enumdef=new  com.redhat.lightblue.metadata.Enum("en");
        Set<String> envalues=new HashSet<>();
        envalues.add("value");
        enumdef.setValues(envalues);
        e.getEntityInfo().getEnums().addEnum(enumdef);

        SimpleField s=new SimpleField("z",StringType.TYPE);
        ArrayList<FieldConstraint> enumsc=new ArrayList<>();
        EnumConstraint enumc=new EnumConstraint();
        enumc.setName("en");
        enumsc.add(enumc);
        s.setConstraints(enumsc);
        e.getFields().put(s);

        s=new SimpleField("x",StringType.TYPE);
        enumsc=new ArrayList<>();
        enumc=new EnumConstraint();
        enumc.setName("en");
        enumsc.add(enumc);
        s.setConstraints(enumsc);
        o.getFields().put(s);
        
        e.validate();
    }

    // Check if a first-level field enum constraint is validated
    @Test
    public void invalid1() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        com.redhat.lightblue.metadata.Enum enumdef=new  com.redhat.lightblue.metadata.Enum("blah");
        Set<String> envalues=new HashSet<>();
        envalues.add("value");
        enumdef.setValues(envalues);
        e.getEntityInfo().getEnums().addEnum(enumdef);

        SimpleField s=new SimpleField("z",StringType.TYPE);
        ArrayList<FieldConstraint> enumsc=new ArrayList<>();
        EnumConstraint enumc=new EnumConstraint();
        enumc.setName("en");
        enumsc.add(enumc);
        s.setConstraints(enumsc);
        e.getFields().put(s);
        try {
            e.validate();
            Assert.fail();
        } catch (Error ex) {}
    }

    // Check if a second-level field enum constraint is validated
    @Test
    public void invalid2() throws Exception {
        EntityMetadata e = new EntityMetadata("testEntity");
        e.setVersion(new Version("1.0.0", null, "some text blah blah"));
        e.setStatus(MetadataStatus.ACTIVE);
        e.getFields().put(new SimpleField("field1", StringType.TYPE));
        ObjectField o = new ObjectField("field2");
        o.getFields().put(new SimpleField("x", IntegerType.TYPE));
        e.getFields().put(o);
        com.redhat.lightblue.metadata.Enum enumdef=new  com.redhat.lightblue.metadata.Enum("blah");
        Set<String> envalues=new HashSet<>();
        envalues.add("value");
        enumdef.setValues(envalues);
        e.getEntityInfo().getEnums().addEnum(enumdef);

        SimpleField s=new SimpleField("x",StringType.TYPE);
        ArrayList<FieldConstraint> enumsc=new ArrayList<>();
        EnumConstraint enumc=new EnumConstraint();
        enumc.setName("en");
        enumsc.add(enumc);
        s.setConstraints(enumsc);
        o.getFields().put(s);
        try {
            e.validate();
            Assert.fail();
        } catch (Error ex) {}
    }
}
