package com.redhat.lightblue.metadata.constraints;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
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
