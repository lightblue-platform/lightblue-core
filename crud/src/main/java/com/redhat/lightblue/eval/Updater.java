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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.crud.UpdateExpression;
import com.redhat.lightblue.crud.SetExpression;
import com.redhat.lightblue.crud.UnsetExpression;
import com.redhat.lightblue.crud.UpdateExpressionList;
import com.redhat.lightblue.crud.ArrayPopExpression;
import com.redhat.lightblue.crud.ArrayPushExpression;
import com.redhat.lightblue.crud.ArrayRemoveByQueryExpression;
import com.redhat.lightblue.crud.ArrayRemoveValuesExpression;

/**
 * Base class for update expression evaluators.
 */
public abstract class Updater {

    /**
     * The implementation should update the document
     *
     * @return true if document is updated, false if not
     */
    public abstract boolean update(JsonDoc doc);

    /**
     * Creates an updater object based on the given update expression
     */
    public static Updater getInstance(JsonNodeFactory factory, EntityMetadata md, UpdateExpression expr) {
        Updater ret = null;
        if (expr instanceof SetExpression) {
            switch (((SetExpression) expr).getOp()) {
                case _set:
                    return new FieldSetter(factory, md, ((SetExpression) expr).getValues());
                case _add:
                    return new FieldAdder(factory, md, ((SetExpression) expr).getValues());
            }
        } else if (expr instanceof UnsetExpression) {
            return new FieldUnsetter(((UnsetExpression) expr).getFields());
        } else if (expr instanceof UpdateExpressionList) {
            return new ListUpdater(factory, md, ((UpdateExpressionList) expr).getList());
        } else if (expr instanceof ArrayPopExpression) {
            return new ArrayPopper((ArrayPopExpression) expr);
        } else if (expr instanceof ArrayPushExpression) {
            return new ArrayPusher(factory, md, (ArrayPushExpression) expr);
        } else if (expr instanceof ArrayRemoveByQueryExpression) {
            return new ArrayRemoveByQueryEvaluator(md, (ArrayRemoveByQueryExpression) expr);
        } else if (expr instanceof ArrayRemoveValuesExpression) {
            return new ArrayRemoveValues(md, (ArrayRemoveValuesExpression) expr);
        }
        return ret;
    }
}
