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

import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

public class FieldComparisonEvaluator extends QueryEvaluator {

    private static final Logger logger=LoggerFactory.getLogger(FieldComparisonEvaluator.class);

    private final FieldTreeNode fieldMd;
    private final FieldTreeNode rfieldMd;
    private final Path relativePath;
    private final Path rfieldRelativePath;
    private final BinaryComparisonOperator operator;

    /**
     * Constructs evaluator for {field op field} style comparison
     *
     * @param expr The expression
     * @param md Entity metadata
     * @param context The path relative to which the expression will be evaluated
     */
    public FieldComparisonEvaluator(FieldComparisonExpression expr,
                                    FieldTreeNode context) {
        this.relativePath=expr.getField();
        this.rfieldRelativePath=expr.getRfield();
        fieldMd=context.resolve(relativePath);
        if(fieldMd==null)
            throw new EvaluationError(expr,"No field " +relativePath);
        rfieldMd=context.resolve(rfieldRelativePath);
        if(rfieldMd==null)
            throw new EvaluationError(expr,"No field "+rfieldRelativePath);
        operator=expr.getOp();
        logger.debug("ctor {} {} {}",relativePath,operator,rfieldRelativePath);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        logger.debug("evaluate {} {} {}",relativePath,operator,rfieldRelativePath);
        JsonNode lvalueNode=JsonDoc.get(ctx.getCurrentContextNode(),relativePath);
        Object ldocValue;
        if(lvalueNode!=null)
            ldocValue=fieldMd.getType().fromJson(lvalueNode);
        else
            ldocValue=null;
        JsonNode rvalueNode=JsonDoc.get(ctx.getCurrentContextNode(),rfieldRelativePath);
        Object rdocValue;
        if(rvalueNode!=null)
            rdocValue=rfieldMd.getType().fromJson(rvalueNode);
        else
            rdocValue=null;
        logger.debug(" lvalue={} rvalue={}",lvalueNode,rvalueNode);
        int result=fieldMd.getType().compare(ldocValue,rdocValue);
        logger.debug(" result={}",result);
        ctx.setResult(operator.test(result));
        return ctx.getResult();
    }
}
