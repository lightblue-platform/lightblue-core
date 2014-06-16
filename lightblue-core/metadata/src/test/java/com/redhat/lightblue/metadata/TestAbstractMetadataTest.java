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

import com.redhat.lightblue.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Named a bit strangely in case we exclude tests that begin with "Abstract", the test is intended to test functionality
 * defined on the AbstractMetadata class.
 *
 * @author nmalik
 */
public class TestAbstractMetadataTest {

    AbstractMetadata metadata;

    @Before
    public void setup() {
        metadata = new AbstractMetadata() {

            @Override
            protected boolean checkVersionExists(String entityName, String version) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void checkBackendIsValid(EntityInfo md) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Response getDependencies(String entityName, String version) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Response getAccess(String entityName, String version) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public EntityMetadata getEntityMetadata(String entityName, String version) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public EntityInfo getEntityInfo(String entityName) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String[] getEntityNames(MetadataStatus... statuses) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public VersionInfo[] getEntityVersions(String entityName) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void createNewMetadata(EntityMetadata md) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void createNewSchema(EntityMetadata md) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void updateEntityInfo(EntityInfo ei) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setMetadataStatus(String entityName, String version, MetadataStatus newStatus, String comment) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void removeEntity(String entityName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

    }

    @Test
    public void checkVersionIsValid_Valid() {
        // test a bunch of values that should be valid
        String[] values = new String[]{
            "1.0.0",
            "1.1.0",
            "100.344.88999",
            "1.0.0-alpha",
            "1.0.3-SNAPSHOT",
            "1.0.4-100"
        };
        for (String value : values) {
            Assert.assertNotNull(value, metadata.checkVersionIsValid(new Version(value, null, null)));
        }
    }

    @Test
    public void checkVersionIsValid_Invalid() {
        // test a bunch of values that should be invalid
        String[] values = new String[]{
            "1.0.0x",
            "100.344.88.999",
            "1.0.0.alpha",
            "1.0.3.SNAPSHOT",
            "1.0-4-100",
            "bob",
            " 1.0.0",
            "100",
            "1.0"
        };
        for (String value : values) {
            try {
                metadata.checkVersionIsValid(new Version(value, null, null));
                Assert.fail("Expected version to be invalid: " + value);
            } catch (UnsupportedOperationException e) {
                throw e;
            } catch (Exception e) {
                // expected
            }
        }
    }
}
