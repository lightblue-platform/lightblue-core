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

/**
 * Represents a field comparison query of the form
 * <pre>
 * field_comparison_expression := { field: <field>,  
 *                                 op: binary_comparison_operator,  
 *                                 rfield: <field> }  
 * </pre>
 */
public class FieldComparisonExpression 
    extends BinaryRelationalExpression {

    private Path field;
    private BinaryComparisonOperator op;
    private Path rfield;

    /**
     * Default ctor
     */
    public FieldComparisonExpression() {}

    /**
     * Ctor with the given values
     */
    public FieldComparisonExpression(Path field,
                                     BinaryComparisonOperator op,
                                     Path rfield) {
        this.field=field;
        this.op=op;
        this.rfield=rfield;
    }

    /**
     * The field on the left side of the operator
     */
    public Path getField() {
        return this.field;
    }

    /**
     * The field on the left side of the operator
     */
    public void setField(Path argField) {
        this.field = argField;
    }

    /**
     * The comparison operator
     */
    public BinaryComparisonOperator getOp() {
        return this.op;
    }

    /**
     * The comparison operator
     */
    public void setOp(BinaryComparisonOperator argOp) {
        this.op = argOp;
    }

    /**
     * The field on the right side of the operator
     */
    public Path getRfield() {
        return this.rfield;
    }

    /**
     * The field on the right side of the operator
     */
    public void setRfield(Path argRfield) {
        this.rfield = argRfield;
    }

    /**
     * Returns json representation of the query
     */
    public JsonNode toJson() {
        return factory.objectNode().put("field",field.toString()).
            put("op",op.toString()).
            put("rfield",rfield.toString());
    }
}
