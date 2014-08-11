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
