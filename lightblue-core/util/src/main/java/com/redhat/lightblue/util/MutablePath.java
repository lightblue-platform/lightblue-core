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
 * A Path that can be modified. Uses copy-on-write semantics to prevent
 * unnecessary copies.
 */
public class MutablePath extends Path {

    private static final long serialVersionUID = 1L;

    /**
     * If true, this MutablePath is the only Path with a reference to the path
     * data. If false, path data is referenced by other Paths and a deep copy
     * will only be made if data is modified.
     */
    private boolean pathOwned;

    public MutablePath() {
        pathOwned = true;
    }

    /**
     * Shallow copy of the input Path that is converted to a deep copy on first
     * update.
     *
     * @param x
     */
    public MutablePath(Path x) {
        super(x);
        pathOwned = false;
    }

    public MutablePath(MutablePath x) {
        setData(new PathRep(x.getData()));
        pathOwned = true;
    }

    /**
     * Create a mutable path as a prefix of the given path
     *
     * @param x Source path
     * @param pfix If positive, the new path is a prefix of x containing pfix
     * elements. If negative, the new path is a prefix of x with last -pfix
     * elements removed.
     */
    public MutablePath(Path x, int pfix) {
        setData(new PathRep(x.getData(), pfix));
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
        p.setData(new PathRep(getData()));
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
            throw new IllegalArgumentException(UtilConstants.ERR_NULL_VALUE_PASSED_TO_PUSH);
        }
        List<String> s = parse(x);
        if (s != null && !s.isEmpty()) {
            own();
            getData().append(s);
        }
        return this;
    }

    /**
     * Appends x to the end of this
     */
    public MutablePath push(Path x) {
        if (x == null) {
            throw new IllegalArgumentException(UtilConstants.ERR_NULL_VALUE_PASSED_TO_PUSH);
        }
        own();
        getData().append(x.getData());
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
            getData().remove(getData().size() - 1);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException(UtilConstants.ERR_CANT_POP_EMPTY_PATH);
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
            getData().remove(getData().size() - 1);
            getData().append(parse(x));
            return this;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException(UtilConstants.ERR_CANT_SET_LAST_SEGMENT_ON_EMPTY_PATH);
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
        getData().set(i, x);
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
        int l = getData().size();
        while (l > length) {
            getData().remove(--l);
        }
        return this;
    }

    /**
     * Sets the path to empty path
     */
    public MutablePath clear() {
        own();
        getData().clear();
        return this;
    }

    /**
     * Sets this path to p
     */
    public MutablePath set(Path p) {
        return clear().push(p);
    }

    /**
     * If the path is not owned by this instance does a deep copy and marks the
     * path as owned.
     */
    private void own() {
        if (!pathOwned) {
            setData(new PathRep(getData()));
            pathOwned = true;
        }
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof Path) {
            return ((Path) x).getData().equals(getData());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getData().hashCode();
    }

    /**
     * Rewrites the array indexes in the prefix of this path that is common with
     * fullPath, based on the array indexes in that shared prefix
     *
     * @param fullPath A path
     *
     * If      <pre>
     *   fullPath= x.y.1.w.2.k
     *   thisPath = x.y.*.w.*.k.*
     * <pre>
     * Then:
     * <pre>
     *    thisPath.rewriteIndexes(fullPath) -> x.y.1.w.2.k.*
     * </pre>
     *
     * This is useful when interpreting an absolute path derived from metadata
     * in the context of a definite absolute path with no ANYs
     *
     * @return this
     */
    public MutablePath rewriteIndexes(Path fullPath) {
        PathRep thisData = getData();
        PathRep fpData = fullPath.getData();
        int thisSize = thisData.size();
        int fpSize = fpData.size();
        for (int index = 0; index < thisSize && index < fpSize; index++) {
            String thisSeg = thisData.get(index);
            String fpSeg = fpData.get(index);
            if (Path.ANY.equals(thisSeg)) {
                own();
                thisData = getData();
                thisData.set(index, fpSeg);
            } else if (!thisSeg.equals(fpSeg)) {
                break;
            }
        }
        return this;
    }
}
