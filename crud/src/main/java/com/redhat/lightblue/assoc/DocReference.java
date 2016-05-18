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

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.util.Path;

/**
 * Represents a reference to another document for another entity.
 */
public class DocReference implements Serializable {

    private static final long serialVersionUID = 1l;

    private final ResultDoc document;

    /**
     * Constructs a reference for the given document and field
     */
    public DocReference(ResultDoc document) {
        this.document = document;
    }

    /**
     * Returns the source document
     */
    public ResultDoc getDocument() {
        return document;
    }

    public String toString() {
        return document.getId().toString();
    }
}
