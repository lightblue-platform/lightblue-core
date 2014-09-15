package com.redhat.lightblue.metadata.constraints;

import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.Enum;
import com.redhat.lightblue.util.Error;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests specific to entity metadata.
 * <p/>
 * Created by nmalik on 9/15/14.
 */
public class EntityMetadataTest {

    @Test
    public void invalidEnumConstraintValue() throws Exception {
        // create new entity metadata with enum name mismatch between info and schema.
        EntityMetadata em = new EntityMetadata("test");

        // add enum in info with name of "status"
        em.getEntityInfo().getEnums().addEnum(new Enum("status"));

        // add field in schema with name of "statusInvalid"
        Field f = new SimpleField("status");
        EnumConstraint ec = new EnumConstraint();
        ec.setName("statusInvalid");
        List<FieldConstraint> lfc = new ArrayList<>();
        lfc.add(ec);
        f.setConstraints(lfc);

        em.getEntitySchema().getFields().addNew(f);

        // verify
        try {
            em.validate();

            // expected failure!
            Assert.fail("Expected validation failure!");
        } catch (Error e) {
            // this is expected, check the status code
            Assert.assertEquals(MetadataConstants.ERR_INVALID_ENUM, e.getErrorCode());
        }
    }
}
