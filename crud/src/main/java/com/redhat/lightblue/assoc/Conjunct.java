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
package com.redhat.lightblue.assoc;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.Path;

public class Conjunct {
    /**
     * The query clause
     */
    private final QueryExpression clause;
    
    /**
     * Field info for the fields in the clause
     */
    private final List<FieldInfo> fieldInfo;
    
    /**
     * A mapping from absolute field names to the query plan nodes
     * containing that field
     */
    private final Map<Path,QueryPlanNode> fieldNodeMap=new HashMap();
    
    /**
     * If non-null, then the query refers to the fields in only
     * one entity, and this is the query plan node containing that
     * entity. If null, query has fields from more than one
     * entity.
     */
    private final QueryPlanNode onlyReferredNode;
    
    public Conjunct(QueryExpression q,
                    CompositeMetadata compositeMetadata,
                    QueryPlan qplan) {
        this.clause=q;
        this.fieldInfo=clause.getQueryFields();
        QueryPlanNode uniqueNode=null;
        boolean hasMultipleNodes=false;
        for(FieldInfo fi:fieldInfo) {
            CompositeMetadata cmd=compositeMetadata.getEntityOfPath(fi.getAbsFieldName());
            if(cmd==null)
                throw new IllegalArgumentException("Cannot find field in composite metadata "+fi.getAbsFieldName()); 
            QueryPlanNode qnode=qplan.getNode(cmd);
            if(qnode==null)
                throw new IllegalArgumentException("An entity referenced in a query is not in composite metadata. Query:"+clause+" fieldInfo:"+fi+" Composite metadata:"+cmd);
            
            if(uniqueNode==null)
                uniqueNode=qnode;
            else if(uniqueNode!=qnode)
                hasMultipleNodes=true;
            
            fieldNodeMap.put(fi.getAbsFieldName(),qnode);
        }
        if(hasMultipleNodes)
            onlyReferredNode=null;
        else
            onlyReferredNode=uniqueNode;
    }

    public QueryPlanNode getOnlyReferredNode() {
        return onlyReferredNode;
    }
}
