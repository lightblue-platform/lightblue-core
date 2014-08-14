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
package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.hystrix.rdbms.ExecuteUpdateCommand;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.metadata.rdbms.converter.Translator;
import com.redhat.lightblue.metadata.rdbms.enums.ExpressionOperators;
import com.redhat.lightblue.metadata.rdbms.enums.IfOperators;
import com.redhat.lightblue.metadata.rdbms.model.Bindings;
import com.redhat.lightblue.metadata.rdbms.model.Conditional;
import com.redhat.lightblue.metadata.rdbms.model.ElseIf;
import com.redhat.lightblue.metadata.rdbms.model.Expression;
import com.redhat.lightblue.metadata.rdbms.model.For;
import com.redhat.lightblue.metadata.rdbms.model.ForEach;
import com.redhat.lightblue.metadata.rdbms.model.If;
import com.redhat.lightblue.metadata.rdbms.model.IfFieldCheckField;
import com.redhat.lightblue.metadata.rdbms.model.IfFieldCheckValue;
import com.redhat.lightblue.metadata.rdbms.model.IfFieldCheckValues;
import com.redhat.lightblue.metadata.rdbms.model.IfFieldEmpty;
import com.redhat.lightblue.metadata.rdbms.model.IfFieldRegex;
import com.redhat.lightblue.metadata.rdbms.model.InOut;
import com.redhat.lightblue.metadata.rdbms.model.Operation;
import com.redhat.lightblue.metadata.rdbms.model.Statement;
import com.redhat.lightblue.metadata.rdbms.model.Then;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lcestari
 */
public class RDBMSProcessor {
    public static void process(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, String operation) {
        // result
        List<JsonDoc> result = new ArrayList<>();

        //create the first SQL statements to run the RDBMS module
        List<SelectStmt> inputStmt = Translator.ORACLE.translate(crudOperationContext, rdbmsContext);

        List<InOut> in = new ArrayList<>();
        List<InOut> out = new ArrayList<>();

        Operation op = rdbmsContext.getRdbms().getOperationByName(operation);
        op.getBindings().setInList(in);
        op.getBindings().setOutList(out);

        recursiveExpressionCall(crudOperationContext, rdbmsContext, op, op.getExpressionList());

        new ExecuteUpdateCommand(rdbmsContext, inputStmt).execute();

        // processed final output
        // TODO need to trnasform the out list into the those JSON documents
        crudOperationContext.addDocuments(result);
    }

    private static void recursiveExpressionCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList) {
        if (expressionList == null) {
            return;
        }
        for (Expression expression : expressionList) {
            final String simpleName = expression.getClass().getSimpleName(); // Could be 'if's using isntanceof
            switch (simpleName) {
                case ExpressionOperators.CONDITIONAL:
                    Conditional c = (Conditional) expression;
                    recursiveConditionalCall(crudOperationContext, rdbmsContext, op, expressionList, c);
                    break;
                case ExpressionOperators.FOR:
                    For f = (For) expression;
                    recursiveForCall(crudOperationContext, rdbmsContext, op, expressionList, f);
                    break;
                case ExpressionOperators.FOREACH:
                    ForEach e = (ForEach) expression;
                    recursiveForEachCall(crudOperationContext, rdbmsContext, op, expressionList, e);
                    break;
                case ExpressionOperators.STATEMENT:
                    Statement s = (Statement) expression;
                    recursiveStatementCall(crudOperationContext, rdbmsContext, op, expressionList, s);
                    break;
                default:
                    throw new IllegalStateException("New implementation of Expression not present in ExpressionOperators");
            }
        }
    }

    private static void recursiveConditionalCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList, Conditional c) {
        if (evaluateConditions(c.getIf(), op.getBindings())) {
            recursiveThenCall(crudOperationContext, rdbmsContext, op, expressionList, c.getThen());
        } else {
            boolean notEnter = true;
            if (c.getElseIfList() != null && !c.getElseIfList().isEmpty()) {
                for (ElseIf ef : c.getElseIfList()) {
                    if (evaluateConditions(ef.getIf(), op.getBindings())) {
                        notEnter = false;
                        recursiveThenCall(crudOperationContext, rdbmsContext, op, expressionList, ef.getThen());
                    }
                }
            }
            if (notEnter && c.getElse() != null) {
                recursiveThenCall(crudOperationContext, rdbmsContext, op, expressionList, c.getElse());
            }
        }
    }

    private static void recursiveForCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList, For f) {
        // TODO need to transform the IN and OUT  into a Map to improve the processing performance. Also need that map for variables
        String var = f.getLoopCounterVariableName(); // Update this string everytime 
        int loopTimes = f.getLoopTimes();
        for (int i = 0; i < loopTimes; i++) {
            recursiveExpressionCall(crudOperationContext, rdbmsContext, op, f.getExpressions());
        }
    }

    private static void recursiveForEachCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList, ForEach e) {
        Path field = e.getIterateOverField();
        // TODO finish this implementation. It need to interate over it, I supose that only arrays will be informed. The input variable will change to each value this array contains so the user can iteract with the array content on the Expressions (statements, conditional, etc)
        // TODO if a statement runs and alter the variable of for each, should it notice or not? possible bug        
        for (int i = 0; i < 1; i++) {
            recursiveExpressionCall(crudOperationContext, rdbmsContext, op, e.getExpressions());
        }
    }

    private static void recursiveStatementCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList, Statement s) {
        String type = s.getType();
        String sql = s.getSQL();
        rdbmsContext.setSql(sql);
        rdbmsContext.setType(type);
        new ExecuteUpdateCommand(rdbmsContext).execute();
    }

    private static void recursiveThenCall(CRUDOperationContext crudOperationContext, RDBMSContext rdbmsContext, Operation op, List<Expression> expressionList, Then then) {
        if (then.getExpressions() != null && !then.getExpressions().isEmpty()) {
            recursiveExpressionCall(crudOperationContext, rdbmsContext, op, then.getExpressions());
        } else {
            // "$fail", "$continue", "$break"
            // TODO put the flang into the context and make the static methods aware of it
            then.getLoopOperator();
        }
    }

    static boolean evaluateConditions(If i, Bindings bindings) {
        final String simpleName = i.getClass().getSimpleName();
        final boolean allConditions;
        switch (simpleName) {
            case IfOperators.IFAND:
                allConditions = true;
                for (Object o : i.getConditions()) {
                    if (!evaluateConditions((If) o, bindings)) {
                        return false;
                    }
                }
                break;
            case IfOperators.IFFIELDCHECKFIELD:
            case IfOperators.IFFIELDCHECKVALUE:
            case IfOperators.IFFIELDCHECKVALUES:
            case IfOperators.IFFIELDEMPTY:
            case IfOperators.IFFIELDREGEX:
                return evaluateField(i, bindings, simpleName);
            case IfOperators.IFNOT:
                return !evaluateConditions((If) i.getConditions().get(0), bindings);
            case IfOperators.IFOR:
                allConditions = false;
                for (Object o : i.getConditions()) {
                    if (evaluateConditions((If) o, bindings)) {
                        return true;
                    }
                }
                break;
            default:
                throw new IllegalStateException("New implementation of If not present in IfOperators");
        }
        return allConditions;
    }

    //TODO evaluate the fields
    private static boolean evaluateField(If i, Bindings bindings, String simpleName) {
        switch (simpleName) {
            case IfOperators.IFFIELDCHECKFIELD:
                IfFieldCheckField fcf = (IfFieldCheckField) i;
                return false;
            case IfOperators.IFFIELDCHECKVALUE:
                IfFieldCheckValue fcv = (IfFieldCheckValue) i;
                return false;
            case IfOperators.IFFIELDCHECKVALUES:
                IfFieldCheckValues fcs = (IfFieldCheckValues) i;
                return false;
            case IfOperators.IFFIELDEMPTY:
                IfFieldEmpty fe = (IfFieldEmpty) i;
                return false;
            case IfOperators.IFFIELDREGEX:
                IfFieldRegex fr = (IfFieldRegex) i;
                return false;
            default:
                throw new IllegalStateException("New implementation of If not present in IfOperators");
        }
    }
}
