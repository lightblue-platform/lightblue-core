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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Internal representation of Path
 */
class PathRep implements Serializable, Comparable<PathRep> {
    private static final long serialVersionUID = 1l;

    private final List<String> segments;

    private transient String stringValue = null;
    private transient int hashValue = 0;

    /**
     * Creates an empty path
     */
    public PathRep() {
        segments = new ArrayList<>(10);
    }

    /**
     * Copy ctor
     */
    public PathRep(PathRep data) {
        segments = new ArrayList<>(data.segments);
        stringValue = data.stringValue;
        hashValue = data.hashValue;
    }

    /**
     * Prefix copy ctor
     *
     * @param data source
     * @param x If x>0, x elements from the beginning are copied. If x<0, -x
     * elements from the end are removed
     */
    public PathRep(PathRep data, int x) {
        int k = data.segments.size();
        segments = new ArrayList<>(k);
        int n;
        if (x >= 0) {
            n = k > x ? x : k;
        } else {
            n = k + x;
        }
        for (String s : data.segments) {
            if (n <= 0) {
                break;
            }
            segments.add(s);
            n--;
        }
    }

    /**
     * Clears the path
     */
    public void clear() {
        segments.clear();
        resetState();
    }

    /**
     * Resets the cached values for string value and hashcode
     */
    public void resetState() {
        stringValue = null;
        hashValue = 0;
    }

    /**
     * Returns the number of segments
     */
    public int size() {
        return segments.size();
    }

    /**
     * Returns the element at the index
     */
    public String get(int index) {
        return segments.get(index);
    }

    /**
     * Removes the element at index
     */
    public void remove(int index) {
        segments.remove(index);
        resetState();
    }

    /**
     * Sets the element at index
     */
    public void set(int index, String x) {
        segments.set(index, x);
        resetState();
    }

    /**
     * Returns an iterator over segments
     */
    public Iterator<String> iterator() {
        return segments.iterator();
    }

    @Override
    public int hashCode() {
        if (hashValue == 0) {
            hashValue = segments.hashCode();
        }
        return hashValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PathRep) {
            PathRep r = (PathRep) o;
            return r.segments.equals(segments);
        }
        return false;
    }

    /**
     * Removes 'from' elements from the beginning
     */
    public void shiftLeft(final int from) {
        if (from > 0) {
            int n = segments.size();
            if (from >= n) {
                segments.clear();
            } else {
                int iFrom = from;
                int k = n - iFrom;
                int to = 0;
                for (int i = 0; i < k; i++) {
                    segments.set(to++, segments.get(iFrom++));
                }
                k = n - k;
                for (int i = 0; i < k; i++) {
                    segments.remove(--n);
                }
            }
            resetState();
        }
    }

    /**
     * Appends p to the end of this
     */
    public void append(PathRep p) {
        append(p.segments);
        resetState();
    }

    /**
     * Appends the string segments to the end of this
     */
    public void append(List<String> x) {
        segments.addAll(x);
        resetState();
    }

    @Override
    public int compareTo(PathRep x) {
        int tn = segments.size();
        int xn = x.segments.size();
        int n = tn > xn ? xn : tn;
        int index = 0;
        while (index < n) {
            int cmp = segments.get(index).compareTo(x.segments.get(index));
            if (cmp != 0) {
                return cmp;
            }
            index++;
        }
        return tn - xn;
    }

    @Override
    public String toString() {
        if (stringValue == null) {
            StringBuilder buf = new StringBuilder(segments.size() * 8);
            boolean first = true;
            for (String x : segments) {
                if (first) {
                    first = false;
                } else {
                    buf.append('.');
                }
                buf.append(x);
            }
            stringValue = buf.toString();
        }
        return stringValue;
    }
}
