
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
package com.redhat.lightblue.mediator;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldBinding;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.QueryInContext;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanData;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Tuples;

public class CompositeFindImpl implements Finder {

    private static final Logger LOGGER=LoggerFactory.getLogger(CompositeFindImpl.class);

    private final CompositeMetadata root;
    private final QueryPlan qplan;

    private final QueryPlanNode[] sources;
    

    /**
     * Represents a document in a data graph node. It contains the
     * document, and its children and parents
     */
    private static final class DataGraphDoc {
        final DocCtx doc;
        final List<DataGraphDoc> parents=new ArrayList<DataGraphDoc>();
        final List<DataGraphDoc> children=new ArrayList<DataGraphDoc>();

        public DataGraphDoc(DocCtx doc) {
            this.doc=doc;
        }
    }
        
    /**
     * Stores all information about an edge of the query plan. These
     * are destination node, edge queries, and binding information. 
     */
    private static final class Edge {
        final QueryPlanNode sourceNode;
        // The destination node
        final QueryPlanNode destNode;
        // The conjunts associated with this edge
        final List<Conjunct> edgeClauses;
        // The query expression built from the conjuncts
        final QueryExpression edgeQuery;
        // The bound query expression
        final QueryExpression boundEdgeQuery;
        // Field bindings in boundEdgeQuery
        final List<FieldBinding> bindings=new ArrayList<FieldBinding>();

        public Edge(QueryPlanNode sourceNode,
                    QueryPlanNode destNode,
                    List<Conjunct> edgeClauses) {
            LOGGER.debug("Processing edge to {}",destNode);
            this.sourceNode=sourceNode;
            this.destNode=destNode;
            this.edgeClauses=edgeClauses;
            if(edgeClauses!=null&&!edgeClauses.isEmpty()) {
                if(edgeClauses.size()==1) {
                    edgeQuery=edgeClauses.get(0).getClause();
                } else {
                    List<QueryExpression> clauses=new ArrayList<>(edgeClauses.size());
                    for(Conjunct c:edgeClauses)
                        clauses.add(c.getClause());
                    edgeQuery=new NaryLogicalExpression(NaryLogicalOperator._and,clauses);
                }

                LOGGER.debug("Edge query:{}",edgeQuery);
                // Bind edge query fields
                List<QueryInContext> bindable=edgeQuery.getBindableClauses();
                LOGGER.debug("Bindable clauses:{}",bindable);
                
                LOGGER.debug("Building bind request");
                Set<Path> bindRequest=new HashSet<Path>();
                for(QueryInContext qic:bindable) {
                    FieldComparisonExpression fce=(FieldComparisonExpression)qic.getQuery();
                    // Find the field that refers to a node before
                    // the destination
                    Path lfield=new Path(qic.getContext(),fce.getField());
                    QueryPlanNode lfieldNode=null;
                    for(Conjunct c:edgeClauses) {
                        lfieldNode=c.getFieldNode(lfield);
                        if(lfieldNode!=null)
                            break;
                    }
                    Path rfield=new Path(qic.getContext(),fce.getRfield());
                    QueryPlanNode rfieldNode=null;
                    for(Conjunct c:edgeClauses) {
                        rfieldNode=c.getFieldNode(rfield);
                        if(rfieldNode!=null)
                            break;
                    }
                    // If lfieldNode points to the destination node,
                    // rfieldNode points to an ancestor, or vice versa
                    if(lfieldNode==destNode) {
                        bindRequest.add(rfield);
                    } else {
                        bindRequest.add(lfield);
                    }
                }
                LOGGER.debug("Bind fields:{}",bindRequest);
                boundEdgeQuery=edgeQuery.bind(bindings,bindRequest);
                LOGGER.debug("Bound query:{}",boundEdgeQuery);
                
            } else {
                edgeQuery=null;
                boundEdgeQuery=null;
            }
        }

        /**
         * Adds this edge to the incoming/outgoing edges list of the
         * dest/source node Execution object
         */
        public void tie() {
            sourceNode.getProperty(Execution.class).outgoingEdges.add(this);
            destNode.getProperty(Execution.class).incomingEdges.add(this);
        }

        public void refreshBinding(DataGraphDoc doc) {
            for(FieldBinding binding:bindings) {
                Path field=binding.getField();
                LOGGER.debug("Binding {} for node between {} and {}",field,sourceNode.getName(),destNode.getName());
                binding.getValue().setValue(doc.doc.getOutputDocument().get(field));
            }
        }

        public String toString() {
            return sourceNode+"->"+destNode;
        }
    }

    /**
     * Encapsulates executions of a query plan node. Contains the
     * operation context that's constructed based on the top-level
     * operation context, and reused for subsequend executions of this
     * node, and the data retrieved at each execution.
     */
    private static final class Execution {

        private final QueryPlanNode node;
        private final List<Edge> outgoingEdges=new ArrayList();
        private final List<Edge> incomingEdges=new ArrayList();
        private final List<DataGraphDoc> docs=new ArrayList<>();
        private final List<Error> errors=new ArrayList<>();
        private final Finder finder;

        public Execution(QueryPlanNode node,
                         Factory factory) {
            this.node=node;
            this.finder=new SimpleFindImpl(node.getMetadata(),factory);
        }

        public void initEdges(QueryPlan qplan) {
            QueryPlanNode[] destNodes=node.getDestinations();
            for(QueryPlanNode d:destNodes) {
                List<Conjunct> clauses;
                QueryPlanData edgeData=qplan.getEdgeData(node,d);
                if(edgeData!=null)
                    clauses=edgeData.getConjuncts();
                else
                    clauses=null;
                new Edge(node,d,clauses).tie();
            }

        }

        public void execute(OperationContext ctx,
                            CRUDFindRequest req) {
            LOGGER.debug("execute {}: init", node.getName());

            // Create a query using the node query and the incoming edge queries
            List<QueryExpression> clauses=new ArrayList<>();
            for(Edge e:incomingEdges)
                if(e.boundEdgeQuery!=null)
                    clauses.add(e.boundEdgeQuery);
            clauses.add(req.getQuery());
            QueryExpression nodeQuery;
            if(clauses.size()==1)
                nodeQuery=clauses.get(0);
            else if(clauses.size()>1)
                nodeQuery=new NaryLogicalExpression(NaryLogicalOperator._and,clauses);
            else
                nodeQuery=null;
            LOGGER.debug("execute {}: node query={}",node.getName(),nodeQuery);


            CRUDFindRequest findRequest=new CRUDFindRequest();
            findRequest.setQuery(nodeQuery);
            findRequest.setProjection(FieldProjection.ALL);

            // Only if this is the root entity, setting sort and ranges makes sense
            if(node.getMetadata().getParent()==null) {
                findRequest.setSort(req.getSort());
                findRequest.setFrom(req.getFrom());
                findRequest.setTo(req.getTo());
            } else {

                // TODO: set findRequest sort from resolved reference SORT

            }

            if(incomingEdges.isEmpty()) {
                OperationContext nodeCtx=ctx.getDerivedOperationContext(findRequest);
                LOGGER.debug("execute {}: executing search", node.getName());
                CRUDFindResponse response=finder.find(nodeCtx,req);
                LOGGER.debug("execute {}: storing documents", node.getName());
               
                for(DocCtx doc:nodeCtx.getDocuments()) {
                    DataGraphDoc dtd=new DataGraphDoc(doc);
                    errors.addAll(nodeCtx.getErrors());
                }
                
            } else {
                // We will evaluate this node for every possible combination of parent docs
                
                Tuples<DataGraphDoc> tuples=new Tuples<DataGraphDoc>();
                for(Edge e:incomingEdges) {
                    Execution parentEx=e.sourceNode.getProperty(Execution.class);
                    tuples.add(parentEx.docs);
                }
                
                // Iterate n-tuples
                for(Iterator<List<DataGraphDoc>> tupleItr=tuples.tuples();tupleItr.hasNext();) {
                    
                    List<DataGraphDoc> tuple=tupleItr.next();
                    // Tuple elements are ordered the same way as the
                    // incoming edges. tuple[i] is from incomingEdge[i]
                    
                    // Re-evaluate bindings
                    LOGGER.debug("execute {}: evaluating bindings",node.getName());
                    for(int i=0;i<incomingEdges.size();i++) {
                        DataGraphDoc parentDoc=tuple.get(i);
                        Edge incomingEdge=incomingEdges.get(i);
                        incomingEdge.refreshBinding(parentDoc);
                    }
                    
                    OperationContext nodeCtx=ctx.getDerivedOperationContext(findRequest);
                    LOGGER.debug("execute {}: executing search with query", node.getName(),findRequest.getQuery());
                    CRUDFindResponse response=finder.find(nodeCtx,req);
                    LOGGER.debug("execute {}: storing documents", node.getName());
                    
                    for(DocCtx doc:nodeCtx.getDocuments()) {
                        DataGraphDoc dtd=new DataGraphDoc(doc);
                        dtd.parents.addAll(tuple);
                        for(DataGraphDoc x:tuple)
                            x.children.add(dtd);
                        errors.addAll(nodeCtx.getErrors());
                    }
                }
            }
            
            LOGGER.debug("execute {}: complete",node.getName());
        }
    }

    public CompositeFindImpl(CompositeMetadata md,
                             QueryPlan qplan,
                             Factory factory) {
        this.root=md;
        this.qplan=qplan;
        
        sources=qplan.getSources();

        // We need two separate for loops below. First associates an
        // Execution to each query plan node. Second adds the edges
        // between those execution data. Setting up the edges requires
        // all execution information readily available.

        //  Setup execution data for each node
        for(QueryPlanNode x:qplan.getAllNodes())
            x.setProperty(Execution.class,new Execution(x,factory));
        
        // setup edges between execution data
        for(QueryPlanNode x:qplan.getAllNodes()) {
            x.getProperty(Execution.class).initEdges(qplan);
        }
        
    }

    /**
     * Initialization associated Execution to every query plan
     * node. The operation starts by evaluating source nodes, and
     * moves on by going to the destination nodes.
     */
    @Override
    public CRUDFindResponse find(OperationContext ctx,
                                 CRUDFindRequest req) {
        LOGGER.debug("Composite find: start");
        // At this stage, we have Execution objects assigned to query plan nodes

        // Put the executions in order
        QueryPlanNode[] nodeOrdering=qplan.getBreadthFirstNodeOrdering();
        // Execute nodes
        for(QueryPlanNode node:nodeOrdering) {
            LOGGER.debug("Composite find: {}",node.getName());
            Execution exec=node.getProperty(Execution.class);
            exec.execute(ctx,req);
        }

        LOGGER.debug("Composite find: end");
        return null;
    }

}
