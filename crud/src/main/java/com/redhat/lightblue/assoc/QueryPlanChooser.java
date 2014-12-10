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
import java.util.Set;

import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.MetadataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.ReferenceField;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

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
            LOGGER.debug("Request query:{}",this.requestQuery);
            
            // Rewrite  request queries  in  conjunctive  normal form  and
            // assign them to nodes/edges
            if(this.requestQuery!=null) {
                List<Conjunct> requestQueryClauses=new ArrayList<>();
                rewriteQuery(this.requestQuery,requestQueryClauses,qplan);
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
            if (!qplan.getUnassignedClauses().isEmpty()) {
                Conjunct x = qplan.getUnassignedClauses().get(0);
                switch (x.getReferredNodes().size()) {
                    case 2:
                        throw Error.get(AssocConstants.ERR_UNRELATED_ENTITY_Q);
                    default:
                        throw Error.get(AssocConstants.ERR_MORE_THAN_TWO_Q);
                }
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
    private void rewriteQuery(QueryExpression q,
                              List<Conjunct> clauseList,
                              QueryPlan qplan) {
        Error.push("rewriteQuery");
        try {
            QueryExpression cnf = qrewriter.rewrite(q);
            LOGGER.debug("Query in conjunctive normal form:{}", cnf);
            if (cnf instanceof NaryLogicalExpression &&
                    ((NaryLogicalExpression) cnf).getOp() == NaryLogicalOperator._and) {
                for (QueryExpression clause : ((NaryLogicalExpression) cnf).getQueries()) {
                    clauseList.add(new Conjunct(clause, compositeMetadata, qplan));
                }
            } else {
                clauseList.add(new Conjunct(cnf, compositeMetadata, qplan));
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(AssocConstants.ERR_REWRITE, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    /**
     * Recursively iterate all associations, and assign queries to
     * nodes and edges.
     */
    private void iterateReferences(CompositeMetadata root,List<Conjunct> unassignedClauses) {
        LOGGER.debug("Iterating references to collect clauses");
        Error.push("iterateReferences");
        try {
            Set<Path> childPaths = root.getChildPaths();
            for (Path childPath : childPaths) {
                ResolvedReferenceField rrf = root.getChildReference(childPath);
                ReferenceField ref = rrf.getReferenceField();
                if (ref.getQuery() != null) {
                    LOGGER.debug("Association query:{} absQuery:{}", ref.getQuery(), rrf.getAbsQuery());
                    List<Conjunct> refQueryClauses = new ArrayList<>();
                    rewriteQuery(rrf.getAbsQuery(), refQueryClauses, qplan);
                    LOGGER.debug("Association query clauses:{}", refQueryClauses);
                    assignQueriesToPlanNodesAndEdges(refQueryClauses, unassignedClauses);
                }
                iterateReferences(rrf.getReferencedMetadata(), unassignedClauses);
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(AssocConstants.ERR_REWRITE, e.getMessage());
        } finally {
            Error.pop();
        }
    }

    
    /**
     * Assigns queries that refer to only one entity to the query plan
     * nodes. These are fixed assignments, and don't change from one
     * query plan to the other
     */
    private void assignQueriesToPlanNodesAndEdges(List<Conjunct> queries,List<Conjunct> unassigned) {
        Error.push("assignQueriesToPlanNodesAndEdges");
        LOGGER.debug("Assigning queries to query plan nodes and edges");
        try {
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
                    // put it into the unassigned list
                    LOGGER.debug("Conjunct is unassigned");
                    unassigned.add(c);
                    break;
                }
            }
        } catch (Error e) {
            // rethrow lightblue error
            throw e;
        } catch (Exception e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(AssocConstants.ERR_REWRITE, e.getMessage());
        } finally {
            Error.pop();
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
     * Chooses the best query play after scoring all possible plans.
     */
    public QueryPlan choose() {
        while(qplanIterator.next()) {
            LOGGER.debug("Scoring plan {}",qplan);
            Comparable score=scorer.score(qplan);
            if(null!=score&&score.compareTo(bestPlanScore)<0) {
                LOGGER.debug("Score is better, storing this plan");
                bestPlan=qplan.deepCopy();
                bestPlanScore=score;
                LOGGER.debug("Stored plan:{}",bestPlan);
            }
        }

        return bestPlan;
    }
}
