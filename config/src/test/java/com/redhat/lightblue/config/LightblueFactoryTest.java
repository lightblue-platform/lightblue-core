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
package com.redhat.lightblue.config;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.mediator.Mediator;
import org.junit.Assert;
import org.junit.Test;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author nmalik
 */
public class LightblueFactoryTest {
    @Test
    public void getMetadata() throws Exception {
        LightblueFactory mgr = new LightblueFactory(new DataSourcesConfiguration());
        Metadata m = mgr.getMetadata();
        Assert.assertNotNull(m);
        Assert.assertTrue(m instanceof DatabaseMetadata);
    }

    @Test
    public void getMediator() throws Exception {
        DataSourcesConfiguration ds = new DataSourcesConfiguration();
        LightblueFactory mgr = new LightblueFactory(new DataSourcesConfiguration());

        Mediator m = mgr.getMediator();
        Assert.assertNotNull(m);
    }

    @Test
    public void crudConfigurationIsLoaded() throws Exception {
        Factory factory = new LightblueFactory(new DataSourcesConfiguration()).getFactory();
        Assert.assertEquals(51, factory.getMaxResultSetSizeForReadsB());
        Assert.assertEquals(52, factory.getMaxResultSetSizeForWritesB());
        Assert.assertEquals(53, factory.getMaxExecutionContextSizeForCompositeFindB());
        Assert.assertEquals(54, factory.getWarnResultSetSizeB());
    }
}
