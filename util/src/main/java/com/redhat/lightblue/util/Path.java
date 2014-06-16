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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a path in a tree, of the form
 *
 * <pre>
 *   field1.field2.index.field3...
 * </pre>
 *
 * where fields are identifiers, and indexes are integers denoting array indexes.
 *
 * Paths can be used to represent fields as well as patterns. A pattern path includes '*' for any matching index. For
 * instance:
 *
 * <pre>
 *    user.address.1.city
 * </pre>
 *
 * denotes the city of the second address of the user, whereas
 *
 * <pre>
 *    user.address.*.city
 * </pre>
 *
 * matches all cities of addresses.
 *
 * Implementation is optimized to be fast to toString and hashCode, and does not occupy too much memory when a lot of
 * paths are created from a common prefix.
 *
 * $parent and $this keywords can be used to create relative paths.
 * <pre>
 *   $this.address // 'address' field relative to this
 *   $parent.address // 'address' field relative to parent
 *   $parent.$parent.address // 'address' field relative to grandparent
 *   $parent.address.$parent.city // 'city' relative to parent of 'address' which is relative to parent
 *   $parent.address.$parent.$parent.login // 'login' field relative to grand parent of address, which is relative to parent of this
 *   address.city.$this // points to 'address'
 * </pre>
 *
 *
 * Path objects are immutable. Use MutablePath for modifiable paths.
 */
public class Path implements Comparable<Path>, Serializable {

    private static final long serialVersionUID = 1l;

    public static final String ANY = "*";

    public static final String PARENT = "$parent";
    public static final String THIS = "$this";

    public static final Path EMPTY = new Path();
    public static final Path ANYPATH = new Path(ANY);

    private PathRep data;

    /**
     * Constructs empty path
     */
    public Path() {
        data = new PathRep();
    }

    /**
     * Constructs a copy of x
     */
    public Path(Path x) {
        data = new PathRep(x.data);
    }

    /**
     * Constructs a path with x+y
     */
    public Path(Path x, Path y) {
        data = new PathRep(x.data);
        data.append(y.data);
    }

    /**
     * Create a mutable path as a prefix of the given path
     *
     * @param x Source path
     * @param pfix If positive, the new path is a prefix of x containing pfix elements. If negative, the new path is a
     * prefix of x with last -pfix elements removed.
     */
    public Path(Path x, int pfix) {
        data = new PathRep(x.data, pfix);
    }

    public Path(String x) {
        this();
        List<String> s = parse(x);
        data.append(s);
    }

    protected void setData(PathRep pr) {
        data = pr;
    }

    protected PathRep getData() {
        return data;
    }

    /**
     * Create a deep copy of this Path.
     *
     * @return the new path
     */
    public Path copy() {
        return new Path(this);
    }

    /**
     * Create an immutable shallow copy of this path.
     *
     * @return
     */
    public Path immutableCopy() {
        return this;
    }

    /**
     * Create a mutable shallow copy of this path.
     *
     * @return
     */
    public MutablePath mutableCopy() {
        return new MutablePath(this);
    }

    /**
     * Get the last path segment.
     *
     * @return
     */
    public String getLast() {
        return tail(0);
    }

    /**
     * Get the number of path segments.
     *
     * @return
     */
    public int numSegments() {
        return data.size();
    }

    /**
     * Check if path is empty
     */
    public boolean isEmpty() {
        return data.size() == 0;
    }

    /**
     * Get path segment at given index starting from the head of the segment list.
     *
     * @param i the index
     * @return the path segment
     */
    public String head(int i) {
        return data.get(i);
    }

    /**
     * Get path segment at given index starting from the tail of the segment list.
     *
     * @param i
     * @return the segment data
     */
    public String tail(int i) {
        return data.get(data.size() - 1 - i);
    }

    /**
     * Get the array index represented by the given path segment index (relative to head).
     *
     * @param i
     * @return
     */
    public int getIndex(int i) {
        return Integer.valueOf(data.get(i));
    }

    /**
     * Check if path segment at given index (relative to head) is an array index.
     *
     * @param i
     * @return
     */
    public boolean isIndex(int i) {
        return Util.isNumber(data.get(i));
    }

    /**
     * Returns the number of ANY elements in the path
     */
    public int nAnys() {
        int n = 0;
        for (Iterator<String> itr = data.iterator(); itr.hasNext();) {
            if (ANY.equals(itr.next())) {
                n++;
            }
        }
        return n;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    /**
     * Returns a new path that is a prefix of this path obtained by removing -x elements from the end (if x is
     * negative), or selecting x elements from the beginning (if x is positive). If the path is a mutable path, the
     * returned path is a mutable path. If the path is an immutable path, the returned path is an immutable path.
     *
     * @param x number of elements to remove from end, or include from the beginning
     * @return the new path
     * @throws IndexOutOfBoundException number of elements to remove is greater than number of segments available
     */
    public Path prefix(int x) {
        Path p;
        if (this instanceof MutablePath) {
            p = new MutablePath((MutablePath) this, x);
        } else {
            p = new Path(this, x);
        }
        return p;
    }

    /**
     * Returns a new path that is a suffix of this path obtained by removing -x elements from the beginning (if x is
     * negative), or selecting x elements from the end (if x is positive). If the path is a mutable path, the returned
     * path is a mutable path.
     */
    public Path suffix(final int x) {
        Path p;
        if (this instanceof MutablePath) {
            p = new MutablePath((MutablePath) this);
        } else {
            p = new Path(this);
        }
        int n = p.data.size();
        if (x >= 0) {
            p.data.shiftLeft(n - Math.min(n, x));
        } else {
            p.data.shiftLeft(Math.min(n, Math.abs(x)));
        }

        return p;
    }

    /**
     * Check if this path matches the path pattern argument passed in.
     *
     * @param pattern the pattern path
     * @return true if it matches, else false
     */
    public boolean matches(Path pattern) {
        int n = data.size();
        if (n == pattern.data.size()) {
            for (int i = 0; i < n; i++) {
                String pat = pattern.data.get(i);
                String val = data.get(i);
                if (!(val.equals(pat) || pat.equals(ANY))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if this path is a matching descendant of the pattern, that is: - path matches pattern, or - a prefix of the
     * path matches the pattern
     */
    public boolean matchingDescendant(Path pattern) {
        int n = pattern.numSegments();
        if (n < numSegments()) {
            return prefix(n).matches(pattern);
        } else if (n == numSegments()) {
            return matches(pattern);
        }
        return false;
    }

    /**
     * Check if this path is a matching prefix of the pattern, that is: - path matches pattern, or - path matches a
     * prefix of the pattern
     */
    public boolean matchingPrefix(Path pattern) {
        int n = pattern.numSegments();
        if (n > numSegments()) {
            return matches(pattern.prefix(numSegments()));
        } else if (n == numSegments()) {
            return matches(pattern);
        }
        return false;
    }

    /**
     * Returns this+p
     */
    public Path add(Path p) {
        return new Path(this, p);
    }

    /**
     * Returns a path with all $parent and $this references removed. This must be called on absolute paths
     */
    public Path normalize() {
        int n = data.size();
        boolean parentThisPresent = false;
        for (int i = 0; i < n; i++) {
            String s = data.get(i);
            if (PARENT.equals(s) || THIS.equals(s)) {
                parentThisPresent = true;
                break;
            }
        }
        if (parentThisPresent) {
            MutablePath p = new MutablePath();
            for (int i = 0; i < n; i++) {
                String s = data.get(i);
                if (PARENT.equals(s)) {
                    p.pop();
                } else if (!THIS.equals(s)) {
                    p.push(s);
                }
            }
            return p.immutableCopy();
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof Path) {
            return ((Path) x).data.equals(data);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Path x) {
        return x == null ? -1 : data.compareTo(x.data);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    /**
     * Parses the input path string (x) and appends each segment to the segments argument.
     *
     * @param x the new paths segments to parse
     * @param segments the segment list to append new segments onto
     */
    protected static List<String> parse(String x) {
        List<String> segments = new ArrayList<>();
        StringBuilder buf = new StringBuilder(32);
        int state = parsePath(buf, x, segments);
        if (state == 1) {
            segments.add(buf.toString());
        }
        return segments;
    }

    private static int parsePath(StringBuilder buf, String x, List<String> segments) {
        int state = 0;
        for (int i = 0; i < x.length(); i++) {
            char c = x.charAt(i);
            state = parsePathElements(state, c, buf, i, x, segments);
        }
        return state;
    }

    private static int parsePathElements(int state, char c, StringBuilder buf, int i, String x, List<String> segments) {
        switch (state) {
            case 0:
                // Beginning of path, or after .
                state = parsePathBeginningOrAfter(c, buf, i, x, state);
                break;
            case 1:
                // Parsing word
                state = parseWord(c, buf, segments, state);
                break;
            case 2:
                // Parsing end of word
                state = parseEndOfWord(c, i, x);
                break;
            default:
                state = 0;
        }
        return state;
    }

    private static int parsePathBeginningOrAfter(char c, StringBuilder buf, int i, String x, int currentState) {
        if (!Character.isWhitespace(c)) {
            if (c == '.') {
                throw new InvalidPathException(UtilConstants.ERR_UNEXPECTED_DOT + i, x);
            } else {
                buf.append(c);
                return 1;
            }
        }
        return currentState;
    }

    private static int parseWord(char c, StringBuilder buf, List<String> segments, int currentState) {
        if (Character.isWhitespace(c)) {
            segments.add(buf.toString());
            buf.setLength(0);
            buf.trimToSize();
            return 2;
        } else if (c == '.') {
            segments.add(buf.toString());
            buf.setLength(0);
            buf.trimToSize();
            return 0;
        } else {
            buf.append(c);
        }
        return currentState;
    }

    private static int parseEndOfWord(char c, int i, String x) {
        if (!Character.isWhitespace(c)) {
            if (c == '.') {
                return 0;
            } else {
                throw new InvalidPathException(UtilConstants.ERR_UNEXPECTED_WHITESPACE + i, x);
            }
        } else {
            throw new InvalidPathException(UtilConstants.ERR_UNEXPECTED_CHARACTER + i, x);
        }
    }

}
