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
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.metadata.rdbms.model.*;
import com.redhat.lightblue.query.*;
import com.redhat.lightblue.util.*;

import java.util.*;

import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
abstract class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    public static Translator ORACLE = new OracleTranslator();

    protected static class TranslationContext {
        CRUDOperationContext c;
        RDBMSContext r;
        FieldTreeNode f;
        LinkedList<SelectStmt> stmts;
        Map<String, ProjectionMapping> fieldToProjectionMap;
        Map<ProjectionMapping, Join> projectionToJoinMap;
        Map<String, ColumnToField> fieldToTableMap;
        SelectStmt sortDependencies;
        Set<String> nameOfTables;

        // temporary variables
        Path tmpArray;
        Type tmpType;
        List<Value> tmpValues;
        public boolean hasJoins;

        public List<SelectStmt> generateFinalTranslation(){
            ArrayList<SelectStmt> result = new ArrayList<>();
            return result;
        }

        public TranslationContext(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f) {
            this.stmts = new LinkedList<>();
            this.fieldToProjectionMap = new HashMap<>();
            this.fieldToTableMap = new HashMap<>();
            this.sortDependencies = new SelectStmt();
            this.sortDependencies.setOrderBy(new ArrayList<String>());
            this.nameOfTables = new HashSet<>();
            this.c = c;
            this.r = r;
            this.f = f;
            index();
        }

        private void index() {
            for (Join join : r.getRdbms().getSQLMapping().getJoins()) {
                for (ProjectionMapping projectionMapping : join.getProjectionMappings()) {
                    String field = projectionMapping.getField();
                    fieldToProjectionMap.put(field, projectionMapping);
                    projectionToJoinMap.put(projectionMapping,join);
                }
            }
            for (ColumnToField columnToField : r.getRdbms().getSQLMapping().getColumnToFieldMap()) {
                fieldToTableMap.put(columnToField.getField(), columnToField);
            }
        }

        public void clearTmp(){
            this.tmpArray = null;
            this.tmpType = null;
            this.tmpValues = null;
        }
        
        public void clearAll(){
            stmts.clear();
            fieldToProjectionMap.clear();
            this.stmts = null;
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
            //translationContext.s.
            preProcess(translationContext);
            recursiveTranslateQuery(translationContext,r.getQueryExpression());
            List<SelectStmt> translation = translationContext.generateFinalTranslation();
            // TODO test translationContext.clearAll(); for moved the objects ahead to GC , maybe it will be better make this call from the class that called this method
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

    private void preProcess(TranslationContext t) {
        translateSort(t);
        CheckForJoins(t);
        translateFromTo(t);
    }

    protected void CheckForJoins(TranslationContext t){
        if(t.r.getFrom() != null && t.r.getTo() != null){
            if (t.sortDependencies.getOrderBy().size() > 1) {
                t.hasJoins = true;
            }else {
                new FindField().recursiveTranslateQuery(t, t.r.getQueryExpression());
                if(t.nameOfTables.size() >1){
                    t.hasJoins = true;
                }
            }
        }
    }

    protected void translateFromTo(TranslationContext t) {
        translateFromToDependencies(t);
    }

    protected abstract void translateFromToDependencies(TranslationContext t);

    protected void translateSort(TranslationContext translationContext) {
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
    protected void translateSortKey(TranslationContext t, SortKey k) {
        String translatePath = translatePath(k.getField());
        ProjectionMapping projectionMapping = t.fieldToProjectionMap.get(translatePath);
        String field;
        if(projectionMapping.getSort() != null && projectionMapping.getSort().isEmpty()){
            field = projectionMapping.getSort();
        } else {
            field = projectionMapping.getColumn();
        }
        t.sortDependencies.getOrderBy().add(field);

    }

    protected void recursiveTranslateQuery(TranslationContext c, QueryExpression q) {
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

    protected FieldTreeNode resolve(FieldTreeNode fieldTreeNode, Path path) {
        FieldTreeNode node = fieldTreeNode.resolve(path);
        if (node == null) {
            throw com.redhat.lightblue.util.Error.get("Invalid field", path.toString());
        }
        return node;
    }

    protected static String translatePath(Path p) {
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

    protected List<Object> translateValueList(Type t, List<Value> values) {
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

    protected void recursiveTranslateArrayContains(TranslationContext c, ArrayContainsExpression arrayContainsExpression) {
        FieldTreeNode arrayNode = resolve(c.f, arrayContainsExpression.getArray());
        if (arrayNode instanceof ArrayField) {
            c.tmpType = ((ArrayField) arrayNode).getElement().getType();
            c.tmpArray = arrayContainsExpression.getArray();
            c.tmpValues = arrayContainsExpression.getValues();
            switch (arrayContainsExpression.getOp()) {
                case _all:
                    recursiveTranslateArrayContainsAll(c);
                    break;
                case _any:
                    recursiveTranslateArrayContainsAny(c);
                    break;
                case _none:
                    recursiveTranslateArrayContainsNone(c);
                    break;
                default:
                    throw com.redhat.lightblue.util.Error.get("Not mapped field", arrayContainsExpression.toString());
            }
            c.clearTmp();
        } else {
            throw com.redhat.lightblue.util.Error.get("Invalid field", arrayContainsExpression.toString());
        }
    }

    //Possible subquery or it will need to run a query before this
    //{ _id: 1, results: [ 82, 85, 88 ] } { _id: 2, results: [ 75, 88, 89 ] } ->{ results: { $elemMatch: { $gte: 80, $lt: 85 } } }->{ "_id" : 1, "results" : [ 82, 85, 88 ] }
    protected void recursiveTranslateArrayElemMatch(TranslationContext c, ArrayMatchExpression expr){
        FieldTreeNode arrayNode = resolve(c.f, expr.getArray());
        if (arrayNode instanceof ArrayField) {
            ArrayElement el = ((ArrayField) arrayNode).getElement();
            if (el instanceof ObjectArrayElement) {
                FieldTreeNode tmp = c.f;
                c.f = el;
                recursiveTranslateQuery(c, expr.getElemMatch());
                String path = translatePath(expr.getArray());

                // TODO
                c.f = tmp;
            }
        }
        throw com.redhat.lightblue.util.Error.get("Invalid field", expr.toString());
    }

    protected void recursiveTranslateFieldComparison(TranslationContext c, FieldComparisonExpression expr){
        StringBuilder str = new StringBuilder();
        // We have to deal with array references here
        Path rField = expr.getRfield();
        Path lField = expr.getField();
        int rn = rField.nAnys();
        int ln = lField.nAnys();
        if (rn > 0 && ln > 0) {
            // TODO Need to define what would happen in this scenario
        } else if (rn > 0 || ln > 0) {
            // TODO Need to define what would happen in this scenario
        } else {
            // No ANYs, direct comparison
            //TODO put a comparison clause in the statenent, which can turn into more than one in case of join tables with pagination
//            str.append(LITERAL_THIS_DOT).
//                    append(translateJsPath(expr.getField())).
//                    append(BINARY_COMPARISON_OPERATOR_JS_MAP.get(expr.getOp())).
//                    append(LITERAL_THIS_DOT).
//                    append(translateJsPath(expr.getRfield()));
        }

      //  return new BasicDBObject("$where", str.toString());
    }

    protected void recursiveTranslateNaryLogicalExpression(TranslationContext c, NaryLogicalExpression naryLogicalExpression){
//        List<QueryExpression> queries = expr.getQueries();
//        List<DBObject> list = new ArrayList<>(queries.size());
//        for (QueryExpression query : queries) {
//            list.add(translate(context, query));
//        }
//        return new BasicDBObject(NARY_LOGICAL_OPERATOR_MAP.get(expr.getOp()), list);
    }

    protected void recursiveTranslateNaryRelationalExpression(TranslationContext c, NaryRelationalExpression naryRelationalExpression){
//        Type t = resolve(context, expr.getField()).getType();
//        if (t.supportsEq()) {
//            List<Object> values = translateValueList(t, expr.getValues());
//            return new BasicDBObject(translatePath(expr.getField()),
//                    new BasicDBObject(NARY_RELATIONAL_OPERATOR_MAP.get(expr.getOp()),
//                            values));
//        } else {
//            throw Error.get(ERR_INVALID_FIELD, expr.toString());
//        }
    }

    protected void recursiveTranslateRegexMatchExpression(TranslationContext c,RegexMatchExpression regexMatchExpression){
//        StringBuilder options = new StringBuilder();
//        BasicDBObject regex = new BasicDBObject("$regex", expr.getRegex());
//        if (expr.isCaseInsensitive()) {
//            options.append('i');
//        }
//        if (expr.isMultiline()) {
//            options.append('m');
//        }
//        if (expr.isExtended()) {
//            options.append('x');
//        }
//        if (expr.isDotAll()) {
//            options.append('s');
//        }
//        String opStr = options.toString();
//        if (opStr.length() > 0) {
//            regex.append("$options", opStr);
//        }
//        return new BasicDBObject(translatePath(expr.getField()), regex);
    }

    protected void recursiveTranslateUnaryLogicalExpression(TranslationContext c, UnaryLogicalExpression unaryLogicalExpression){
//        return new BasicDBObject(UNARY_LOGICAL_OPERATOR_MAP.get(expr.getOp()), translate(context, expr.getQuery()));
    }

    protected void recursiveTranslateValueComparisonExpression(TranslationContext c, ValueComparisonExpression valueComparisonExpression){
//        Type t = resolve(context, expr.getField()).getType();
//        if (expr.getOp() == BinaryComparisonOperator._eq
//                || expr.getOp() == BinaryComparisonOperator._neq) {
//            if (!t.supportsEq()) {
//                throw Error.get(ERR_INVALID_COMPARISON, expr.toString());
//            }
//        } else {
//            if (!t.supportsOrdering()) {
//                throw Error.get(ERR_INVALID_COMPARISON, expr.toString());
//            }
//        }
//        Object valueObject = t.cast(expr.getRvalue().getValue());
//        if (expr.getField().equals(ID_PATH)) {
//            valueObject = new ObjectId(valueObject.toString());
//        }
//        if (expr.getOp() == BinaryComparisonOperator._eq) {
//            return new BasicDBObject(translatePath(expr.getField()), valueObject);
//        } else {
//            return new BasicDBObject(translatePath(expr.getField()),
//                    new BasicDBObject(BINARY_COMPARISON_OPERATOR_MAP.get(expr.getOp()), valueObject));
//        }
    }


    protected abstract void recursiveTranslateArrayContainsAll(TranslationContext c);
    protected abstract void recursiveTranslateArrayContainsAny(TranslationContext c);
    protected abstract void recursiveTranslateArrayContainsNone(TranslationContext c) ;
}
