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

import com.redhat.lightblue.util.DefaultRegistry;

import com.redhat.lightblue.metadata.ValueGenerator;

import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;

/**
 * Register value generators by type and backend
 */
public class GeneratorsRegistry extends DefaultRegistry<GeneratorKey, ValueGeneratorSupport> {

    public GeneratorsRegistry() {
        registerDefault(UUIDGenerator.instance);
        registerDefault(CurrentTimeGenerator.instance);
    }

    public void register(ValueGenerator.ValueGeneratorType type, String backend, ValueGeneratorSupport g) {
        add(new GeneratorKey(type, backend), g);
    }

    public ValueGeneratorSupport getValueGenerator(ValueGenerator g, String backend) {
        ValueGeneratorSupport generator = find(new GeneratorKey(g.getValueGeneratorType(), backend));
        // If the backend does not have a generator of this type,
        // see if we have one in core
        if (generator == null && backend != null) {
            generator = find(new GeneratorKey(g.getValueGeneratorType(), null));
        }
        return generator;
    }

    private void registerDefault(ValueGeneratorSupport vg) {
        for (ValueGenerator.ValueGeneratorType t : vg.getSupportedGeneratorTypes()) {
            register(t, null, vg);
        }
    }
}
