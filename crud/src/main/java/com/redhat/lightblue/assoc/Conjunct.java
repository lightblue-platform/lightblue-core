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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.Path;

public class Conjunct {
    
    private static final Logger LOGGER=LoggerFactory.getLogger(Conjunct.class);
    
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
     * The list of distinct query plan nodes referred by the clause
     */
    private final List<QueryPlanNode> referredNodes=new ArrayList<>();
    
    public Conjunct(QueryExpression q,
                    CompositeMetadata compositeMetadata,
                    QueryPlan qplan) {
        this.clause=q;
        this.fieldInfo=clause.getQueryFields();
        LOGGER.debug("Conjunct for query {} with fields {}",q,fieldInfo);
        for(FieldInfo fi:fieldInfo) {
            CompositeMetadata cmd=compositeMetadata.getEntityOfPath(fi.getAbsFieldName());
            if(cmd==null)
                throw new IllegalArgumentException("Cannot find field in composite metadata "+fi.getAbsFieldName()); 
            QueryPlanNode qnode=qplan.getNode(cmd);
            if(qnode==null)
                throw new IllegalArgumentException("An entity referenced in a query is not in composite metadata. Query:"+clause+" fieldInfo:"+fi+" Composite metadata:"+cmd);
            
            boolean found=false;
            for(QueryPlanNode n:referredNodes)
                if(n==qnode) {
                    found=true;
                    break;
                }
            if(!found)
                referredNodes.add(qnode);
            fieldNodeMap.put(fi.getAbsFieldName(),qnode);
        }
    }

    /**
     * Returns the nodes referenced by this clause
     */
    public List<QueryPlanNode> getReferredNodes() {
        return referredNodes;
    }

    public String toString() {
        StringBuilder bld=new StringBuilder();
        bld.append("clause=").append(clause.toString()).
            append(" entities=");
        for(QueryPlanNode n:referredNodes)
            bld.append(' ').append(n.getName());
        
        return bld.toString();
    }
}
