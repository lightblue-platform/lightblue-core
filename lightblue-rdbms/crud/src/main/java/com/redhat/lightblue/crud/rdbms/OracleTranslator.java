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

    @Override
    public void recursiveTranslateArrayContains(TranslationContext c, ArrayContainsExpression arrayContainsExpression) {
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

    @Override
    public void recursiveTranslateArrayElemMatch(TranslationContext c, ArrayMatchExpression arrayMatchExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateFieldComparison(TranslationContext c, FieldComparisonExpression fieldComparisonExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryLogicalExpression(TranslationContext c, NaryLogicalExpression naryLogicalExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateNaryRelationalExpression(TranslationContext c, NaryRelationalExpression naryRelationalExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateRegexMatchExpression(TranslationContext c, RegexMatchExpression regexMatchExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateUnaryLogicalExpression(TranslationContext c, UnaryLogicalExpression unaryLogicalExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void recursiveTranslateValueComparisonExpression(TranslationContext c, ValueComparisonExpression valueComparisonExpression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void recursiveTranslateArrayContainsAll(TranslationContext c) {
        Path array = c.tmpArray;
        Type t =  c.tmpType;
        List<Value> values = c.tmpValues;
        
        String translatePath = translatePath(array);
        List<Object> translateValueList = translateValueList(t, values);
        
        //TODO return new BasicDBObject(translatePath, new BasicDBObject("$all", translateValueList));
    }

    private void recursiveTranslateArrayContainsAny(TranslationContext c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void recursiveTranslateArrayContainsNone(TranslationContext c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
