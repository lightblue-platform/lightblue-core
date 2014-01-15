/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import com.redhat.lightblue.metadata.MetadataManager;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class MetadataManagerTest {
    @Test
    public void getMetadata() throws Exception {
        Metadata m = MetadataManager.getMetadata();
        Assert.assertNotNull(m);
        Assert.assertTrue(m instanceof DatabaseMetadata);
    }
}
