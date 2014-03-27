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
public class GetEntityNamesCommandTest extends AbstractRestCommandTest {
    @Test
    public void execute() {
        GetEntityNamesCommand command = new GetEntityNamesCommand(null, metadata);

        String output = command.execute();

        Assert.assertNotNull(output);
    }
}
