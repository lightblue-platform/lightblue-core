/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud;

import com.redhat.lightblue.mediator.Mediator;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class CrudManagerTest {
    @Test
    public void getMediator() throws Exception {
        Mediator m = CrudManager.getMediator();
        Assert.assertNotNull(m);
    }
}
