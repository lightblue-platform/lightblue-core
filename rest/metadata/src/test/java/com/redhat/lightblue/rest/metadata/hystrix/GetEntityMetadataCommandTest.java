/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class GetEntityMetadataCommandTest extends AbstractRestCommandTest {
    @Test
    public void execute() {
        GetEntityMetadataCommand command = new GetEntityMetadataCommand(null, metadata, null, null);

        String output = command.execute();

        Assert.assertNotNull(output);
    }

    @Test
    public void executeDefault() {
        GetEntityMetadataCommand command = new GetEntityMetadataCommand(null, metadata, "test", "default");

        String output = command.execute();

        Assert.assertNotNull(output);

        // verify "default" version to translated to null
        Assert.assertNull(metadata.args[1]);
    }
}
