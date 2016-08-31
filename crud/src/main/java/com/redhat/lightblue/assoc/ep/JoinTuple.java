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
package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;

/**
 * A join tuple is a tuple representing a slot in a parent document, if there is
 * one, and a list of child documents. These slot and child documents will be
 * used to retrieve a new batch of documents, and they will be attached to the
 * parent document slot if there is one.
 */
public class JoinTuple {
    private final ResultDocument parentDocument;
    private final ChildSlot parentDocumentSlot;
    private final List<ResultDocument> childTuple;

    public JoinTuple(ResultDocument parentDocument, ChildSlot slot, List<ResultDocument> children) {
        this.parentDocument = parentDocument;
        this.parentDocumentSlot = slot;
        this.childTuple = children;
    }

    public ResultDocument getParentDocument() {
        return parentDocument;
    }

    public ChildSlot getParentDocumentSlot() {
        return parentDocumentSlot;
    }

    public List<ResultDocument> getChildTuple() {
        return childTuple;
    }

    public List<ExecutionBlock> getBlocks() {
        List<ExecutionBlock> blocks = new ArrayList<>();
        if (parentDocument != null) {
            blocks.add(parentDocument.getBlock());
        }
        if (childTuple != null) {
            for (ResultDocument d : childTuple) {
                blocks.add(d.getBlock());
            }
        }
        return blocks;
    }

    @Override
    public String toString() {
        return "P:" + parentDocumentSlot + "/" + parentDocument + " C:" + childTuple;
    }

}
