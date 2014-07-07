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
package com.redhat.lightblue.util;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of the registry interface
 */
public class DefaultRegistry<K, V> implements Registry<K, V>, Serializable {
    private final List<Resolver<K, V>> resolvers = new ArrayList<>();
    private final Map<K, V> items = new HashMap<>();

    @Override
    public void add(K key, V value) {
        items.put(key, value);
    }

    @Override
    public void add(Resolver<K, V> resolver) {
        resolvers.add(resolver);
    }

    @Override
    public V find(K name) {
        V value = items.get(name);
        if (value == null) {
            for (Resolver<K, V> x : resolvers) {
                value = x.find(name);
                if (value != null) {
                    break;
                }
            }
        }
        return value;
    }
}
