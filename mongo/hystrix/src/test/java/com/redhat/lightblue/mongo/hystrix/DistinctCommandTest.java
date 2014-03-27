/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class DistinctCommandTest extends AbstractMongoTest {
    @Test
    public void execute() {
        List values = new DistinctCommand(null, coll, key1).execute();
        
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.contains("obj1"));
        Assert.assertTrue(values.contains("obj2"));
        Assert.assertTrue(values.contains("obj3"));
        Assert.assertTrue(values.contains("obj4"));
    }
}
