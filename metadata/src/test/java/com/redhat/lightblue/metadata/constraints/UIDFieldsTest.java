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
package com.redhat.lightblue.metadata.constraints;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldConstraint;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.UIDFields;
import com.redhat.lightblue.metadata.types.UIDType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmalik on 9/16/14.
 */
public class UIDFieldsTest {
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    @Test
    public void uidWithIdentity_initialized() throws Exception {
        // create basic metadata
        EntityMetadata entityMetadata = new EntityMetadata("test");

        // add uid field with identity constraint
        SimpleField f = new SimpleField("simpleUID", UIDType.TYPE);
        List<FieldConstraint> lfc = new ArrayList<>();
        lfc.add(new IdentityConstraint());
        f.setConstraints(lfc);
        entityMetadata.getFields().addNew(f);

        // create document with uid value set
        ObjectNode node = nodeFactory.objectNode();
        node.put("simpleUID", 10);
        JsonDoc doc = new JsonDoc(node);

        // initialize uid fields
        UIDFields.initializeUIDFields(nodeFactory, entityMetadata, doc);

        // verify uid is not null
        Assert.assertNotNull(doc.get(new Path("simpleUID")));
    }

    @Test
    public void uidWithIdentity_notInitialized() throws Exception {
        // create basic metadata
        EntityMetadata entityMetadata = new EntityMetadata("test");

        // add uid field with identity constraint
        SimpleField f = new SimpleField("simpleUID", UIDType.TYPE);
        List<FieldConstraint> lfc = new ArrayList<>();
        lfc.add(new IdentityConstraint());
        f.setConstraints(lfc);
        entityMetadata.getFields().addNew(f);

        // create document with uid value NOT set
        ObjectNode node = nodeFactory.objectNode();
        JsonDoc doc = new JsonDoc(node);

        // initialize uid fields
        UIDFields.initializeUIDFields(nodeFactory, entityMetadata, doc);

        // verify uid is not null
        Assert.assertNotNull(doc.get(new Path("simpleUID")));
    }
}
