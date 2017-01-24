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

import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

abstract class IndexQueryProcessorBase<T> extends QueryIteratorSkeleton<T> {

    public static final String ERR_INDEX_ANALYZE="crud:index:QueryAnalysisError";

    protected final List<QueryFieldInfo> fieldInfo;
    protected QueryFieldInfo enclosingArray;
    
    public IndexQueryProcessorBase(List<QueryFieldInfo> fields) {
        this.fieldInfo=fields;
    }
    
    protected QueryFieldInfo findFieldInfo(Path field, QueryExpression clause) {
        for (QueryFieldInfo fi : fieldInfo) {
            if (fi.getClause() == clause) {
                if (fi.getFieldNameInClause().equals(field)) {
                    return fi;
                }
            }
        }
        throw Error.get(ERR_INDEX_ANALYZE, field.toString() + "@" + clause.toString());
    }

    protected SimpleKeySpec simpleKeySpec(QueryFieldInfo finfo) {
        return new SimpleKeySpec(finfo);
    }

    @Override
    protected T itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        return null;
    }

    @Override
    protected T itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        return null;
    }

    @Override
    protected T itrAllMatchExpression(AllMatchExpression q, Path context) {
        return null;
    }

    @Override
    protected T itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        return null;
    }


    protected abstract T processValueComparisonExpression(ValueComparisonExpression q);
    
    @Override
    protected T itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        if(q.getOp()==BinaryComparisonOperator._neq) {
            return null;
        } else {
            return processValueComparisonExpression(q);
        }
    }

    protected abstract T processRegexMatchExpression(RegexMatchExpression q);
    
    @Override
    protected T itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        String pattern=q.getRegex();
        if(pattern.length()>0 && pattern.charAt(0)=='^') {
            return processRegexMatchExpression(q);
        } else {
            return null;
        }
    }

    protected abstract T processInExpression(NaryValueRelationalExpression q);

    @Override
    protected T itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        if(q.getOp()==NaryRelationalOperator._in) {
            return processInExpression(q);
        } else {
            return null;
        }
    }

    protected abstract T processAnyExpression(ArrayContainsExpression q);
    
    @Override
    protected T itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        if(q.getOp()==ContainsOperator._any) {
            return processAnyExpression(q);
        } else
            return null;
    }

    protected abstract T processOrQueries(List<QueryExpression> list,Path context);
    protected abstract T processAndQueries(List<QueryExpression> list,Path context);
    
    @Override
    protected T itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        List<QueryExpression> queries=q.getQueries();
        if(queries.size()==1) {
            return super.iterate(queries.get(0));
        } else {
            if(q.getOp()==NaryLogicalOperator._or) {
                boolean allOk=true;
                // We process the queries if all of them are value comparisons with the same field using equality
                FieldTreeNode field=null;
                for(QueryExpression query:q.getQueries()) {                    
                    QueryFieldInfo fld=null;
                    if(query instanceof ValueComparisonExpression &&
                       ((ValueComparisonExpression)query).getOp()==BinaryComparisonOperator._eq) { 
                        fld=findFieldInfo( ((ValueComparisonExpression)query).getField(), query);
                    } else if(query instanceof NaryValueRelationalExpression &&
                              ((NaryValueRelationalExpression)query).getOp()==NaryRelationalOperator._in) {
                        fld=findFieldInfo( ((NaryValueRelationalExpression)query).getField(), query );
                    }
                    if(fld!=null) {
                        if(field==null) {
                            field=fld.getFieldMd();
                        } else {
                            if(field!=fld.getFieldMd()) {
                                allOk=false;
                                break;
                            }
                        }
                    } else {
                        allOk=false;
                        break;
                    }
                }
                if(allOk) {
                    return processOrQueries(queries,context);
                } else {
                    return null;
                }
            } else {
                return processAndQueries(queries,context);
            }
        }
    }

    protected abstract T processArrayMatchExpression(QueryExpression nestedExpression,Path nestedContext);
    
    @Override
    protected T itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        QueryFieldInfo oldArray=enclosingArray;        
        enclosingArray=findFieldInfo(q.getArray(),q);
        T ret=processArrayMatchExpression(q.getElemMatch(), new Path(new Path(context, q.getArray()), Path.ANYPATH));
        enclosingArray=oldArray;
        return ret;
    }
}
