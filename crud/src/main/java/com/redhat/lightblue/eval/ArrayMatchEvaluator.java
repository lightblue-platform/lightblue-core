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

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

public class ArrayMatchEvaluator extends QueryEvaluator {
    private final Path field;
    private final QueryEvaluator ev;
    private final ArrayElement elem;

    public ArrayMatchEvaluator(ArrayMatchExpression expr,
                               FieldTreeNode context) {
        // field needs to be resolved relative to the current context
        field = expr.getArray();
        FieldTreeNode node = context.resolve(field);
        if (node == null) {
            throw new EvaluationError(expr);
        }
        if (node instanceof ArrayField) {
            elem = ((ArrayField) node).getElement();
            ev = QueryEvaluator.getInstance(expr.getElemMatch(), elem);
        } else {
            throw new EvaluationError(expr, CrudConstants.ERR_EXPECTED_ARRAY + field);
        }
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        boolean ret = false;
        KeyValueCursor<Path, JsonNode> cursor = ctx.getNodes(field);
        while(cursor.hasNext()&&!ret) {
            cursor.next();
            JsonNode node=cursor.getCurrentValue();
            if (node instanceof ArrayNode) {
                ArrayNode array = (ArrayNode) node;
                int index = 0;
                QueryEvaluationContext nestedCtx = null;
                for (Iterator<JsonNode> itr = array.elements(); itr.hasNext();) {
                    JsonNode arrayElem = itr.next();
                    if (index == 0) {
                        nestedCtx = ctx.firstElementNestedContext(arrayElem, field);
                    } else {
                        nestedCtx.elementNestedContext(arrayElem, index);
                    }
                    if (ev.evaluate(nestedCtx)) {
                        ret = true;
                    }
                    index++;
                }
            }
        }
        ctx.setResult(ret);
        return ret;
    }
}
