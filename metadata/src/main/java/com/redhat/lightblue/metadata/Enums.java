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

import com.redhat.lightblue.util.Error;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nmalik
 */
public class Enums implements Serializable {

    private static final long serialVersionUID = 1l;

    private final Map<String, Enum> enums = new HashMap<>();

    /**
     * Sets enums
     */
    public void setEnums(Collection<Enum> l) {
        enums.clear();
        if (l != null) {
            for (Enum x : l) {
                addEnum(x);
            }
        }
    }

    public void addEnum(Enum x) {
        if (enums.containsKey(x.getName())) {
            throw Error.get(MetadataConstants.ERR_DUPLICATE_ENUM, x.getName());
        }
        enums.put(x.getName(), x);
    }

    /**
     * Returns all enums
     */
    @SuppressWarnings("unchecked")
    public Map<String, Enum> getEnums() {
        return (Map<String, Enum>) ((HashMap) enums).clone();
    }

    /**
     * Returns an enum with the given name, or null if it doesn't exist
     */
    public Enum getEnum(String name) {
        return enums.get(name);
    }

    /**
     * Returns if enums list is empty
     */
    public boolean isEmpty() {
        return enums.isEmpty();
    }
}
