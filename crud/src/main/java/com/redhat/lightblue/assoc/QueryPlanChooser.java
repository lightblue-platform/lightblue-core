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
import java.util.Iterator;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryPlanChooser.class);

    private static final QueryRewriter qrewriter = new QueryRewriter(true);

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
                            QueryExpression requestQuery,
                            Set<CompositeMetadata> filter) {
        LOGGER.debug("QueryPlanChooser.ctor");
        Error.push("QueryPlanChooser");
        try {
            this.compositeMetadata = cmd;
            this.qplanIterator = qpitr;
            this.scorer = scorer;
            qplan = new QueryPlan(compositeMetadata, scorer, filter);
            LOGGER.debug("Initial query plan:{}", qplan);

            this.requestQuery = requestQuery;
            LOGGER.debug("Request query:{}", this.requestQuery);

            // Rewrite  request queries  in  conjunctive  normal form  and
            // assign them to nodes/edges
            if (this.requestQuery != null) {
                List<Conjunct> requestQueryClauses = new ArrayList<>();
                rewriteQuery(this.requestQuery, requestQueryClauses, qplan, null);
                LOGGER.debug("Request query clauses:{}", requestQueryClauses);
                assignQueriesToPlanNodesAndEdges(requestQueryClauses, qplan.getUnassignedClauses(),false);
                LOGGER.debug("Completed assigning request query clauses");
            }

            // Rewrite association queries in conjunctive normal form, and
            // assign them to nodes/edges
            iterateReferences(compositeMetadata, qplan.getUnassignedClauses());

            reset();

        } catch (Error e) {
            LOGGER.error("During construction:{}", e);
            throw e;
        } catch (RuntimeException re) {
            LOGGER.error("During construction:{}", re);
            throw Error.get(AssocConstants.ERR_CANNOT_CREATE_CHOOSER, re.toString());
        } finally {
            Error.pop();
        }
    }

    /**
     * Rewrites the query expression in its conjunctive normal form, and adds
     * clauses to the end of the clause list
     */
    private void rewriteQuery(QueryExpression q,
                              List<Conjunct> clauseList,
                              QueryPlan qplan,
                              ResolvedReferenceField context) {
        Error.push("rewriteQuery");
        AnalyzeQuery analyzer = new AnalyzeQuery(compositeMetadata, context);
        try {
            QueryExpression cnf = qrewriter.rewrite(q);
            LOGGER.debug("Query in conjunctive normal form:{}", cnf);
            if (cnf instanceof NaryLogicalExpression
                    && ((NaryLogicalExpression) cnf).getOp() == NaryLogicalOperator._and) {
                for (QueryExpression clause : ((NaryLogicalExpression) cnf).getQueries()) {
                    analyzer.iterate(clause);
                    clauseList.add(new Conjunct(clause, analyzer.getFieldInfo(), context));
                }
            } else {
                analyzer.iterate(cnf);
                clauseList.add(new Conjunct(cnf, analyzer.getFieldInfo(), context));
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
     * Recursively iterate all associations, and assign queries to nodes and
     * edges.
     */
    private void iterateReferences(CompositeMetadata root, List<Conjunct> unassignedClauses) {
        LOGGER.debug("Iterating references to collect clauses");
        Error.push("iterateReferences");
        try {
            Set<Path> childPaths = root.getChildPaths();
            LOGGER.debug("childPaths={}",childPaths);
            QueryPlanNode sourceNode = qplan.getNode(root);
            for (Path childPath : childPaths) {
                LOGGER.debug("Processing child path={}",childPath);
                ResolvedReferenceField rrf = root.getDescendantReference(childPath);
                if(rrf!=null) {
                    QueryPlanNode destNode = qplan.getNode(rrf.getReferencedMetadata());
                    if(destNode!=null) {
                        QueryPlanData qd = qplan.getEdgeData(sourceNode, destNode);
                        if (qd == null) {
                            qplan.setEdgeData(sourceNode, destNode, qd = qplan.newData());
                        }
                        qd.setReference(rrf);
                        ReferenceField ref = rrf.getReferenceField();
                        if (ref.getQuery() != null) {
                            LOGGER.debug("Association query:{}", ref.getQuery());
                            List<Conjunct> refQueryClauses = new ArrayList<>();
                            rewriteQuery(ref.getQuery(), refQueryClauses, qplan, rrf);
                            LOGGER.debug("Association query clauses:{}", refQueryClauses);
                            assignQueriesToPlanNodesAndEdges(refQueryClauses, unassignedClauses,true);
                        }
                        iterateReferences(rrf.getReferencedMetadata(), unassignedClauses);
                    } // else, this destination entity is excluded in query plan
                } else
                    throw new RuntimeException("Cannot retrieve descendant reference for "+childPath);
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
     * Assigns queries that refer to only one entity to the query plan nodes.
     * These are fixed assignments, and don't change from one query plan to the
     * other
     */
    private void assignQueriesToPlanNodesAndEdges(List<Conjunct> queries,
                                                  List<Conjunct> unassigned,
                                                  boolean relationQuery) {
        Error.push("assignQueriesToPlanNodesAndEdges");
        LOGGER.debug("Assigning queries to query plan nodes and edges");
        try {
            for (Conjunct c : queries) {
                Set<CompositeMetadata> entities = c.getEntities();
                Conjunct.ConjunctType t=c.getConjunctType();
                LOGGER.debug("Conjunct {}:{}", c,t);
                if(t==Conjunct.ConjunctType.value) {
                    // All query clauses that depend on fields from a single
                    // entity can be assigned to a query plan node. If clauses
                    // depend on multiple entities, their assignments may change
                    // based on the query plan.
                    LOGGER.debug("Conjunct has one entity");
                    // Rewrite the query for that node
                    QueryPlanNode node = qplan.getNode(entities.iterator().next());
                    RewriteQuery rw = new RewriteQuery(compositeMetadata, node.getMetadata());
                    QueryExpression q = rw.rewriteQuery(c.getClause(), c.getFieldInfo()).query;
                    AnalyzeQuery analyzer = new AnalyzeQuery(node.getMetadata(), c.getReference());
                    analyzer.iterate(q);
                    node.getData().getConjuncts().add(new Conjunct(q,
                                                                   analyzer.getFieldInfo(),
                                                                   c.getReference()));
                } else {
                    boolean assigned=false;
                    if(t==Conjunct.ConjunctType.relation||
                       (relationQuery&&entities.size()<=2) ) {
                        // There are two entities referred to in the conjunct
                        // This clause can be associated with the edge between those two entities.
                        // If the two entities are not associated, then the conjunct goes into the
                        // unassigned queries list.
                        Iterator<CompositeMetadata> itr = entities.iterator();
                        QueryPlanNode node1 = qplan.getNode(itr.next());
                        QueryPlanNode node2 = qplan.getNode(itr.next());
                        if (qplan.isUndirectedConnected(node1, node2)) {
                            LOGGER.debug("Conjunct is assigned to an edge");
                            QueryPlanData qd = qplan.getEdgeData(node1, node2);
                            if (qd == null) {
                                qplan.setEdgeData(node1, node2, qd = qplan.newData());
                            }
                            qd.getConjuncts().add(c);
                            assigned=true;
                        }
                    }
                    if(!assigned) {
                        // Query clause cannot be assigned to a node or edge,
                        // put it into the unassigned list
                        LOGGER.debug("Conjunct is unassigned");
                        unassigned.add(c);
                    }
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
     * Resets the query chooser to a state where it can start evaluating the
     * query plans again
     */
    public void reset() {
        bestPlan = null;
        bestPlanScore = null;

        qplanIterator.reset(qplan);
        scorer.reset(this);

        bestPlan = qplan.deepCopy();
        bestPlanScore = scorer.score(bestPlan);
        LOGGER.debug("Storing initial plan as the best plan:{}", bestPlan);

    }

    /**
     * Chooses the best query play after scoring all possible plans.
     */
    public QueryPlan choose() {
        while (qplanIterator.next()) {
            LOGGER.debug("Scoring plan {}", qplan);
            Comparable score = scorer.score(qplan);
            if (null != score && score.compareTo(bestPlanScore) < 0) {
                LOGGER.debug("Score is better, storing this plan");
                bestPlan = qplan.deepCopy();
                bestPlanScore = score;
                LOGGER.debug("Stored plan:{}", bestPlan);
            }
        }

        return bestPlan;
    }
}
