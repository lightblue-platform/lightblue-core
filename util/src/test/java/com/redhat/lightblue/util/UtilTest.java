/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class UtilTest {

    @Test
    public void isNumber_true() {
        Assert.assertTrue(Util.isNumber("0"));
        Assert.assertTrue(Util.isNumber("1"));
        Assert.assertTrue(Util.isNumber("+1"));
        Assert.assertTrue(Util.isNumber("-1"));
        Assert.assertTrue(Util.isNumber("100"));
        Assert.assertTrue(Util.isNumber("+100"));
        Assert.assertTrue(Util.isNumber("-100"));
    }

    @Test
    public void isNumber_false() {
        Assert.assertFalse(Util.isNumber(null));
        Assert.assertFalse(Util.isNumber(""));
        Assert.assertFalse(Util.isNumber("a"));
        Assert.assertFalse(Util.isNumber("1-"));
    }
}
