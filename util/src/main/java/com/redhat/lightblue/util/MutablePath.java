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

import java.util.List;

/**
 * A Path that can be modified. Uses copy-on-write semantics to prevent unnecessary copies.
 */
public class MutablePath extends Path {
    
    private static final long serialVersionUID = 1L;
	
    /**
     * If true, this MutablePath is the only Path with a reference to the path data. If false, path data is referenced
     * by other Paths and a deep copy will only be made if data is modified.
     */
    private boolean pathOwned;

    public MutablePath() {
        pathOwned = true;
    }

    /**
     * Shallow copy of the input Path that is converted to a deep copy on first update.
     *
     * @param x
     */
    public MutablePath(Path x) {
        super(x);
        pathOwned = false;
    }

    public MutablePath(MutablePath x) {
        data=new PathRep(x.data);
        pathOwned = true;
    }

    /**
     * Create a mutable path as a prefix of the given path
     *
     * @param x Source path
     * @param pfix If positive, the new path is a prefix of x containing pfix elements. If negative, the new path is a
     * prefix of x with last -pfix elements removed.
     */
    public MutablePath(Path x, int pfix) {
        data=new PathRep(x.data, pfix);
        pathOwned = true;
    }

    public MutablePath(String x) {
        super(x);
        pathOwned = true;
    }

    @Override
    public MutablePath copy() {
        return new MutablePath(this);
    }

    @Override
    public Path immutableCopy() {
        Path p = new Path();
        p.data=new PathRep(data);
        return p;
    }

    /**
     * Appends the given path to the current path segments.
     *
     * @param x the new path segments
     * @return the updated path
     */
    public MutablePath push(String x) {
        if (x == null) {
            throw new IllegalArgumentException("null value passed to push");
        }
        List<String> s = parse(x);
        if(s!=null&&!s.isEmpty()) {
            own();
            data.append(s);
        }
        return this;
    }

    /**
     * Appends x to the end of this
     */
    public MutablePath push(Path x) {
        if (x == null) {
            throw new IllegalArgumentException("null value passed to push");
        }
        own();
        data.append(x.data);
        return this;
    }

    /**
     * Appends the given integer (array index) to the current path segments.
     *
     * @param x the array index
     * @return the updated path
     */
    public MutablePath push(int x) {
        return push(Integer.toString(x));
    }

    /**
     * Remove the last path segment from this path.
     *
     * @return the updated path
     */
    public MutablePath pop() {
        try {
            own();
            data.remove(data.size()-1);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Attempted to pop empty path");
        }
        return this;
    }

    /**
     * Replace the last path segment with the path supplied.
     *
     * @param x the new end of the path
     * @return the updated path
     */
    public Path setLast(String x) {
        try {
            own();
            data.remove(data.size()-1);
            data.append(parse(x));
            return this;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Attempted to set last segment on empty path.");
        }
    }

    /**
     * Replace the last path segment with the given integer (array index).
     *
     * @param x the array index
     * @return the updated path
     */
    public Path setLast(int x) {
        return setLast(String.valueOf(x));
    }

    public Path set(int i, String x) {
        own();
        data.set(i, x);
        return this;
    }

    public Path set(int i, int value) {
        return set(i, Integer.toString(value));
    }

    /**
     * Reduces the length of the path to the given length
     */
    public MutablePath cut(int length) {
        own();
        int l = data.size();
        while (l > length) {
            data.remove(--l);
        }
        return this;
    }

    /**
     * If the path is not owned by this instance does a deep copy and marks the path as owned.
     */
    private void own() {
        if (!pathOwned) {
            data=new PathRep(data);
            pathOwned = true;
        }
    }
}
