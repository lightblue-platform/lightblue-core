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
 * Represents a reference to a child document for another entity. A reference is
 * from a document:field pair to another document. There can be multiple
 * references from a particular document to the documents of the same entity,
 * for instance, if the reference is an array element.
 */
public class ChildDocReference extends DocReference {

    private final Path referenceField;
    private final List<ResultDoc> children = new ArrayList<>();

    /**
     * Constructs a reference for the given document and field
     */
    public ChildDocReference(ResultDoc document, Path field) {
        super(document);
        this.referenceField = field;
    }

    /**
     * Returns the field in the source document that this reference represents
     */
    public Path getReferenceField() {
        return referenceField;
    }

    /**
     * Returns the child documents for this reference
     */
    public List<ResultDoc> getChildren() {
        return children;
    }

    public String toString() {
        return "child ref: " + referenceField + "@" + super.toString();
    }
}
