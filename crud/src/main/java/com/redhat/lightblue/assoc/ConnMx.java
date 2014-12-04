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
package com.redhat.lightblue.assoc;

import java.io.Serializable;

/**
 * A simple connection matrix implementation
 */
public class ConnMx implements Serializable {
    
    private static final long serialVersionUID=1l;

    private static final int[] I0=new int[0];
    
    /**
     * Connection matrix. connMx[i][j] is true if there is an edge from i to j
     */
    private final boolean[][] connMx;

    public ConnMx(int nNodes) {
        connMx=new boolean[nNodes][];
        for(int i=0;i<nNodes;i++)
            connMx[i]=new boolean[nNodes];
    }

    /**
     * Copy constructor.
     */
    public ConnMx(ConnMx source) {
        connMx=new boolean[source.connMx.length][];
        for(int i=0;i<source.connMx.length;i++)
            connMx[i]=source.connMx[i].clone();
    }

    /**
     * Returns the sources of the given node
     */
    public int[] getSources(int node) {
        int[] sources=new int[connMx.length];
        int nSources=0;
        for(int i=0;i<connMx.length;i++) {
            if(connMx[i][node]) {
                sources[nSources++]=i;
            }
        }
        return trunc(sources,nSources);
    }

    /**
     * Returns the destinations of the given node
     */
    public int[] getDestinations(int node) {
        int[] dests=new int[connMx.length];
        int nDests=0;
        for(int i=0;i<connMx.length;i++)
            if(connMx[node][i]) {
                dests[nDests++]=i;
            }
        return trunc(dests,nDests);
    }
    
    /**
     * Returns an array of source nodes, nodes with no incoming
     * edges. 
     */
    public int[] getSources() {
        // Source nodes are those with no incoming edges.
        int[] sources=new int[connMx.length];
        int n=0;
        for(int node=0;node<connMx.length;node++) {
            boolean incomingExists=false;
            for(int from=0;from<connMx.length;from++) {
                if(connMx[from][node]) {
                    incomingExists=true;
                    break;
                }
            }
            if(!incomingExists)
                sources[n++]=node;
        }
        return trunc(sources,n);
    }

    /**
     * Flips the direction of a node.
     */
    public void flip(int x,int y) {
        if(connMx[x][y]) {
            connMx[x][y]=false;
            connMx[y][x]=true;
        } else if(connMx[y][x]) {
            connMx[y][x]=false;
            connMx[x][y]=true;
        }
    }
    
    /**
     * Returns if there exists a directed edge between the nodes,
     * directed from <code>from</code> to <code>to</code>
     */
    public boolean isDirectedConnected(int from,
                                       int to) {
        return connMx[from][to];
    }

    /**
     * Returns if there exists an edge between the two nodes, pointing either way
     */
    public boolean isUndirectedConnected(int from,
                                         int to) {
        return connMx[from][to]||connMx[to][from];
    }


    @Override
    public String toString() {
        StringBuilder bld=new StringBuilder(128);
        for(int i=0;i<connMx.length;i++) {
            for(int j=0;j<connMx.length;j++) {
                bld.append(connMx[i][j]?'1':'0').append(' ');
            }
            bld.append('\n');
        }
        return bld.toString();
    }


    /**
     * Adds an edge from node <code>from</code> to node <code>to</code>
     */
    public void connect(int from,int to) {
        connMx[from][to]=true;
    }

    private int[] trunc(int[] arr,int n) {
        if(n==arr.length)
            return arr;
        else if(n==0)
            return I0;
        else {
            int[] ret=new int[n];
            System.arraycopy(arr,0,ret,0,n);
            return ret;
        }
    }
    
}
