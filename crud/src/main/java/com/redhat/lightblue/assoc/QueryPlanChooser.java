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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.assoc.qrew.QueryRewriter;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class QueryPlanChooser {

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryPlanChooser.class);

    private static final QueryRewriter qrewriter=new QueryRewriter(true);

    private final CompositeMetadata compositeMetadata;
    private final QueryExpression requestQuery;
    private final QueryPlanIterator qplanIterator;
    private final QueryPlanScorer scorer;

    private QueryPlan qplan;

    private QueryPlan bestPlan;
    private Comparable bestPlanScore;
    
    
    /**
     * Query clauses that come from the request
     */
    private final List<Conjunct> requestQueryClauses=new ArrayList<Conjunct>();

    public QueryPlanChooser(CompositeMetadata cmd,
                            QueryPlanIterator qpitr,
                            QueryPlanScorer scorer,
                            QueryExpression requestQuery) {
        LOGGER.debug("QueryPlanChooser.ctor");
        Error.push("QueryPlanChooser");
        try {
            this.compositeMetadata=cmd;
            this.qplanIterator=qpitr;
            this.scorer=scorer;
            qplan=new QueryPlan(compositeMetadata,scorer);
            LOGGER.debug("Initial query plan:{}",qplan);
            
            this.requestQuery=requestQuery;
            LOGGER.debug("Request query:{}",requestQuery);
            
            // Rewrite  request queries  in  conjunctive  normal form  and
            // assign them to nodes/edges
            if(requestQuery!=null) {
                rewriteQuery(compositeMetadata.getFieldTreeRoot(),requestQuery,requestQueryClauses,qplan);
                LOGGER.debug("Request query clauses:{}",requestQueryClauses);
                assignQueriesToPlanNodesAndEdges(requestQueryClauses,qplan.getUnassignedClauses());
                LOGGER.debug("Completed assigning request query clauses");
            }
            
            // Rewrite association queries in conjunctive normal form, and
            // assign them to nodes/edges
            iterateReferences(compositeMetadata,qplan.getUnassignedClauses());
            
            reset();

            // Check unimplemented features. If there is anything in
            // unassigned clauses list, we fail
            for(Conjunct x:qplan.getUnassignedClauses())
                switch(x.getReferredNodes().size()) {
                case 2: throw Error.get(AssocConstants.ERR_UNRELATED_ENTITY_Q);
                default: throw Error.get(AssocConstants.ERR_MORE_THAN_TWO_Q);
                }

        } catch(Error e) {
            LOGGER.error("During construction:{}",e);
            throw e;
        } catch(RuntimeException re) {
            LOGGER.error("During construction:{}",re);
            throw Error.get(AssocConstants.ERR_CANNOT_CREATE_CHOOSER,re.toString());
        } finally {
            Error.pop();
        }
    }

    /**
     * Rewrites the query expression in its conjunctive normal form,
     * and adds clauses to the end of the clause list
     */
    private void rewriteQuery(FieldTreeNode root,
                              QueryExpression q,
                              List<Conjunct> clauseList,
                              QueryPlan qplan) {
        QueryExpression cnf=qrewriter.rewrite(q);
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
     * Recursively iterate all associations, and assign queries to
     * nodes and edges.
     */
    private void iterateReferences(CompositeMetadata root,List<Conjunct> unassignedClauses) {
        LOGGER.debug("Iterating references to collect clauses");
        Set<Path> childrenNames=root.getChildNames();
        for(Path childName:childrenNames) {
            ResolvedReferenceField rrf=root.getChildReference(childName);
            ReferenceField ref=rrf.getReferenceField();
            if(ref.getQuery()!=null) {
                LOGGER.debug("Association query:{} absQuery:{}",ref.getQuery(),rrf.getAbsQuery());
                List<Conjunct> refQueryClauses=new ArrayList<>();
                rewriteQuery(rrf.getElement(),rrf.getAbsQuery(),refQueryClauses,qplan);
                LOGGER.debug("Association query clauses:{}",refQueryClauses);
                assignQueriesToPlanNodesAndEdges(refQueryClauses,unassignedClauses);
            }
            iterateReferences(rrf.getReferencedMetadata(),unassignedClauses);
        }
    }

    
    /**
     * Assigns queries that refer to only one entity to the query plan
     * nodes. These are fixed assignments, and don't change from one
     * query plan to the other
     */
    private void assignQueriesToPlanNodesAndEdges(List<Conjunct> queries,List<Conjunct> unassigned) {
        LOGGER.debug("Assigning queries to query plan nodes and edges");
        for(Conjunct c:queries) {
            List<QueryPlanNode> nodes=c.getReferredNodes();
            LOGGER.debug("Conjunct {}",c);
            switch(nodes.size()) {
            case 1:
                // All query clauses that depend on fields from a single
                // entity can be assigned to a query plan node. If clauses
                // depend on multiple entities, their assignments may change
                // based on the query plan.
                LOGGER.debug("Conjunct has one entity");
                nodes.get(0).getData().getConjuncts().add(c);
                break;

            case 2:
                // There are two or more entities referred to in the conjunct
                // This clause can be associated with an edge
                QueryPlanNode node1=nodes.get(0);
                QueryPlanNode node2=nodes.get(1);
                if(qplan.isUndirectedConnected(node1,node2)) {
                    LOGGER.debug("Conjunct is assigned to an edge");
                    QueryPlanData qd=qplan.getEdgeData(node1,node2);
                    if(qd==null)
                        qplan.setEdgeData(node1,node2,qd=qplan.newData());
                    qd.getConjuncts().add(c);
                    break;
                }
                // No break, falls through to default
            default:
                
                // Query clause cannot be assigned to a node or edge,
                // put it into the unnasigned list
                LOGGER.debug("Conjunct is unassigned");
                unassigned.add(c);
                break;
            }
        }
    }

    /**
     * Return the root metadata
     */
    public CompositeMetadata getMetadata() {
        return compositeMetadata;
    }

    /**
     * Return the query expression coming from the request
     */
    public QueryExpression getRequestQuery() {
        return requestQuery;
    }   
    
    /**
     * Returns the query plan that's currently chosen
     */
    public QueryPlan getQueryPlan() {
        return qplan;
    }

    /**
     * Returns the best plan chosen so far
     */
    public QueryPlan getBestPlan() {
        return bestPlan;
    }

    /**
     * Resets the query chooser to a state where it can start evaluating the query plans again
     */
    public void reset() {
        bestPlan=null;
        bestPlanScore=null;
        
        qplanIterator.reset(qplan);
        scorer.reset(this);

        bestPlan=qplan.deepCopy();
        bestPlanScore=scorer.score(bestPlan);
        LOGGER.debug("Storing initial plan as the best plan:{}",bestPlan);
        
    }

    /**
     * Advances to the next possible query plan, and scores it. Keeps
     * a copy if the new query plan is better than the current known
     * best
     *
     * @return <code>true</code> if there is more query plans to iterate
     */
    public boolean next() {
        if(qplanIterator.next()) {
            LOGGER.debug("Scoring plan {}",qplan);
            Comparable score=scorer.score(qplan);
            if(score.compareTo(bestPlanScore)<0) {
                LOGGER.debug("Score is better, storing this plan");
                bestPlan=qplan.deepCopy();
                bestPlanScore=score;
                LOGGER.debug("Stored plan:{}",bestPlan);
            }
            return true;
        } else
            return false;
    }

    /**
     * Runs the query plan chooser loop, and returns the best plan
     */
    public QueryPlan choose() {
        reset();
        while(next());
        return bestPlan;
    }
}
