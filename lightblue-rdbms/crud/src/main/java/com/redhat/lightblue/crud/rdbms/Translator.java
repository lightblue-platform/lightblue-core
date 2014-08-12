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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
// TODO Need to define some details how complex queries will be handles, for example: which expression would produce a query which joins two tables with 1->N relationship with paging (limit, offfset and sort), how would needs will be mapped by rdbms' json schema (it would need to map PK (or PKS in case of compose) and know which ones it would need to do a query before (to not brute force and do for all tables)) (in other words the example could be expressed as "find the first X customer after Y and get its first address", where customer 1 -> N addresses)
abstract class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);

    public static Translator ORACLE = new OracleTranslator();
    private static final Map<BinaryComparisonOperator, String> BINARY_TO_SQL = new HashMap<>();

    private static final HashMap<NaryLogicalOperator, String> NARY_TO_SQL = new HashMap<>();

    static {
        BINARY_TO_SQL.put(BinaryComparisonOperator._eq, "=");
        BINARY_TO_SQL.put(BinaryComparisonOperator._neq, "<>");
        BINARY_TO_SQL.put(BinaryComparisonOperator._lt, "<");
        BINARY_TO_SQL.put(BinaryComparisonOperator._gt, ">");
        BINARY_TO_SQL.put(BinaryComparisonOperator._lte, "<=");
        BINARY_TO_SQL.put(BinaryComparisonOperator._gte, ">=");

        NARY_TO_SQL.put(NaryLogicalOperator._and, "and");
        NARY_TO_SQL.put(NaryLogicalOperator._or, "or");
    }

    protected static class TranslationContext {
        CRUDOperationContext c;
        RDBMSContext r;
        FieldTreeNode f;
        Map<String, ProjectionMapping> fieldToProjectionMap;
        Map<ProjectionMapping, Join> projectionToJoinMap;
        Map<String, ColumnToField> fieldToTablePkMap;
        SelectStmt sortDependencies;
        Set<String> nameOfTables;
        boolean needDistinct;
        Boolean notOp;

        // temporary variables
        Path tmpArray;
        Type tmpType;
        List<Value> tmpValues;

        public boolean hasJoins;
        public boolean hasSortOrLimit;


        LinkedList<SelectStmt> firstStmts; // Useful for complex queries which need to run before the  main one
        SelectStmt baseStmt;
        List<Map.Entry<String,List<String>>> logicalStmt;


        public TranslationContext(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f) {
            this.firstStmts = new LinkedList<>();
            this.fieldToProjectionMap = new HashMap<>();
            this.fieldToTablePkMap = new HashMap<>();
            this.sortDependencies = new SelectStmt();
            this.sortDependencies.setOrderBy(new ArrayList<String>());
            this.nameOfTables = new HashSet<>();
            this.baseStmt =  new SelectStmt();
            this.logicalStmt =  new ArrayList<>();
            this.c = c;
            this.r = r;
            this.f = f;
            index();
        }

        public List<SelectStmt> generateFinalTranslation(){
            ArrayList<SelectStmt> result = new ArrayList<>();
            return result;
        }

        private void index() {
            for (Join join : r.getRdbms().getSQLMapping().getJoins()) {
                for (ProjectionMapping projectionMapping : join.getProjectionMappings()) {
                    String field = projectionMapping.getField();
                    fieldToProjectionMap.put(field, projectionMapping);
                    projectionToJoinMap.put(projectionMapping,join);
                }
                needDistinct = join.isNeedDistinct() || needDistinct;
            }
            for (ColumnToField columnToField : r.getRdbms().getSQLMapping().getColumnToFieldMap()) {
                fieldToTablePkMap.put(columnToField.getField(), columnToField);
            }
        }

        public void clearTmp(){
            this.tmpArray = null;
            this.tmpType = null;
            this.tmpValues = null;
        }
        
        public void clearAll(){
            firstStmts.clear();
            fieldToProjectionMap.clear();
            this.firstStmts = null;
            this.c = null;
            this.r = null;
            this.f = null;
            this.clearTmp();

        }


        public void checkJoins(){
            if(nameOfTables.size() >1){
                hasJoins = true;
            }
        }
    }

    public List<SelectStmt> translate(CRUDOperationContext c, RDBMSContext r) {
        LOGGER.debug("translate {}", r.getQueryExpression());
        com.redhat.lightblue.util.Error.push("translateQuery");
        FieldTreeNode f = r.getEntityMetadata().getFieldTreeRoot();

        try {
            TranslationContext translationContext = new TranslationContext(c, r, f);
            preProcess(translationContext);
            recursiveTranslateQuery(translationContext,r.getQueryExpression());
            posProcess(translationContext);
            List<SelectStmt> translation = translationContext.generateFinalTranslation();
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

    private void posProcess(TranslationContext t) {
        t.checkJoins();
    }

    private void preProcess(TranslationContext t) {
        translateSort(t);
        translateFromTo(t);
    }

    protected void translateFromTo(TranslationContext t) {
        if(t.r.getTo() != null || t.r.getFrom() != null){
            t.hasSortOrLimit = true;
        }
    }

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
        t.hasSortOrLimit = true;

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

    protected void recursiveTranslateArrayContains(TranslationContext c, ArrayContainsExpression expr) {
        FieldTreeNode arrayNode = resolve(c.f, expr.getArray());
        if (arrayNode instanceof ArrayField) {
            c.tmpType = ((ArrayField) arrayNode).getElement().getType();
            c.tmpArray = expr.getArray();
            c.tmpValues = expr.getValues();
            String op;
            switch (expr.getOp()) {
                case _all:
                    op = "IN";
                    break;
                case _any:
                    op = null; //OR
                    break;
                case _none:
                    op = "IN";
                    break;
                default:
                    throw com.redhat.lightblue.util.Error.get("Not mapped field", expr.toString());
            }
            Type t = resolve(c.f, expr.getArray()).getType();
            if(op != null) {
                List<Object> values = translateValueList(t, expr.getValues());
                String f = expr.getArray().toString();
                ProjectionMapping fpm = c.fieldToProjectionMap.get(f);
                Join fJoin = c.projectionToJoinMap.get(fpm);
                fillTables(c, c.baseStmt.getFromTables(), fJoin);
                fillWhere(c, c.baseStmt.getWhereConditionals(), fJoin);
                String s = null;
                if (t.supportsEq()) {
                    s = fpm.getColumn() + " " + op + " " + "(\"" + StringUtils.join(values, "\",\"") + "\")";
                }else{
                    for (int i = 0; i < values.size(); i++) {
                        Object v = values.get(i);
                        s = s + fpm.getColumn() + " = " +v;
                        if(i != values.size()-1){
                            s = s +" OR ";
                        }
                    }
                }
                addConditional(c, s);
            } else {
                throw Error.get("invalid field", expr.toString());
            }
            c.clearTmp();
        } else {
            throw com.redhat.lightblue.util.Error.get("Invalid field", expr.toString());
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
                // TODO Need to define what would happen in this scenario (not supported yet)
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
            String f = expr.getField().toString();
            String r = expr.getRfield().toString();

            ProjectionMapping fpm = c.fieldToProjectionMap.get(f);
            Join fJoin = c.projectionToJoinMap.get(fpm);
            fillTables(c, c.baseStmt.getFromTables(), fJoin);
            fillWhere(c, c.baseStmt.getWhereConditionals(), fJoin);

            ProjectionMapping rpm = c.fieldToProjectionMap.get(r);
            Join rJoin = c.projectionToJoinMap.get(rpm);
            fillTables(c, c.baseStmt.getFromTables(), rJoin);
            fillWhere(c, c.baseStmt.getWhereConditionals(), rJoin);

            String s = fpm.getColumn() + " " + BINARY_TO_SQL.get(expr.getOp()) + " " + rpm.getColumn();
            addConditional(c, s);
        }
    }

    private void addConditional(TranslationContext c, String s) {
        if(c.logicalStmt.size() > 0){
            c.logicalStmt.get(c.logicalStmt.size()-1).getValue().add(s);
        } else {
            c.baseStmt.getWhereConditionals().add(s);
        }
    }

    private void fillWhere(TranslationContext c, List<String> wheres, Join fJoin) {
        wheres.add(fJoin.getJoinTablesStatement());
    }

    private void fillTables(TranslationContext c, List<String> fromTables, Join fJoin) {
        for (Table table : fJoin.getTables()) {
            if(c.nameOfTables.add(table.getName())){
                LOGGER.warn("Table mentioned more than once in the same query. Possible N+1 problem");
            }
            if(table.getAlias() != null && table.getAlias().isEmpty() ){
                fromTables.add(table.getName() + " AS " + table.getAlias() );
            } else {
                fromTables.add(table.getName());
            }
        }
    }

    protected void recursiveTranslateNaryLogicalExpression(TranslationContext c, NaryLogicalExpression naryLogicalExpression){
        String ops = NARY_TO_SQL.get(naryLogicalExpression.getOp());
        boolean b = c.logicalStmt.size() == 0;
        c.logicalStmt.add( new AbstractMap.SimpleEntry<String,List<String>>(ops, new ArrayList<String>()));
        recursiveTranslateQuery(c,naryLogicalExpression);
        Map.Entry<String, List<String>> remove = c.logicalStmt.remove(c.logicalStmt.size()-1);
        List<String> op = remove.getValue();
        StringBuilder sb = new StringBuilder();
        if(!b){
            sb.append("(");
        }
        for (int i = 0; i < remove.getValue().size() ; i++) {
            String s = remove.getValue().get(i);
            if(i == (remove.getValue().size()-1)) {
                sb.append(s);
                if(!b){
                    sb.append(")");
                }
            } else {
                sb.append(s).append(" ").append(op);
            }
        }
        if(b) {
            c.baseStmt.getWhereConditionals().add(sb.toString());
        } else {
            c.logicalStmt.get(c.logicalStmt.size()-1).getValue().add(sb.toString());
        }
    }

    protected void recursiveTranslateNaryRelationalExpression(TranslationContext c, NaryRelationalExpression expr){
        Type t = resolve(c.f, expr.getField()).getType();
        if (t.supportsEq()) {
            List<Object> values = translateValueList(t, expr.getValues());
            String f = expr.getField().toString();
            String op = expr.getOp().toString().equals("$in") ? "IN" : "NOT IN";

            ProjectionMapping fpm = c.fieldToProjectionMap.get(f);
            Join fJoin = c.projectionToJoinMap.get(fpm);
            fillTables(c, c.baseStmt.getFromTables(), fJoin);
            fillWhere(c, c.baseStmt.getWhereConditionals(), fJoin);
            String s = fpm.getColumn() + " " + op + " " +  "(\"" +StringUtils.join(values, "\",\"")+"\")";
            addConditional(c, s);
        } else {
            throw Error.get("invalid field", expr.toString());
        }
    }

    protected void recursiveTranslateRegexMatchExpression(TranslationContext c,RegexMatchExpression expr){
        throw Error.get("invalid operator", expr.toString());
    }

    protected void recursiveTranslateUnaryLogicalExpression(TranslationContext c, UnaryLogicalExpression expr){
        c.notOp = true; //TODO invert the logic when the flag is true and turn it to false once again
        recursiveTranslateQuery(c, expr.getQuery());
    }

    protected void recursiveTranslateValueComparisonExpression(TranslationContext c, ValueComparisonExpression expr){
        StringBuilder str = new StringBuilder();
        // We have to deal with array references here
        Value rvalue = expr.getRvalue();
        Path lField = expr.getField();
        int ln = lField.nAnys();
        if (ln > 0) {
            // TODO Need to define what would happen in this scenario
        } else if (ln > 0) {
            // TODO Need to define what would happen in this scenario
        } else {
            // No ANYs, direct comparison
            String f = lField.toString();
            String r = rvalue.toString();

            ProjectionMapping fpm = c.fieldToProjectionMap.get(f);
            Join fJoin = c.projectionToJoinMap.get(fpm);
            fillTables(c, c.baseStmt.getFromTables(), fJoin);
            fillWhere(c, c.baseStmt.getWhereConditionals(), fJoin);

            String s = fpm.getColumn() + " " + BINARY_TO_SQL.get(expr.getOp()) + " " + r;
            addConditional(c, s);
        }
    }


}
