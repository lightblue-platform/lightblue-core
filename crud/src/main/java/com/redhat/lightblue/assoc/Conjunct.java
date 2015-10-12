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

import java.io.Serializable;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.Path;

/**
 * A query clause that cannot be further broken into conjuncts of a
 * conjunctive normal form query. This class also keeps metadata
 * related to that clause, such as the referred nodes of query plan,
 * fieldinfo, etc.
 *
 * Object identity of conjuncts are preserved during query plan
 * processing. That is, a Conjunct object created for a particular
 * query plan is used again for other incarnation of that query plan,
 * only node associations are changed. So, it is possible to keep maps
 * that map a Conjunct to some other piece of data. This is important
 * in query scoring. Scoring can process conjuncts, and keep data
 * internally to prevent recomputing the cost associated with the
 * conjunct for every possible query plan.
 */
public class Conjunct implements Serializable {

    private static final long serialVersionUID=1l;
    
    private static final Logger LOGGER=LoggerFactory.getLogger(Conjunct.class);
    
    /**
     * The original query clause
     */
    private final QueryExpression clause;

    /**
     * Field info for the fields in the clause
     */
    private final ResolvedFieldInfo[] fieldInfo;
    
    /**
     * The list of distinct query plan nodes referred by the clause
     */
    private final Set<QueryPlanNode> referredNodes=new HashSet<>();

    /**
     * Specifies if the query belongs to the request, or one of the reference fields
     */
    private final boolean requestQuery;

    
    public Conjunct(QueryExpression q,
                    CompositeMetadata compositeMetadata,
                    QueryPlan qplan,
                    ResolvedReferenceField context) {
        this.clause=q;
        this.requestQuery=context==null;
        List<FieldInfo> fInfo=clause.getQueryFields();
        LOGGER.debug("Conjunct for query {} with fields {}",q,fInfo);
        fieldInfo=new ResolvedFieldInfo[fInfo.size()];
        int index=0;
        for(FieldInfo fi:fInfo) {
            ResolvedFieldInfo rfi=new ResolvedFieldInfo(fi,compositeMetadata,context,qplan);
            fieldInfo[index++]=rfi;
            referredNodes.add(rfi.getFieldQueryPlanNode());
        }
        
    }

    /**
     * Returns true if the clause belongs to a request query, not to a reference field
     */
    public boolean isRequestQuery() {
        return requestQuery;
    }

    /**
     * Return the relative field name based on the original field name
     */
    public Path mapOriginalFieldName(Path originalFieldName) {
        ResolvedFieldInfo fi=getFieldInfoByOriginalFieldName(originalFieldName);
        if(fi==null)
            return originalFieldName;
        else
            return fi.getEntityRelativeFieldName();
    }

    /**
     * Returns the nodes referenced by this clause
     */
    public Set<QueryPlanNode> getReferredNodes() {
        return referredNodes;
    }


    /**
     * Returns the field information about the fields in the conjunct
     */
    public ResolvedFieldInfo[] getFieldInfo() {
        return fieldInfo;
    }

    /**
     * Return the field info by the field name of the field as used in the unmodified query
     */
    public ResolvedFieldInfo getFieldInfoByOriginalFieldName(Path p) {
        for(ResolvedFieldInfo fi:fieldInfo)
            if(fi.getOriginalFieldName().equals(p))
                return fi;
        return null;
    }

    /**
     * Returns the field info by the field name as it appears in the entity-relative query
     */
    public ResolvedFieldInfo getFieldInfoByRelativeFieldName(Path p) {
        for(ResolvedFieldInfo fi:fieldInfo)
            if(fi.getEntityRelativeFieldName().equals(p))
                return fi;
        return null;
    }

    /**
     * Returns the query clause
     */
    public QueryExpression getClause() {
        return clause;
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
