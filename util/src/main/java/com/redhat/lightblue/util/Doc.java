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

public class Doc<T> {

    private final DocAdapter<T> adapter;
    private final T documentRoot;

    public Doc(DocAdapter<T> adapter, T documentRoot) {
        this.adapter = adapter;
        this.documentRoot = documentRoot;
    }

    public T get(Path p) {
        T current = documentRoot;
        int n = p.numSegments();
        for (int level = 0; level < n; level++) {
            String name = p.head(level);
            if (name.equals(Path.ANY)) {
                if (!adapter.acceptsAny(current))
                    throw new CannotResolvePathException(p);
                current = adapter.getChild(current, name);
            } else {
                try {
                    int index = Integer.valueOf(name);
                    if (!adapter.acceptsIndex(current))
                        throw new CannotResolvePathException(p);
                    current = adapter.getChild(current, index);
                } catch (Exception e) {
                    if (!adapter.acceptsName(current))
                        throw new CannotResolvePathException(p);
                    current = adapter.getChild(current, name);
                }
            }
            if (current == null)
                break;
        }
        return current;
    }

}
