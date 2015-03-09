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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.redhat.lightblue.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.AllMatchExpression;
import com.redhat.lightblue.query.ForEachExpression;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.RemoveElementExpression;
import com.redhat.lightblue.query.UpdateExpression;

/**
 * Evaluates a loop over the elements of an array
 */
public class ForEachExpressionEvaluator extends Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForEachExpressionEvaluator.class);

    private final JsonNodeFactory factory;
    private final Memento memento;
    private UpdateInfo updateInfo = null;
    private int numAny = 0;


    public ForEachExpressionEvaluator(JsonNodeFactory factory, FieldTreeNode context, ForEachExpression expr) {
        this.memento = new Memento(factory,context,expr);
        this.factory = factory;
        this.numAny = expr.getField().nAnys();
        // Resolve the field, make sure it is an array
        this.updateInfo = generateProcessedInfo(context, expr, null, null);


    }

    private UpdateInfo generateProcessedInfo(FieldTreeNode context, ForEachExpression expr, Path informedField, Updater updaterInstance) {
        Path field;
        ArrayField fieldMd;
        QueryEvaluator queryEvaluator;
        Updater updater;

        if(informedField == null) {
            field = expr.getField();
        } else {
            field = informedField;
        }

        FieldTreeNode md = context.resolve(field);
        if (md instanceof ArrayField) {
            fieldMd = (ArrayField) md;
        } else {
            throw new EvaluationError(CrudConstants.ERR_FIELD_NOT_ARRAY + field);
        }

        // Get a query evaluator
        QueryExpression query = expr.getQuery();
        if (query instanceof AllMatchExpression) {
            queryEvaluator = new AllEvaluator();
        } else {
            queryEvaluator = QueryEvaluator.getInstance(query, fieldMd.getElement());
        }

        if(updaterInstance == null) {
            // Get an updater to execute on each matching element
            UpdateExpression upd = expr.getUpdate();
            if (upd instanceof RemoveElementExpression) {
                updater = new RemoveEvaluator(fieldMd.getElement().getFullPath());
            } else {
                updater = Updater.getInstance(factory, fieldMd.getElement(), upd);
            }
        } else {
            updater = updaterInstance;
        }

        UpdateInfo updateInfoInstance = new UpdateInfo(field, fieldMd, queryEvaluator, updater);

        return updateInfoInstance;
    }

    @Override
    public void getUpdateFields(Set<Path> fields) {
        this.updateInfo.updater.getUpdateFields(fields);
    }

    @Override
    public boolean update(JsonDoc doc, FieldTreeNode contextMd, Path contextPath) {
        boolean ret = false;

        if(numAny > 0) {
            KeyValueCursor<Path, JsonNode> cursor = doc.getAllNodes(memento.expr.getField());

            boolean b = cursor.hasNext();
            while(b){
                cursor.next();
                Path currentKey = cursor.getCurrentKey();
                JsonNode currentValue = cursor.getCurrentValue();

                UpdateInfo updateInfoInstance = generateProcessedInfo(memento.context, memento.expr, currentKey, this.updateInfo.updater);

                ret =  update(doc, contextPath, updateInfoInstance);
                b = cursor.hasNext();
            }

            return ret;
        } else {
            ret = update(doc, contextPath, this.updateInfo);
            return ret;
        }
    }

    private boolean update(JsonDoc doc, Path contextPath, UpdateInfo updateInfo) {
        boolean ret = false;
        // Get a reference to the array field, and iterate all elements in the array
        ArrayNode arrayNode = (ArrayNode) doc.get(new Path(contextPath, updateInfo.field));
        LOGGER.debug("Array node {}={}", updateInfo.field, arrayNode);
        ArrayElement elementMd = updateInfo.fieldMd.getElement();
        if (arrayNode != null) {
            int index = 0;
            MutablePath itrPath = new MutablePath(contextPath);
            itrPath.push(updateInfo.field);
            MutablePath arrSizePath = itrPath.copy();
            arrSizePath.setLast(arrSizePath.getLast() + "#");
            arrSizePath.rewriteIndexes(contextPath);

            itrPath.push(index);
            // Copy the nodes to a separate list, so we iterate on the
            // new copy, and modify the original
            ArrayList<JsonNode> nodes = new ArrayList<>();
            for (Iterator<JsonNode> itr = arrayNode.elements(); itr.hasNext(); ) {
                nodes.add(itr.next());
            }
            for (JsonNode elementNode : nodes) {
                itrPath.setLast(index);
                Path elementPath = itrPath.immutableCopy();
                LOGGER.debug("itr:{}", elementPath);
                QueryEvaluationContext ctx = new QueryEvaluationContext(elementNode, elementPath);
                if (updateInfo.queryEvaluator.evaluate(ctx)) {
                    LOGGER.debug("query matches {}", elementPath);
                    LOGGER.debug("Calling updater {}", updateInfo.updater);
                    if (updateInfo.updater.update(doc, elementMd, elementPath)) {
                        LOGGER.debug("Updater {} returns {}", updateInfo.updater, true);
                        ret = true;
                        // Removal shifts nodes down
                        if (updateInfo.updater instanceof RemoveEvaluator) {
                            index--;
                        }
                    } else {
                        LOGGER.debug("Updater {} return false", updateInfo.updater);
                    }
                } else {
                    LOGGER.debug("query does not match {}", elementPath);
                }
                index++;
            }
            if (ret) {
                doc.modify(arrSizePath, factory.numberNode(arrayNode.size()), false);
            }
        }
        return ret;
    }

    private class Memento {
        private final JsonNodeFactory factory;
        private final FieldTreeNode context;
        private final ForEachExpression expr;

        public Memento(JsonNodeFactory factory, FieldTreeNode context, ForEachExpression expr) {
            this.factory =factory;
            this.context =context;
            this.expr =expr;
        }
    }

    private class UpdateInfo {
        private Path field;
        private ArrayField fieldMd;
        private QueryEvaluator queryEvaluator;
        private Updater updater;

        public UpdateInfo(Path field, ArrayField fieldMd, QueryEvaluator queryEvaluator, Updater updater) {
            this.field = field;
            this.fieldMd = fieldMd;
            this.queryEvaluator = queryEvaluator;
            this.updater = updater;
        }
    }


    /**
     * Inner class for $all
     */
    private static class AllEvaluator extends QueryEvaluator {
        @Override
        public boolean evaluate(QueryEvaluationContext ctx) {
            ctx.setResult(true);
            return true;
        }
    }

    private static class RemoveEvaluator extends Updater {
        private final Path absField;

        public RemoveEvaluator(Path absField) {
            this.absField = absField;
        }

        @Override
        public void getUpdateFields(Set<Path> fields) {
            fields.add(absField);
        }

        @Override
        public boolean update(JsonDoc doc, FieldTreeNode contextMd, Path contextPath) {
            return doc.modify(contextPath, null, false) != null;
        }
    }
}
