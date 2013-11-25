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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDoc implements Serializable {

    private static final long serialVersionUID=1l;

    private final JsonNode docRoot;

    private class PathCursor implements KeyValueCursor<Path,JsonNode> {
        MutablePath path;
        Path currentPath;
        JsonNode currentNode;
        JsonNode node;
        int anys[];
        int values[];
        boolean nextFound=false;
        boolean ended=false;

        public PathCursor(Path p) {
            path=new MutablePath(p);
            int n=path.numSegments();
            int nAnys=0;
            for(int i=0;i<n;i++)
                if(Path.ANY.equals(path.head(i)))
                    nAnys++;
            anys=new int[nAnys];
            values=new int[nAnys];
            int j=0;
            if(nAnys>0)
                for(int i=0;i<n;i++)
                    if(Path.ANY.equals(path.head(i))) 
                        anys[j++]=i;
            // First initialization
            // Point to the element before the first
            if(anys.length>0) {
                for(int i=0;i<values.length;i++)
                    values[i]=-1;
           }
        }

        public Path getCurrentKey() {
            return currentPath;
        }

        public JsonNode getCurrentValue() {
            return currentNode;
        }

        public boolean hasNext() {
            if(!nextFound)
                if(!ended)
                    seekNext();
            return nextFound;
        }

        public void next() {
            if(!nextFound)
                if(!ended)
                    seekNext();
            if(nextFound) {
                currentPath=path.immutableCopy();
                currentNode=node;
            } else {
                currentPath=null;
                node=null;
            }
            nextFound=false;
        }

        private void seekNext() {
            nextFound=false;
            
            if(anys.length==0) {
                // Non-array ref
                nextFound=(node=get(path))!=null;
                ended=true;
            } else {
                // Array ref
                // values[i]=-1 means we need to initialize that level first
                // dir: determines the direction digptr will advance.
                int dir=1;
                int digptr=0;
                do {
                    if(dir==1) {
                        boolean newInit=false;
                        if(values[digptr]==-1) {
                            // See if this is accessible
                            values[digptr]=0;
                            path.set(anys[digptr],values[digptr]);
                            Path prefix=path.prefix(anys[digptr]+1);
                            if(get(prefix)==null) {
                                // Level is not accesible, go back
                                values[digptr]=-1;
                                dir=-1;
                            } else
                                newInit=true;
                        } 

                        digptr+=dir;
                        if(digptr<0) {
                            ended=true;
                        } else if(digptr>=anys.length) {
                            digptr=anys.length-1;
                            dir=-1;
                            if(newInit&&(node=get(path))!=null) {
                                nextFound=true;
                            }
                        }
                    } else {
                        values[digptr]++;
                        path.set(anys[digptr],values[digptr]);
                        Path prefix=path.prefix(anys[digptr]+1);
                        if(get(prefix)!=null) {
                            // Level is accessible, go deeper
                            dir=1;
                        } else {
                            // Reset level and go back
                            values[digptr]=-1;
                            dir=-1;
                        }
                        digptr+=dir;
                        if(digptr<0) {
                            ended=true;
                        } else if(digptr>=anys.length) {
                            digptr=anys.length-1;
                            dir=-1;
                            if((node=get(path))!=null)
                                nextFound=true;
                        }
                    }
                } while(!nextFound&&!ended);
            }
        }

        public String toString() {
            StringBuilder str=new StringBuilder(128);
            str.append("path=").append(path).append('\n').
                append("currentPath=").append(currentPath).append('\n');
            if(anys!=null) {
                str.append("anys=");
                for(int i=0;i<anys.length;i++) {
                    if(i>0)
                        str.append(',');
                    str.append(anys[i]);
                }
                str.append('\n');
            }
            str.append("nextFound=").append(nextFound).append(" ended=").append(ended);
            return str.toString();
        }
    }

    public JsonDoc(JsonNode doc) {
        this.docRoot=doc;
    }

    public JsonNodeCursor cursor() {
        return cursor(Path.EMPTY);
    }

    public JsonNodeCursor cursor(Path p) {
        return new JsonNodeCursor(p,docRoot);
    }
    
    /**
     * Returns all nodes matching the path. The path can contain *
     * 
     * @param p The path
     *
     * Returns a cursor iterating through all nodes of arrays, if any
     */
    public KeyValueCursor<Path,JsonNode> getAllNodes(Path p) {
        return new PathCursor(p);
    }

    /**
     * Returns a node matching a path
     *
     * @param p The path
     *
     * The path cannot contain *.
     *
     * @returns The node, or null if the node cannot be found
     */
    public JsonNode get(Path p) {
        JsonNode current = docRoot;
        int n = p.numSegments();
        for (int level = 0; level < n; level++) {
            String name = p.head(level);
            if (name.equals(Path.ANY))
                throw new IllegalArgumentException(p.toString());
            else if(current instanceof ArrayNode) {
                int index=Integer.valueOf(name);
                current=((ArrayNode)current).get(index);
            } else if(current instanceof ObjectNode) {
                current=current.get(name);
            } else
                current=null;
            if(current==null)
                break;
        }
        return current;
    }
}
