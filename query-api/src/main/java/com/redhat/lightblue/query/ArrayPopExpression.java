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
package com.redhat.lightblue.query;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Path;

public class ArrayPopExpression extends  ArrayUpdateExpression {
    private boolean first;

    public ArrayPopExpression() {}

    public ArrayPopExpression(Path field,boolean first) {
        super(field);
        this.first=first;
    }

    @Override
    public UpdateOperator getOp() {
        return UpdateOperator._pop;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean b) {
        first=b;
    }

    @Override
    protected JsonNode jsonValue() {
        return getFactory().textNode(first?"first":"last");
    }

    public static ArrayPopExpression fromJson(Path field,JsonNode node) {
        return new ArrayPopExpression(field,
                                      "first".equals(node.asText()));
    }
}
