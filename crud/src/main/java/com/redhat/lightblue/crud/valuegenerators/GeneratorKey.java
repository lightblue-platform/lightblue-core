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
package com.redhat.lightblue.crud.valuegenerators;

import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.metadata.ValueGenerator;

class GeneratorKey {

    private final ValueGenerator.ValueGeneratorType type;
    private final String backend;

    public GeneratorKey(ValueGenerator.ValueGeneratorType type, String backend) {
        this.type = type;
        this.backend = backend;
    }

    public boolean equals(Object x) {
        if (x != null) {
            try {
                return ((GeneratorKey) x).type == type
                        && ((backend == null && ((GeneratorKey) x).backend == null)
                        || (backend != null && backend.equals(((GeneratorKey) x).backend)));
            } catch (Exception e) {
            }
        }
        return false;
    }

    public int hashCode() {
        return type.hashCode() * (backend == null ? 1 : backend.hashCode());
    }

}
