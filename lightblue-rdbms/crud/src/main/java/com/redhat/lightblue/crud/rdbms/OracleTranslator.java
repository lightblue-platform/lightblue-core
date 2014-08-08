/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;
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
    protected void translateFromToDependencies(TranslationContext t) {
        if(t.hasJoins){
            for (String s : t.sortDependencies.getOrderBy()) {

            }
        } else {

        }

    }

    protected void recursiveTranslateArrayContainsAll(TranslationContext c) {
        Path array = c.tmpArray;
        Type t =  c.tmpType;
        List<Value> values = c.tmpValues;
        
        String translatePath = translatePath(array);
        List<Object> translateValueList = translateValueList(t, values);
        
        //TODO return new BasicDBObject(translatePath, new BasicDBObject("$all", translateValueList));
    }

    protected void recursiveTranslateArrayContainsAny(TranslationContext c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected void recursiveTranslateArrayContainsNone(TranslationContext c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
