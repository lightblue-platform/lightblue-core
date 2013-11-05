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

package com.redhat.lightblue.metadata.parser;

/**
 * Common interface for parsers for metadata extensions
 *
 * The <code>NodeType</code> refers to the type of the tree node in
 * the document object tree. The parameter <code>T</code> is the
 * object returned once a node of type <code>NodeType</code> is
 * parsed.
 */
public interface Parser<NodeType,T> {

    /**
     * Return an object representation of the node
     */
    public T parse(NodeType node);

    /**
     * Intialize the empt node <code>emptyNode</code> with the
     * contents of <code>object</code>
     */
    public void convert(NodeType emptyNode,T object);
}

    
