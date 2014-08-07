/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.metadata.rdbms.converter.SelectStmt;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.util.Path;
import java.util.List;

/**
 *
 * @author lcestari
 */
class OracleTranslator extends Translator {

    public OracleTranslator() {
    }

    public void recursiveTranslateArrayContains(CRUDOperationContext c, ArrayContainsExpression arrayContainsExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        FieldTreeNode arrayNode = resolve(f, arrayContainsExpression.getArray());
        if (arrayNode instanceof ArrayField) {
            Type t = ((ArrayField) arrayNode).getElement().getType();
            
            switch (arrayContainsExpression.getOp()) {
                case _all:
                    recursiveTranslateArrayContainsAll(c,r,f,t,result,currentIndex, arrayContainsExpression.getArray(), arrayContainsExpression.getValues());
                    break;
                case _any:
                    recursiveTranslateArrayContainsAny(c,r,f,t,result,currentIndex, arrayContainsExpression.getArray(), arrayContainsExpression.getValues());
                    break;
                case _none:
                    recursiveTranslateArrayContainsNone(c,r,f,t,result,currentIndex, arrayContainsExpression.getArray(), arrayContainsExpression.getValues());
                    break;
                default:
                    throw com.redhat.lightblue.util.Error.get("Not mapped field", arrayContainsExpression.toString());
            }
        } else {
            throw com.redhat.lightblue.util.Error.get("Invalid field", arrayContainsExpression.toString());
        }
    }

    @Override
    public void recursiveTranslateArrayElemMatch(CRUDOperationContext c, ArrayMatchExpression arrayMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateFieldComparison(FieldComparisonExpression fieldComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryLogicalExpression(CRUDOperationContext c, NaryLogicalExpression naryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryRelationalExpression(CRUDOperationContext c, NaryRelationalExpression naryRelationalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateRegexMatchExpression(RegexMatchExpression regexMatchExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateUnaryLogicalExpression(CRUDOperationContext c, UnaryLogicalExpression unaryLogicalExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateValueComparisonExpression(CRUDOperationContext c, ValueComparisonExpression valueComparisonExpression, RDBMSContext r, FieldTreeNode f, List<SelectStmt> result, Depth currentIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    private void recursiveTranslateArrayContainsAll(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f, Type t, List<SelectStmt> result, Depth currentIndex, Path array, List<Value> values) {
        String translatePath = translatePath(array);
        List<Object> translateValueList = translateValueList(t, values);
        c.
        return new BasicDBObject(translatePath, new BasicDBObject("$all", translateValueList));
    }

    private void recursiveTranslateArrayContainsAny(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f, Type t, List<SelectStmt> result, Depth currentIndex, Path array, List<Value> values) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void recursiveTranslateArrayContainsNone(CRUDOperationContext c, RDBMSContext r, FieldTreeNode f, Type t, List<SelectStmt> result, Depth currentIndex, Path array, List<Value> values) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
