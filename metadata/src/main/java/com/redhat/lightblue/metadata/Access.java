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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps a set of roles that can perform a certain operation
 */
public class Access implements Serializable {

    private static final long serialVersionUID = 1l;

    private final HashSet<String> values = new HashSet<String>();

    /**
     * Default ctor
     */
    public Access() {
    }

    /**
     * Sets the roles. The given collection contents are copied to internal storage.
     */
    public void setRoles(Collection<String> roles) {
        values.clear();
        if (roles != null) {
            values.addAll(roles);
        }
    }

    /**
     * Retrieves the roles. A copy of the internal storage is returned.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRoles() {
        return (Set<String>) values.clone();
    }
}
