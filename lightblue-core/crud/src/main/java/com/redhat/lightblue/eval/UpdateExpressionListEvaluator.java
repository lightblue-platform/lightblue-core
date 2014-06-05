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
import java.util.Set;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.UpdateExpressionList;
import com.redhat.lightblue.query.PartialUpdateExpression;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Evaluates a list of update expressions
 */
public class UpdateExpressionListEvaluator extends Updater {

    private final List<Updater> updaters;

    public UpdateExpressionListEvaluator(JsonNodeFactory factory, FieldTreeNode context, UpdateExpressionList expr) {
        List<PartialUpdateExpression> list = expr.getList();
        updaters = new ArrayList<>(list.size());
        for (PartialUpdateExpression x : list) {
            updaters.add(Updater.getInstance(factory, context, x));
        }
    }

    @Override
    public void getUpdateFields(Set<Path> fields) {
        for (Updater x : updaters) {
            x.getUpdateFields(fields);
        }
    }

    @Override
    public boolean update(JsonDoc doc, FieldTreeNode contextMd, Path contextPath) {
        boolean ret = false;
        for (Updater x : updaters) {
            if (x.update(doc, contextMd, contextPath)) {
                ret = true;
            }
        }
        return ret;
    }
}
