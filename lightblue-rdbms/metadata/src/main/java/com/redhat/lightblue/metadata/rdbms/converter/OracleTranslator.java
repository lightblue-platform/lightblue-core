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
package com.redhat.lightblue.metadata.rdbms.converter;

import com.redhat.lightblue.metadata.rdbms.model.Join;
import com.redhat.lightblue.metadata.rdbms.model.ProjectionMapping;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.util.*;
import com.redhat.lightblue.util.Error;

import java.util.LinkedList;

/**
 *
 * Translator based on Oracle 10.0+
 * @author lcestari
 */
public class OracleTranslator extends Translator {

    @Override
    protected void generateWhere(SelectStmt s, StringBuilder queryStr, LinkedList<String> whereConditionals) {
        super.generateWhere(s,queryStr,whereConditionals);
        Long limit = s.getLimit();
        Long offset = s.getOffset();
        if (s.getLimit() != null && offset != null) {
            offset = offset +limit;
            queryStr.append("AND ROWNUM BETWEEN ").append(Long.toString(offset)).append(" AND ").append(Long.toString(limit)).append(" ");
        } else if (limit != null) {
            queryStr.append("AND ROWNUM >= ").append(Long.toString(limit)).append(" ");
        } else if (offset != null) {
            queryStr.append("AND ROWNUM <=").append(Long.toString(offset)).append(" ");
        }
    }

    @Override
    protected void generateLimitOffset(SelectStmt s, StringBuilder queryStr, Long limit, Long offset) {
        //Do nothing
    }

    @Override
    protected void recursiveTranslateRegexMatchExpression(TranslationContext c, RegexMatchExpression expr) {
        String regex = expr.getRegex();
        Path lField = expr.getField();

        String f = lField.toString();

        ProjectionMapping fpm = c.fieldToProjectionMap.get(f);
        Join fJoin = c.projectionToJoinMap.get(fpm);
        fillTables(c, c.baseStmt.getFromTables(), fJoin);
        fillWhere(c, c.baseStmt.getWhereConditionals(), fJoin);

        if(c.notOp){
            throw Error.get("not supported operator", expr.toString());
        }
        String options = expr.isCaseInsensitive()?"i":"c";
        options = options + (expr.isDotAll()?"n":"");
        options = options + (expr.isMultiline()?"m":"");
        String s =  "REGEXP_LIKE("+ fpm.getColumn() +",'"+ regex + "','"+ options +"')";
        addConditional(c, s);
    }
}
