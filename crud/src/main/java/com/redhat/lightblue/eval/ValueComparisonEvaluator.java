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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

public class ValueComparisonEvaluator extends QueryEvaluator {

    private static final Logger logger=LoggerFactory.getLogger(ValueComparisonEvaluator.class);

    private final FieldTreeNode fieldMd;
    private final Path field;
    private final BinaryComparisonOperator operator;
    private final Object value;

    /**
     * Constructs evaluator for {field op value} style comparison
     *
     * @param expr The expression
     * @param md Entity metadata
     */
    public ValueComparisonEvaluator(ValueComparisonExpression expr,
                                    FieldTreeNode context) {
        this.field=expr.getField();
        fieldMd=context.resolve(field);
        if(fieldMd==null)
            throw new EvaluationError(expr,"No field "+field);
        operator=expr.getOp();
        value=expr.getRvalue().getValue();
        logger.debug("ctor {} {} {}",field,operator,value);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        logger.debug("evaluate {} {} {}",field,operator,value);
        JsonNode valueNode=ctx.getNode(field);
        Object docValue;
        if(valueNode!=null)
            docValue=fieldMd.getType().fromJson(valueNode);
        else
            docValue=null;
        logger.debug(" value={}",valueNode);
        int result=fieldMd.getType().compare(docValue,value);
        logger.debug(" result={}",result);
        ctx.setResult(operator.apply(result));
        return ctx.getResult();
    }
}
