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
package com.redhat.lightblue.mindex;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.assoc.QueryFieldInfo;
import com.redhat.lightblue.assoc.AssocConstants;

import com.redhat.lightblue.metadata.ArrayField;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * This class analyzes a query, and builds a key spec for an index
 * that would be useful in evaluating that query
 */
public class GetIndexKeySpec extends IndexQueryProcessorBase<KeySpec> {

    public GetIndexKeySpec(List<QueryFieldInfo> fields) {
        super(fields);
    }
    
    
    @Override
    protected KeySpec processValueComparisonExpression(ValueComparisonExpression q) {
        return simpleKeySpec(findFieldInfo(q.getField(),q));
    }
    
    @Override
    protected KeySpec processRegexMatchExpression(RegexMatchExpression q) {
        return simpleKeySpec(findFieldInfo(q.getField(),q));
    }

    @Override
    protected KeySpec processInExpression(NaryValueRelationalExpression q) {
        return simpleKeySpec(findFieldInfo(q.getField(),q));
    }
    
    @Override
    protected KeySpec processAnyExpression(ArrayContainsExpression q) {
        return simpleKeySpec(findFieldInfo(q.getArray(),q));
    }

    @Override
    protected KeySpec processOrQueries(List<QueryExpression> list,Path context) {
        // Here, we're sure there's only one field involved in all the queries
        return super.iterate(list.get(0),context);
    }
    
    @Override
    protected KeySpec processAndQueries(List<QueryExpression> list,Path context) {
        // X and Y means we need an index scanning both
        // but there can be sub-expressions with ORs in them, and they will return null
        // So, we collect only nonempty keys, and create an index from them
        List<KeySpec> keys=new ArrayList<>(list.size());
        for(QueryExpression query:list) {
            KeySpec spec=super.iterate(query,context);
            if(spec!=null)
                keys.add(spec);
        }
        if(keys.isEmpty()) {
            return null;
        } else {
            return new CompositeKeySpec(keys.toArray(new KeySpec[keys.size()]));
        }
    }
    
    @Override
    protected KeySpec processArrayMatchExpression(QueryExpression nestedExpression,Path nestedContext) {
        KeySpec nestedSpec=iterate(nestedExpression,nestedContext);
        KeySpec ret;
        if(nestedSpec==null) {
            ret=null;
        } else {
            if(nestedSpec instanceof CompositeKeySpec) {
                ret=new ArrayKeySpec(enclosingArray,((CompositeKeySpec)nestedSpec).keyFields);
            } else {
                ret=new ArrayKeySpec(enclosingArray,new KeySpec[]{nestedSpec});
            }
        }
        return ret;
    }
}
