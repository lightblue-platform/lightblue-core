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

import java.util.stream.Collectors;

import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.metadata.ArrayField;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Given a query and key spec, builds a lookup spec
 * 
 * This class follows the same pattern as GetIndexKeySpec
 */
public class GetIndexLookupSpec extends IndexQueryProcessorBase<LookupSpec> {
    private static final String REGEX_CHARS="$[]*.\\^-&|{}()?+:<>!=";
   
    public GetIndexLookupSpec(List<QueryFieldInfo> l) {
        super(l);
    }
    
    @Override
    protected LookupSpec processValueComparisonExpression(ValueComparisonExpression q) {
        switch(q.getOp()) {
        case _eq:
            return new ValueLookupSpec(simpleKeySpec(findFieldInfo(q.getField(),q)),q.getRvalue());
        case _lte:
        case _lt:
            return new RangeLookupSpec(simpleKeySpec(findFieldInfo(q.getField(),q)),null,q.getRvalue());
        case _gte:
        case _gt:
            return new RangeLookupSpec(simpleKeySpec(findFieldInfo(q.getField(),q)),q.getRvalue(),null);
        }
        return null;
    }

    static String getPrefix(String pattern) {
        StringBuilder bld=new StringBuilder();
        int n=pattern.length();
        for(int i=0;i<n;i++) {
            char c=pattern.charAt(i);
            if(i==0) {
                if(c=='^') {
                    // ok
                } else if(pattern.startsWith("\\A")) {
                    // ok
                    i++; // pass A
                } else {
                    break;
                }
            } else {
                if(REGEX_CHARS.indexOf(c)!=-1)
                    bld.append(c);
                else
                    break;
            }
        }
        return bld.toString();
    }
   
    @Override
    protected LookupSpec processRegexMatchExpression(RegexMatchExpression q) {
        return new PrefixLookupSpec(simpleKeySpec(findFieldInfo(q.getField(),q)),getPrefix(q.getRegex()),q.isCaseInsensitive());
    }

    @Override
    protected LookupSpec processInExpression(NaryValueRelationalExpression q) {
        return new MultiValueLookupSpec(simpleKeySpec(findFieldInfo(q.getField(),q)),
                                        q.getValues().stream().map(Value::getValue).collect(Collectors.toList()));
    }
    
    @Override
    protected LookupSpec processAnyExpression(ArrayContainsExpression q) {
        return new MultiValueLookupSpec(simpleKeySpec(findFieldInfo(q.getArray(),q)),
                                        q.getValues().stream().map(Value::getValue).collect(Collectors.toList()));
    }
    

    @Override
    protected LookupSpec processOrQueries(List<QueryExpression> list,Path context) {
        // Here, we're sure that all queries in the list refer to the same field, and there is only one
        // SimpleKeySpec for the whole list
        // Thus, we build a multiValuelookupSpec
        List<Object> values=new ArrayList<>(list.size());
        QueryExpression firstq=null;
        Path firstPath=null;
        boolean first=true;
        for(QueryExpression q:list) {
            if(first) 
                firstq=q;
            if(q instanceof ValueComparisonExpression) {
                if(first)
                    firstPath=((ValueComparisonExpression)q).getField();
                values.add( ((ValueComparisonExpression)q).getRvalue().getValue() );
            } else if(q instanceof NaryValueRelationalExpression) {
                if(first)
                    firstPath=((NaryValueRelationalExpression)q).getField();
                for(Value v: ((NaryValueRelationalExpression)q).getValues()) {
                    values.add(v.getValue());
                }
            }
            first=false;
        }
        return new MultiValueLookupSpec(simpleKeySpec(findFieldInfo(firstPath,firstq)),values);
    }
    
    @Override
    protected LookupSpec processAndQueries(List<QueryExpression> list,Path context) {
        List<LookupSpec> specs=new ArrayList<>(list.size());
        for(QueryExpression query:list) {
            LookupSpec spec=super.iterate(query,context);
            if(spec!=null)
                specs.add(spec);
        }
        if(specs.isEmpty()) {
            return null;
        } else {
            return new CompositeLookupSpec(specs.toArray(new LookupSpec[specs.size()]));
        }
    }
    
    @Override
    protected LookupSpec processArrayMatchExpression(QueryExpression nestedExpression,Path nestedContext) {
        LookupSpec nestedSpec=iterate(nestedExpression,nestedContext);
        if(nestedSpec==null) {
            return null;
        } else {
            if(nestedSpec instanceof CompositeLookupSpec) {
                return new ArrayLookupSpec(((CompositeLookupSpec)nestedSpec).values);
            } else {
                return new ArrayLookupSpec(new LookupSpec[]{nestedSpec});
            }
        }
    }
}
