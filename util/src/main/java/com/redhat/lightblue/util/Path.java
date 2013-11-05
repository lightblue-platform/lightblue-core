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

/**
 * Represents a path in a tree, of the form
 *
 * <pre>
 *   field1.field2.index.field3...
 * </pre>
 * 
 * where fields are identifiers, and indexes are integers denoting
 * array indexes.
 *
 * Paths can be used to represent fields as well as patterns. A
 * pattern path includes '*' for any matching field or index. For instance:
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
 * Implementation is optimized to be fast to toString and hashCode,
 * and does not occupy too much memory when a lot of paths are created
 * from a common prefix.
 *
 * Path objects are immutable. To modify paths, use MutablePath objects.
 */
public class Path implements Comparable<Path>, Serializable  {

    public static final String ANY="*";

    class PathRep implements Serializable {
        ArrayList<String> segments;
        transient String stringValue=null;
        transient int hashValue=0;

        public PathRep() {
            segments=new ArrayList<String>();
        }

        public PathRep(PathRep data) {
            segments=new ArrayList<String>(data.segments);
            stringValue=data.stringValue;
            hashValue=data.hashValue;
        }

        public void resetState() {
            stringValue=null;
            hashValue=0;
        }
        
        public int hashCode() {
            if(hashValue==0) {
                hashValue=segments.hashCode();
            }
            return hashValue;
        }

        public boolean equals(PathRep r) {
            return r!=null&&r.segments.equals(segments);
        }

        public int compareTo(PathRep x) {
            int tn=segments.size();
            int xn=x.segments.size();
            int n=tn>xn?xn:tn;
            int index=0;
            while(index<n) {
                int cmp=segments.get(index).compareTo(x.segments.get(index));
                if(cmp!=0)
                    return cmp;
                index++;
            }
            return tn-xn;
        }
        
        public String toString() {
            if(stringValue==null) {
                StringBuilder buf=new StringBuilder(segments.size()*8);
                boolean first=true;
                for(String x:segments) {
                if(first)
                    first=false;
                else
                    buf.append('.');
                buf.append(x);
                }
                stringValue=buf.toString();
            }
            return stringValue;
        }
    }

    protected PathRep data=new PathRep();

    public Path() {}

    public Path(Path x) {
        data.segments.addAll(x.data.segments);
    }

    public Path(String x) {
        parse(x,data.segments);
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
     * @return
     */
    public MutablePath mutableCopy() {
        return new MutablePath(this);
    }
    
    /**
     * Get the last path segment.
     * @return
     */
    public String getLast() {
        return tail(0);
    }

    /**
     * Get the number of path segments.
     * @return
     */
    public int numSegments() {
        return data.segments.size();
    }

    /**
     * Get path segment at given index starting from the head of the segment list.
     * 
     * @param i the index
     * @return the path segment
     */
    public String head(int i) {
        return data.segments.get(i);
    }

    /**
     * Get path segment at given index starting from the tail of the segment list.
     * 
     * @param i
     * @return the segment data
     */
    public String tail(int i) {
        return data.segments.get(data.segments.size()-1-i);
    }

    /**
     * Get the array index represented by the given path segment index (relative to head).
     * 
     * @param i
     * @return
     */
    public int getIndex(int i) {
        return Integer.valueOf(data.segments.get(i));
    }

    /**
     * Check if path segment at given index (relative to head) is an array index.
     * 
     * @param i
     * @return
     */
    public boolean isIndex(int i) {
        return Util.isNumber(data.segments.get(i));
    }

    public int hashCode() {
        return data.hashCode();
    }

    /**
     * Returns a new path that is a prefix of this path obtained by
     * removing x elements from the end. If the path is a mutable
     * path, the returned path is a mutable path. If the path is an
     * immutable path, the returned path is an immutable path.
     * 
     * @param x number of elements to remove from end of path
     * @return the new path
     * @throws IndexOutOfBoundException number of elements to remove is greater than number of segments available
     */
    public Path prefix(int x) {
        Path p = null;
        if(this instanceof MutablePath) {
            p=new MutablePath((MutablePath)this);
        } else {
            p=new Path(this);
        }
        for(int i=0;i<x;i++)
            p.data.segments.remove(p.data.segments.size()-1);
        p.data.resetState();
        return p;
    }

    /**
     * Check if this path matches the path pattern argument passed in.
     * 
     * @param pattern the pattern path
     * @return true if it matches, else false
     */
    public boolean matches(Path pattern) {
        int n=data.segments.size();
        if(n==pattern.data.segments.size()) {
            for(int i=0;i<n;i++) {
                String pat=pattern.data.segments.get(i);
                String val=data.segments.get(i);
                if(!(val.equals(pat)||pat.equals(ANY)))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean equals(Object x) {
        if(x!=null&&x instanceof Path)
            return ((Path)x).data.equals(data);
        else
            return false;
    }

    public int compareTo(Path x) {
        return x==null?-1:data.compareTo(x.data);
    }

    public String toString() {
        return data.toString();
    }
    
    /**
     * Parses the input path string (x) and appends each segment to the segments argument.
     * 
     * @param x the new paths segments to parse
     * @param segments the segment list to append new segments onto
     */
    protected static void parse(String x,List<String> segments) {
        StringBuilder buf=new StringBuilder(32);
        int state=0;
        int n=x.length();
        for(int i=0;i<n;i++) {
            char c=x.charAt(i);
            switch(state) {
            case 0: // Beginning of path, or after .
                if(!Character.isWhitespace(c))
                    if(c=='.')
                        throw new InvalidPathException("Unexpected '.' at " + i, x);
                    else {
                        buf.append(c);
                        state=1;
                    }
                break;

            case 1: // Parsing word
                if(Character.isWhitespace(c)) {
                    segments.add(buf.toString());
                    buf=new StringBuilder(32);
                    state=2;
                } else if(c=='.') {
                    segments.add(buf.toString());
                    buf=new StringBuilder(32);
                    state=0;
                } else
                    buf.append(c);
                break;

            case 2: // Parsing end of word
                if(!Character.isWhitespace(c))
                    if(c=='.') {
                        state=0;
                    } else
                        throw new InvalidPathException("Expected whitespace or '.' at " + i, x);
                else
                    throw new InvalidPathException("Unexpected character at " + i, x);
                break;
            }
        }
        if(state==1)
            segments.add(buf.toString());
    }
}
   
