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
package com.redhat.lightblue.assoc.qrew.rules;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryValueRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.assoc.qrew.Rewriter;

/**
 * Base class that combines in/not-in and value comparison expressions to
 * in/not-in expressions.
 */
abstract class ExtendRelationalInLogical extends Rewriter {

    private final NaryLogicalOperator logicalOp;
    private final BinaryComparisonOperator binaryOp;
    private final NaryRelationalOperator relationalOp;

    protected ExtendRelationalInLogical(NaryLogicalOperator logicalOp,
                                        BinaryComparisonOperator binaryOp,
                                        NaryRelationalOperator relationalOp) {
        this.logicalOp = logicalOp;
        this.binaryOp = binaryOp;
        this.relationalOp = relationalOp;
    }

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        NaryLogicalExpression le = dyncast(NaryLogicalExpression.class, q);
        if (le != null) {
            if (le.getOp() == logicalOp) {
                // Get all in expressions in a list, keep a modified flag
                boolean queryModified = false;
                List<NaryValueRelationalExpression> newExp = new ArrayList<>();
                List<QueryExpression> removedExp = new ArrayList<>();
                for (QueryExpression iq : le.getQueries()) {
                    NaryValueRelationalExpression naryExp = dyncast(NaryValueRelationalExpression.class, iq);
                    if (naryExp != null && naryExp.getOp() == relationalOp) {
                        boolean expModified = false;
                        // See if there are any value expressions with the same field and operator
                        for (QueryExpression vq : le.getQueries()) {
                            ValueComparisonExpression vce = dyncast(ValueComparisonExpression.class, vq);
                            if (vce != null) {
                                if (vce.getField().equals(naryExp.getField()) && vce.getOp() == binaryOp) {
                                    // Add this value to the new expression list
                                    if (!removedExp.contains(vce)) {
                                        if (!expModified) {
                                            expModified = true;
                                            naryExp = new NaryValueRelationalExpression(naryExp.getField(),
                                                    naryExp.getOp(),
                                                    new ArrayList<>(naryExp.getValues()));
                                        }
                                        boolean found = false;
                                        for (Value x : naryExp.getValues()) {
                                            if (x.equals(vce.getRvalue())) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            naryExp.getValues().add(vce.getRvalue());
                                        }
                                        newExp.add(naryExp);
                                        removedExp.add(vce);
                                        removedExp.add(naryExp);
                                    }
                                }
                            }
                        }
                        if (expModified) {
                            queryModified = true;
                        }
                    }
                }
                if (queryModified) {
                    List<QueryExpression> newList = new ArrayList<>();
                    for (QueryExpression ip : le.getQueries()) {
                        if (!removedExp.contains(ip)) {
                            newList.add(ip);
                        }
                    }
                    newList.addAll(newExp);
                    return new NaryLogicalExpression(logicalOp, newList);
                }
            }
        }
        return q;
    }
}
