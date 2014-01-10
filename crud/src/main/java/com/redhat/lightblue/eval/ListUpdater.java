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
package com.redhat.lightblue.eval;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.crud.PartialUpdateExpression;

/**
 * Encapsulates multiple updaters
 */
public class ListUpdater extends Updater {

    private final List<Updater> updaters;

    /**
     * Ctor
     *
     * @param factory Node factory
     * @param md Entity metadata
     * @param exprs List of update expressions
     */
    public ListUpdater(JsonNodeFactory factory, EntityMetadata md, List<PartialUpdateExpression> exprs) {
        updaters = new ArrayList<>(exprs.size());
        for (PartialUpdateExpression x : exprs) {
            updaters.add(Updater.getInstance(factory, md, x));
        }
    }

    /**
     * Updates the document
     */
    @Override
    public boolean update(JsonDoc doc) {
        boolean ret = false;
        for (Updater x : updaters) {
            if (x.update(doc)) {
                ret = true;
            }
        }
        return ret;
    }
}
