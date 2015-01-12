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
