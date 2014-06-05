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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public abstract class AbstractDefaultRegistryTest<K, V> {

    protected abstract DefaultRegistry<K, V> createRegistery();

    protected abstract K createKey();

    protected abstract V createValue();

    @Test
    public void addKV_single() {
        DefaultRegistry<K, V> registery = createRegistery();
        K key = createKey();
        V value = createValue();
        registery.add(key, value);

        V found = registery.find(key);

        Assert.assertEquals(value, found);
    }

    @Test
    public void addKV_multiple() {
        Map<K, V> kvMap = new HashMap<>();
        DefaultRegistry<K, V> registery = createRegistery();
        for (int i = 0; i < 1000; i++) {
            K key = createKey();
            V value = createValue();
            if (!kvMap.containsKey(key)) {
                kvMap.put(key, value);
                registery.add(key, value);
            }
        }

        Assert.assertTrue(kvMap.size() > 500);

        for (K key : kvMap.keySet()) {
            V expected = kvMap.get(key);
            V found = registery.find(key);

            Assert.assertEquals(expected, found);
        }
    }

    @Test
    public void addResolver_single() {
        DefaultRegistry<K, V> registery = createRegistery();
        final K key = createKey();
        final V value = createValue();
        Resolver<K, V> resolver = new Resolver<K, V>() {

            @Override
            public V find(K name) {
                if (key.equals(name)) {
                    return value;
                }
                return null;
            }
        };

        registery.add(resolver);

        V found = registery.find(key);

        Assert.assertEquals(value, found);
    }

    @Test
    public void addResolver_multiple() {
        final Map<K, V> kvMap = new HashMap<>();
        DefaultRegistry<K, V> registery = createRegistery();
        DefaultResolver<K, V> resolver = new DefaultResolver<>();
        for (int i = 0; i < 1000; i++) {
            K key = createKey();
            V value = createValue();
            if (!kvMap.containsKey(key)) {
                kvMap.put(key, value);
                resolver.addValue(key, value);
            }
        }

        Assert.assertTrue(kvMap.size() > 500);

        registery.add(resolver);

        for (K key : kvMap.keySet()) {
            V expected = kvMap.get(key);
            V found = registery.find(key);

            Assert.assertEquals(expected, found);
        }
    }

    @Test
    public void find() {
        // test when things are added by both K/V and resolver
        Map<K, V> kvMap = new HashMap<>();
        DefaultRegistry<K, V> registery = createRegistery();
        for (int i = 0; i < 1000; i++) {
            K key = createKey();
            V value = createValue();
            if (!kvMap.containsKey(key)) {
                kvMap.put(key, value);
                registery.add(key, value);
            }
        }

        Assert.assertTrue(kvMap.size() > 500);

        final Map<K, V> resolverMap = new HashMap<>();
        DefaultResolver<K, V> resolver = new DefaultResolver<>();

        for (int i = 0; i < 1000; i++) {
            K key = createKey();
            V value = createValue();
            // don't add if it exists in either kv or resolver maps. this ensures hits on resolver don't fail if duplicated in kv map
            if (!kvMap.containsKey(key) && !resolverMap.containsKey(key)) {
                resolverMap.put(key, value);
                resolver.addValue(key, value);
            }
        }

        Assert.assertTrue(resolverMap.size() > 500);

        registery.add(resolver);

        for (K key : kvMap.keySet()) {
            V expected = kvMap.get(key);
            V found = registery.find(key);

            Assert.assertEquals(expected, found);
        }

        for (K key : resolverMap.keySet()) {
            V expected = resolverMap.get(key);
            V found = registery.find(key);

            Assert.assertEquals(expected, found);
        }

    }
}
