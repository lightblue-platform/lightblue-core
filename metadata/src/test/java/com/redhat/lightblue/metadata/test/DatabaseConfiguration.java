/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata.test;

/**
 *
 * @author nmalik
 */
public class DatabaseConfiguration {
    private String name;
    private String hostname = "localhost";
    private String port = "27017";
    private String collection = "metadata";

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getCollection() {
        return collection;
    }

}
