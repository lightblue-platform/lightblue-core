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
