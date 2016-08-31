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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryValueRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.assoc.qrew.Rewriter;

import com.redhat.lightblue.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class that combines value comparison expressions to in/not-in
 * expressions.
 */
abstract class CombineComparisonsToInNotIn extends Rewriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CombineComparisonsToInNotIn.class);

    private final NaryLogicalOperator logicalOp;
    private final BinaryComparisonOperator binaryOp;
    private final NaryRelationalOperator relationalOp;

    protected CombineComparisonsToInNotIn(NaryLogicalOperator logicalOp,
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
                LOGGER.debug("Processing q={}", le);
                // group value comparison expressions with given logical operator ($and/$or)
                boolean needCombine = false;
                Map<Path, List<ValueComparisonExpression>> map = new HashMap<>();
                for (QueryExpression x : le.getQueries()) {
                    ValueComparisonExpression vce = dyncast(ValueComparisonExpression.class, x);
                    if (vce != null && vce.getOp() == binaryOp) {
                        List<ValueComparisonExpression> values = map.get(vce.getField());
                        if (values == null) {
                            map.put(vce.getField(), values = new ArrayList<>());
                        } else if (!needCombine) {
                            needCombine = true; // There exists more than one matching value comparison for this path, so need to combine to one
                        }
                        values.add(vce);
                    }
                }
                LOGGER.debug("Grouped expressions={}", map);
                if (needCombine) {
                    LOGGER.debug("Query expressions can be combined");
                    List<QueryExpression> newList = new ArrayList<>(le.getQueries().size());
                    for (Map.Entry<Path, List<ValueComparisonExpression>> entry : map.entrySet()) {
                        if (entry.getValue().size() > 1) {
                            // Combine them into an Nary ($in/$nin) expression
                            Set<Value> valueList = new HashSet<>();
                            for (ValueComparisonExpression x : entry.getValue()) {
                                valueList.add(x.getRvalue());
                            }
                            newList.add(new NaryValueRelationalExpression(entry.getKey(), relationalOp,
                                    new ArrayList<>(valueList)));
                        } else {
                            newList.addAll(entry.getValue());
                        }
                    }
                    // Add all the expressions that are not value comparison expressions
                    for (QueryExpression x : le.getQueries()) {
                        if (x instanceof ValueComparisonExpression) {
                            if (((ValueComparisonExpression) x).getOp() != binaryOp) {
                                newList.add(x);
                            }
                        } else {
                            newList.add(x);
                        }
                    }
                    LOGGER.debug("Combined expression list={}", newList);
                    return new NaryLogicalExpression(logicalOp, newList);
                }
            }
        }
        return q;
    }
}
