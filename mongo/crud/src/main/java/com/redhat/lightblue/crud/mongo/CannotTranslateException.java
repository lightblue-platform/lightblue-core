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
package com.redhat.lightblue.crud.mongo;

import com.redhat.lightblue.query.UpdateExpression;

import com.redhat.lightblue.util.Path;

/**
 * Thrown whenever an update expression cannot be translated to MongoDB update expression
 */
public class CannotTranslateException extends Exception {

    private final UpdateExpression expr;

    public CannotTranslateException(UpdateExpression expr) {
        super(expr.toString());
        this.expr = expr;
    }

    public CannotTranslateException(Path field) {
        super(field.toString());
        expr = null;
    }

    public UpdateExpression getExpr() {
        return expr;
    }
}
