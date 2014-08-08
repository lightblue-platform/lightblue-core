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

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.CompositeSortKey;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.util.Path;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
abstract class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    public static Translator ORACLE = new OracleTranslator();
  
    public static class TranslationContext {
        CRUDOperationContext c;
        RDBMSContext r;
        FieldTreeNode f;
        Stack<SelectStmt> s;
        
        
        Path tmpArray;
        Type tmpType;
        List<Value> tmpValues;
        
        public List<SelectStmt> generateFinalTranslation(){
            ArrayList<SelectStmt> result = new ArrayList<>();
            return result;
        }

        public TranslationContext(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f) {
            this.s = new Stack<>();
            this.c = c;
            this.r = r;
            this.f = f;
        }
        
        public void clearTmp(){
            this.tmpArray = null;
            this.tmpType = null;
            this.tmpValues = null;
        }
        
        public void clearAll(){
            s.clear();
            this.s = null;
            this.c = null;
            this.r = null;
            this.f = null;
            this.clearTmp();
        }
    }

    public List<SelectStmt> translate(CRUDOperationContext c, RDBMSContext r) {
        LOGGER.debug("translate {}", r.getQueryExpression());
        com.redhat.lightblue.util.Error.push("translateQuery");
        FieldTreeNode f = r.getEntityMetadata().getFieldTreeRoot();

        try {
            TranslationContext translationContext = new TranslationContext(c, r, f);
            translateSort(translationContext);
            recursiveTranslateQuery(translationContext,r.getQueryExpression());
            List<SelectStmt> translation = translationContext.generateFinalTranslation();
            translationContext.clearAll();
            return translation;
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
    private void translateSort(TranslationContext translationContext) {
        Sort sort = translationContext.r.getSort();
        if(sort instanceof CompositeSortKey) {
            CompositeSortKey c =  (CompositeSortKey) sort;
            for (SortKey k : c.getKeys()) {
                translateSortKey(translationContext, k);            
            }

        } else {
            SortKey k = (SortKey) sort;
            translateSortKey(translationContext, k);            
        }
    }
    private void translateSortKey(TranslationContext translationContext, SortKey k) {            
        
    }
    
    private void recursiveTranslateQuery(TranslationContext c, QueryExpression q) {
        if (q instanceof ArrayContainsExpression) {
            recursiveTranslateArrayContains(c, (ArrayContainsExpression) q);
        } else if (q instanceof ArrayMatchExpression) {
            recursiveTranslateArrayElemMatch(c, (ArrayMatchExpression) q);
        } else if (q instanceof FieldComparisonExpression) {
            recursiveTranslateFieldComparison(c, (FieldComparisonExpression) q);
        } else if (q instanceof NaryLogicalExpression) {
            recursiveTranslateNaryLogicalExpression(c, (NaryLogicalExpression) q);
        } else if (q instanceof NaryRelationalExpression) {
            recursiveTranslateNaryRelationalExpression(c, (NaryRelationalExpression) q);
        } else if (q instanceof RegexMatchExpression) {
            recursiveTranslateRegexMatchExpression(c, (RegexMatchExpression) q);
        } else if (q instanceof UnaryLogicalExpression) {
            recursiveTranslateUnaryLogicalExpression(c, (UnaryLogicalExpression) q);
        } else {
            recursiveTranslateValueComparisonExpression(c, (ValueComparisonExpression) q);
        }
    }
    
    public FieldTreeNode resolve(FieldTreeNode fieldTreeNode, Path path) {
        FieldTreeNode node = fieldTreeNode.resolve(path);
        if (node == null) {
            throw com.redhat.lightblue.util.Error.get("Invalid field", path.toString());
        }
        return node;
    }
    
    public static String translatePath(Path p) {
        StringBuilder str = new StringBuilder();
        int n = p.numSegments();
        for (int i = 0; i < n; i++) {
            String s = p.head(i);
            if (!s.equals(Path.ANY)) {
                if (i > 0) {
                    str.append('.');
                }
                str.append(s);
            }
        }
        return str.toString();
    }
    
    public List<Object> translateValueList(Type t, List<Value> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Empty values");
        }
        List<Object> ret = new ArrayList<>(values.size());
        for (Value v : values) {
            Object value = v == null ? null : v.getValue();
            if (value != null) {
                value = t.cast(value);
            }
            ret.add(value);
        }
        return ret;
    }

    public abstract void recursiveTranslateArrayContains(TranslationContext c, ArrayContainsExpression arrayContainsExpression);

    public abstract void recursiveTranslateArrayElemMatch(TranslationContext c, ArrayMatchExpression arrayMatchExpression);

    public abstract void recursiveTranslateFieldComparison(TranslationContext c,FieldComparisonExpression fieldComparisonExpression);

    public abstract void recursiveTranslateNaryLogicalExpression(TranslationContext c, NaryLogicalExpression naryLogicalExpression);

    public abstract void recursiveTranslateNaryRelationalExpression(TranslationContext c, NaryRelationalExpression naryRelationalExpression);

    public abstract void recursiveTranslateRegexMatchExpression(TranslationContext c,RegexMatchExpression regexMatchExpression);

    public abstract void recursiveTranslateUnaryLogicalExpression(TranslationContext c, UnaryLogicalExpression unaryLogicalExpression);

    public abstract void recursiveTranslateValueComparisonExpression(TranslationContext c, ValueComparisonExpression valueComparisonExpression);
}
