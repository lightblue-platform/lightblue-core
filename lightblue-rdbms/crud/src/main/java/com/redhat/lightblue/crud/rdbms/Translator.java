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
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.ValueComparisonExpression;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
abstract class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    public static Translator ORACLE = new OracleTranslator();

    public List<SelectStmt> translate(CRUDOperationContext c, QueryExpression q, RDBMSContext r) {
        LOGGER.debug("translate {}", q);
        com.redhat.lightblue.util.Error.push("translateQuery");
        FieldTreeNode f = r.getEntityMetadata().getFieldTreeRoot();

        try {
            ArrayList<SelectStmt> result = new ArrayList<>();
            recursiveTranslate(c, q, r, f, result, 0);
            return result;
        } catch (com.redhat.lightblue.util.Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw com.redhat.lightblue.util.Error.get("Invalid Object!", e.getMessage());
        } finally {
            com.redhat.lightblue.util.Error.pop();
        }
    }

    private void recursiveTranslate(CRUDOperationContext c, QueryExpression q, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        if (q instanceof ArrayContainsExpression) {
            recursiveTranslateArrayContains(c, (ArrayContainsExpression) q, r, f, result, currentIndex);
        } else if (q instanceof ArrayMatchExpression) {
            recursiveTranslateArrayElemMatch(c, (ArrayMatchExpression) q, r, f, result, currentIndex);
        } else if (q instanceof FieldComparisonExpression) {
            recursiveTranslateFieldComparison((FieldComparisonExpression) q, r, f, result, currentIndex);
        } else if (q instanceof NaryLogicalExpression) {
            recursiveTranslateNaryLogicalExpression(c, (NaryLogicalExpression) q, r, f, result, currentIndex);
        } else if (q instanceof NaryRelationalExpression) {
            recursiveTranslateNaryRelationalExpression(c, (NaryRelationalExpression) q, r, f, result, currentIndex);
        } else if (q instanceof RegexMatchExpression) {
            recursiveTranslateRegexMatchExpression((RegexMatchExpression) q, r, f, result, currentIndex);
        } else if (q instanceof UnaryLogicalExpression) {
            recursiveTranslateUnaryLogicalExpression(c, (UnaryLogicalExpression) q, r, f, result, currentIndex);
        } else {
            recursiveTranslateValueComparisonExpression(c, (ValueComparisonExpression) q, r, f, result, currentIndex);
        }
    }

    public abstract void recursiveTranslateArrayContains(CRUDOperationContext c, ArrayContainsExpression arrayContainsExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateArrayElemMatch(CRUDOperationContext c, ArrayMatchExpression arrayMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateFieldComparison(FieldComparisonExpression fieldComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateNaryLogicalExpression(CRUDOperationContext c, NaryLogicalExpression naryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateNaryRelationalExpression(CRUDOperationContext c, NaryRelationalExpression naryRelationalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateRegexMatchExpression(RegexMatchExpression regexMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateUnaryLogicalExpression(CRUDOperationContext c, UnaryLogicalExpression unaryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);

    public abstract void recursiveTranslateValueComparisonExpression(CRUDOperationContext c, ValueComparisonExpression valueComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex);
}
