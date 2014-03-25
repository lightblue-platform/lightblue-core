/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic information about a dependency between entities.
 *
 * @author nmalik
 */
public class Dependency {
    private final String name;
    private final String version;
    private final ArrayList<Dependency> dependencies = new ArrayList<>();

    /**
     * Create with name and version set.
     *
     * @param name the name of the entity
     * @param version the version of the entity
     */
    public Dependency(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Create with name and version and initialize with list of dependencies.
     *
     * @param name
     * @param version
     * @param deps
     */
    public Dependency(String name, String version, List<Dependency> deps) {
        this(name, version);
        if (null != deps && !deps.isEmpty()) {
            this.dependencies.addAll(deps);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }

    /**
     * @return copy of the dependencies list
     */
    public List<Dependency> getDependencies() {
        return (List<Dependency>) dependencies.clone();
    }
}
