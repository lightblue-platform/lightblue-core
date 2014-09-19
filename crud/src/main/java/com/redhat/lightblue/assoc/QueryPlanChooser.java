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

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.assoc.qrew.QueryRewriter;

import com.redhat.lightblue.util.Path;

public class QueryPlanChooser {

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryPlanChooser.class);

    private static final QueryRewriter qrewriter=new QueryRewriter(true);

    private final CompositeMetadata compositeMetadata;
    private final QueryExpression requestQuery;
    private QueryPlan qplan;
    private QueryPlanIterator qplanIterator;

    private final List<Conjunct> requestQueryClauses=new ArrayList<Conjunct>();

    public QueryPlanChooser(CompositeMetadata cmd,
                            QueryExpression requestQuery) {
        LOGGER.debug("QueryPlanChooser.ctor");
        this.compositeMetadata=cmd;
        qplan=new QueryPlan(compositeMetadata);
        LOGGER.debug("Initial query plan:{}",qplan);

        this.requestQuery=requestQuery;
        LOGGER.debug("Request query:{}",requestQuery);

        // Rewrite request queries in conjunctive normal form
        if(requestQuery!=null) {
            rewriteQuery(requestQuery,requestQueryClauses,qplan);
            LOGGER.debug("Request query clauses:{}",requestQueryClauses);
            assignQueriesToPlanNodes(requestQueryClauses);
        }


        qplanIterator=new QueryPlanIterator(qplan);
    }

    /**
     * Rewrites the query expression in its conjunctive normal form,
     * and adds clauses to the end of the clause list
     */
    private void rewriteQuery(QueryExpression q,List<Conjunct> clauseList,QueryPlan qplan) {
        QueryExpression cnf=qrewriter.rewrite(this.requestQuery);
        LOGGER.debug("Query in conjunctive normal form:{}",cnf);
        if(cnf instanceof NaryLogicalExpression&&
           ((NaryLogicalExpression)cnf).getOp()==NaryLogicalOperator._and) {
            for(QueryExpression clause:((NaryLogicalExpression)cnf).getQueries()) {
                clauseList.add(new Conjunct(clause,compositeMetadata,qplan));
            }
        } else {
            clauseList.add(new Conjunct(cnf,compositeMetadata,qplan));
        }
    }
    
    /**
     * Assigns queries that refer to only one entity to the query plan
     * nodes. These are fixed assignments, and don't change from one
     * query plan to the other
     */
    private void assignQueriesToPlanNodes(List<Conjunct> queries) {
        // All query clauses that depend on fields from a single
        // entity can be assigned to a query plan node. If clauses
        // depend on multiple entities, their assignments may change
        // based on the query plan.
        LOGGER.debug("Assigning queries to query plan nodes");
        for(Conjunct c:queries) {
            if(c.getOnlyReferredNode()!=null) {
                c.getOnlyReferredNode().getConjuncts().add(c);
            }
        }
    }

    public CompositeMetadata getMetadata() {
        return compositeMetadata;
    }

    public QueryExpression getRequestQuery() {
        return requestQuery;
    }   
    
    public QueryPlan getQueryPlan() {
        return qplan;
    }

    public List<Conjunct> getRequestQueryClauses() {
        return requestQueryClauses;
    }
}
