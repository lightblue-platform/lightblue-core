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

/**
 * A registry is a mapper that contains key-value pairs and resolver of key/value pairs.
 */
public interface Registry<K,V> extends Resolver<K,V> {
    /**
     * Adds a collection of items to this registry.
     */
    void add(Resolver<K,V> resolver);

    /**
     * Adds an item to the registry
     */
    void add(K name,V value);
}