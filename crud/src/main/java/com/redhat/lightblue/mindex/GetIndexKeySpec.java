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

class GetQueryIndexInfo extends IndexQueryProcessorBase<KeySpec> {

    private ArrayField enclosingArrayMd;
    
    public GetQueryIndexInfo(List<QueryFieldInfo> fields) {
        super(fields);
    }
    
    @Override
    protected KeySpec itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        return null;
    }

    @Override
    protected KeySpec itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        return null;
    }

    @Override
    protected KeySpec itrAllMatchExpression(AllMatchExpression q, Path context) {
        return null;
    }

    private KeySpec simpleKeySpec(QueryFieldInfo finfo) {
        if(enclosingArrayMd==null) {
            return new SimpleKeySpec(finfo.getFieldMd());
        } else {
            return new SimpleKeySpec(enclosingArrayMd,finfo.getFieldMd());
        }
    }
    
    @Override
    protected KeySpec itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        if(q.getOp()==BinaryComparisonOperator._neq) {
            return null;
        } else {
            QueryFieldInfo finfo=findFieldInfo(q.getField(),q);
            return simpleKeySpec(finfo);
        }
    }
        
    @Override
    protected KeySpec itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        String pattern=q.getRegex();
        if(pattern.length()>0 && pattern.charAt(0)=='^') {
            QueryFieldInfo finfo=findFieldInfo(q.getField(),q);
            return simpleKeySpec(finfo);
        } else {
            return null;
        }
    }

    @Override
    protected KeySpec itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        if(q.getOp()==NaryRelationalOperator._in) {
            QueryFieldInfo finfo=findFieldInfo(q.getField(),q);
            return simpleKeySpec(finfo);
        } else {
            return null;
        }
    }
    
    @Override
    protected KeySpec itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        if(q.getOp()==ContainsOperator._any) {
            QueryFieldInfo finfo=findFieldInfo(q.getArray(),q);
            return simpleKeySpec(finfo);
        } else
            return null;
    }
    
    @Override
    protected KeySpec itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        // Negation makes index useless
        return null;
    }
    
    @Override
    protected KeySpec itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        List<KeySpec> keys=new ArrayList<>(q.getQueries().size());
        if(q.getOp()==NaryLogicalOperator._or) {
            // Keep it simple: ORin queries with the same field is ok. Using different fields is not.
            for(QueryExpression query:q.getQueries()) {
                KeySpec spec=super.iterate(query,context);
                if(spec instanceof SimpleKeySpec) {
                    // This is useful only if a single field is used
                    if(keys.isEmpty()) {
                        keys.add(spec);
                    } else {
                        SimpleKeySpec k=(SimpleKeySpec)keys.get(0);
                        if(k.fieldMd!=((SimpleKeySpec)spec).fieldMd) {
                            keys.clear();
                            break;
                        }
                    }
                } else {
                    // Can't use this field as an index
                    keys.clear();
                    break;
                }
            }
        } else {
            // X and Y means we need an index scanning both
            // but there can be sub-expressions with ORs in them, and they will return null
            // So, we collect only nonempty keys, and create an index from them
            for(QueryExpression query:q.getQueries()) {
                KeySpec spec=super.iterate(query,context);
                if(spec!=null)
                    keys.add(spec);
            }
        }
        if(keys.isEmpty()) {
            return null;
        } else if(keys.size()==1) {
            return keys.get(0);
        } else {
            return new CompositeKeySpec(keys.toArray(new KeySpec[keys.size()]));
        }
    }

    
    @Override
    protected KeySpec itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        ArrayField oldArray=enclosingArrayMd;        
        QueryFieldInfo finfo=findFieldInfo(q.getArray(),q);
        enclosingArrayMd=(ArrayField)finfo.getFieldMd();
        KeySpec nestedSpec=iterate(q.getElemMatch(), new Path(new Path(context, q.getArray()), Path.ANYPATH));
        KeySpec ret;
        if(nestedSpec==null) {
            ret=null;
        } else {
            if(nestedSpec instanceof CompositeKeySpec) {
                ret=new ArrayKeySpec(enclosingArrayMd, ((CompositeKeySpec)nestedSpec).keyFields);
            } else {
                ret=new ArrayKeySpec(enclosingArrayMd ,new KeySpec[]{nestedSpec});
            }
        }
        enclosingArrayMd=oldArray;
        return ret;
    }
}
