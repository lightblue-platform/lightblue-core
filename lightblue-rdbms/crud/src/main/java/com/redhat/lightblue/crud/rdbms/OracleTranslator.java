/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.ValueComparisonExpression;
import java.util.List;

/**
 *
 * @author lcestari
 */
class OracleTranslator extends Translator {

    public OracleTranslator() {
    }

    @Override
    public void recursiveTranslateArrayContains(CRUDOperationContext c, ArrayContainsExpression arrayContainsExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateArrayElemMatch(CRUDOperationContext c, ArrayMatchExpression arrayMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateFieldComparison(FieldComparisonExpression fieldComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryLogicalExpression(CRUDOperationContext c, NaryLogicalExpression naryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryRelationalExpression(CRUDOperationContext c, NaryRelationalExpression naryRelationalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateRegexMatchExpression(RegexMatchExpression regexMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateUnaryLogicalExpression(CRUDOperationContext c, UnaryLogicalExpression unaryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateValueComparisonExpression(CRUDOperationContext c, ValueComparisonExpression valueComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, int currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
