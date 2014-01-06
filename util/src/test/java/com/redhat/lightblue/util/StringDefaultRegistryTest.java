/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

/**
 *
 * @author nmalik
 */
public class StringDefaultRegistryTest extends AbstractDefaultRegistryTest<String, String> {
    @Override
    protected DefaultRegistry<String, String> createRegistery() {
        return new DefaultRegistry<>();
    }

    @Override
    protected String createKey() {
        return Integer.toString((int) (Math.random() * 100000.0));
    }

    @Override
    protected String createValue() {
        return Integer.toString((int) (Math.random() * 10000000.0));
    }
}
