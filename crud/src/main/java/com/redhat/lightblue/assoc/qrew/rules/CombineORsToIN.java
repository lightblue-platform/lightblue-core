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

import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.BinaryComparisonOperator;

import com.redhat.lightblue.assoc.qrew.Rewriter;

/**
 * If
 * <pre>
 *   q={$or:{...,{field:x,op:=,rvalue=v},..,{field:x,op:=,rvalue=w}...}}
 * </pre> this rewrites q as
 * <pre>
 *   q={$or:{...,{$in:{field:x,values:[v,w]}},...}}
 * </pre>
 */
public class CombineORsToIN extends CombineComparisonsToInNotIn {

    public static final Rewriter INSTANCE = new CombineORsToIN();

    public CombineORsToIN() {
        super(NaryLogicalOperator._or,
                BinaryComparisonOperator._eq,
                NaryRelationalOperator._in);
    }
}
