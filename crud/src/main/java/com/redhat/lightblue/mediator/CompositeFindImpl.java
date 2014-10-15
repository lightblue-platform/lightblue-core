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
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.OperationStatus;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldBinding;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.QueryInContext;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.RelativeRewriteIterator;

import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanChooser;

import com.redhat.lightblue.assoc.scorers.SimpleScorer;
import com.redhat.lightblue.assoc.iterators.First;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.DocIdExtractor;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.Type;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Tuples;
import com.redhat.lightblue.util.JsonDoc;

public class CompositeFindImpl implements Finder {

    private static final Logger LOGGER=LoggerFactory.getLogger(CompositeFindImpl.class);

    private final CompositeMetadata root;
    private final QueryPlan qplan;
    private final Factory factory;

    private final QueryPlanNode[] sources;

    private final Map<DocId,JsonDoc> documentCache=new HashMap<>();
    
    private final List<Error> errors=new ArrayList<>();

    /**
     * Represents a document in a data graph node. It contains the
     * document, and its children and parents
     */
    private static final class DataGraphDoc {
        
        private final JsonDoc doc;
        private final DocId id;
        private final List<Error> errors=new ArrayList<>();
        private final QueryPlanNode node;
        // Parent documents of this doc. Multiple parents are
        // possible, one parent document for each incoming node
        private final List<DataGraphDoc> parents=new ArrayList<>();
        // Child documents of this doc.
        private final Map<QueryPlanNode,List<DataGraphDoc>> children=new HashMap<>();
        
        public DataGraphDoc(JsonDoc doc,
                            List<Error> errors,
                            DocId id,
                            QueryPlanNode node) {
            this.doc=doc;
            if(errors!=null)
                this.errors.addAll(errors);
            this.id=id;
            this.node=node;
        }

        public void addChildren(QueryPlanNode node,List<DataGraphDoc> list) {
            List<DataGraphDoc> clist=children.get(node);
            if(clist==null) {
                children.put(node,list);
            } else {
                clist.addAll(list);
            }
        }

        public void addChild(QueryPlanNode node,DataGraphDoc doc) {
            List<DataGraphDoc> clist=children.get(node);
            if(clist==null) {
                children.put(node,clist=new ArrayList<>());
            } 
            clist.add(doc);
        }

        public List<DataGraphDoc> getChildren(QueryPlanNode node) {
            return children.get(node);
        }
    }

    private static final class BindingAndType {
        private final FieldBinding binding;
        private final Type t;

        public BindingAndType(FieldBinding b,Type t) {
            this.binding=b;
            this.t=t;
        }
    }

    /**
     * Stores all information about an edge of the query plan. These
     * are destination node, edge queries, and binding information. 
     */
    private final class Edge {
        final QueryPlanNode sourceNode;
        // The destination node
        final QueryPlanNode destNode;
        // The conjunts associated with this edge
        final List<Conjunct> edgeClauses;
        // The query expression built from the conjuncts
        final QueryExpression edgeQuery;
        // The bound query expression
        final QueryExpression boundEdgeQuery;
        // clone of boundEdgeQuery that can be used to run a search.
        // This is necessary, because boundEdgeQuery contains fields relative to the root entity
        // What we need here is queries whose fields are relative to the root of the entity for the destNode
        // The trick here is that when you bind values to boundEdgeQuery, runExpression values are also bound.
        final QueryExpression runExpression;
        // Field bindings in boundEdgeQuery
        final List<BindingAndType> bindings=new ArrayList<>();

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
                    LOGGER.debug("lfield={}, rfield={}",lfieldNode==null?null:lfieldNode.getName(),
                                 rfieldNode==null?null:rfieldNode.getName());
                    // If lfieldNode points to the destination node,
                    // rfieldNode points to an ancestor, or vice versa
                    if(lfieldNode.getName().equals(destNode.getName())) {
                        bindRequest.add(rfield);
                    } else {
                        bindRequest.add(lfield);
                    }
                }
                LOGGER.debug("Bind fields:{}",bindRequest);
                List<FieldBinding> fb=new ArrayList<>();
                boundEdgeQuery=edgeQuery.bind(fb,bindRequest);
                for(FieldBinding b:fb) {
                    bindings.add(new BindingAndType(b,root.resolve(b.getField()).getType()));
                }
                LOGGER.debug("Bound query:{}",boundEdgeQuery);
                
                runExpression=new RelativeRewriteIterator(new Path(destNode.getMetadata().getEntityPath(),
                                                                   Path.ANYPATH)).iterate(boundEdgeQuery);
                LOGGER.debug("Run expression:{}",runExpression);
                
            } else {
                edgeQuery=null;
                boundEdgeQuery=null;
                runExpression=null;
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
            for(BindingAndType binding:bindings) {
                Path field=binding.binding.getField();
                LOGGER.debug("Binding {} for node between {} and {}",field,sourceNode.getName(),destNode.getName());
                JsonNode node=doc.doc.get(field);
                if(node==null)
                    binding.binding.getValue().setValue(null);
                else
                    binding.binding.getValue().setValue(binding.t.fromJson(node));
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
    private final class Execution {

        private final QueryPlanNode node;
        private final List<Edge> outgoingEdges=new ArrayList();
        private final List<Edge> incomingEdges=new ArrayList();
        private final List<Error> errors;
        private final DocIdExtractor docIdx;
        private final Finder finder;
        private List<DataGraphDoc> docs;

        public Execution(QueryPlanNode node,
                         Factory factory,
                         List<Error> errors) {
            docs=new ArrayList<>();
            this.node=node;
            this.finder=new SimpleFindImpl(node.getMetadata(),factory);
            this.errors=errors;
            docIdx=new DocIdExtractor(node.getMetadata());
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
                    clauses.add(e.runExpression);
            if(req.getQuery()!=null)
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
                OperationContext nodeCtx=ctx.getDerivedOperationContext(node.getMetadata().getName(),findRequest);
                LOGGER.debug("execute {}: executing search", node.getName());
                CRUDFindResponse response=finder.find(nodeCtx,findRequest);
                LOGGER.debug("execute {}: storing documents", node.getName());
               
                for(DocCtx doc:nodeCtx.getDocuments()) {
                    DocId id=docIdx.getDocId(doc.getOutputDocument());
                    JsonDoc jdoc=documentCache.get(id);
                    if(jdoc==null) {
                        jdoc=doc.getOutputDocument();
                        documentCache.put(id,jdoc);
                    }
                    DataGraphDoc dtd=new DataGraphDoc(doc.getOutputDocument(),
                                                      doc.getErrors(),
                                                      id,
                                                      node);
                    docs.add(dtd);
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
                    LOGGER.debug("Processing an {}-tuple",tuple.size());
                    // Tuple elements are ordered the same way as the
                    // incoming edges. tuple[i] is from incomingEdge[i]
                    
                    // Re-evaluate bindings
                    LOGGER.debug("execute {}: evaluating bindings",node.getName());
                    for(int i=0;i<incomingEdges.size();i++) {
                        DataGraphDoc parentDoc=tuple.get(i);
                        Edge incomingEdge=incomingEdges.get(i);
                        incomingEdge.refreshBinding(parentDoc);
                    }
                    
                    OperationContext nodeCtx=ctx.getDerivedOperationContext(node.getMetadata().getName(),findRequest);
                    LOGGER.debug("execute {}: executing search with query {} for entity {}", 
                                 node.getName(),findRequest.getQuery(),nodeCtx.getEntityName());
                    CRUDFindResponse response=finder.find(nodeCtx,findRequest);
                    LOGGER.debug("execute {}: storing documents", node.getName());
                    
                    for(DocCtx doc:nodeCtx.getDocuments()) {
                        DocId id=docIdx.getDocId(doc.getOutputDocument());
                        JsonDoc jdoc=documentCache.get(id);
                        if(jdoc==null) {
                            jdoc=doc.getOutputDocument();
                            documentCache.put(id,jdoc);
                        }
                        DataGraphDoc dtd=new DataGraphDoc(doc.getOutputDocument(),
                                                          doc.getErrors(),
                                                          id,
                                                          node);
                        docs.add(dtd);
                        dtd.parents.addAll(tuple);
                        for(DataGraphDoc x:tuple)
                            x.addChild(node,dtd);
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
        this.factory=factory;

        sources=qplan.getSources();
        init(qplan,factory,errors);
    }


    private void init(QueryPlan q,
                      Factory factory,
                      List<Error> errors) {
        // We need two separate for loops below. First associates an
        // Execution to each query plan node. Second adds the edges
        // between those execution data. Setting up the edges requires
        // all execution information readily available.
        
        //  Setup execution data for each node
        for(QueryPlanNode x:q.getAllNodes())
            x.setProperty(Execution.class,new Execution(x,factory,errors));
        
        // setup edges between execution data
        for(QueryPlanNode x:q.getAllNodes()) {
            x.getProperty(Execution.class).initEdges(q);
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
        // Execute nodes. Only the nodes up to and including the root
        // node needs to be executed. The query choose makes sure that
        // any query plan descendands of the root node don't have any
        // queries associated with them, so they will be retrieved
        // only using edge conditions
        QueryPlanNode rootNode=null;
        for(QueryPlanNode node:nodeOrdering) {
            LOGGER.debug("Composite find: {}",node.getName());
            Execution exec=node.getProperty(Execution.class);
            exec.execute(ctx,req);
            if(node.getMetadata().getParent()==null) {
                // Reached the root node. Terminate execution, and build documents
                rootNode=node;
                break;
            }
        }

        LOGGER.debug("Composite find: retrieval of result set is complete, now building documents");
        
        CRUDFindResponse response=new CRUDFindResponse();
        List<DocCtx> resultDocuments=retrieveDocuments(ctx,rootNode.getProperty(Execution.class));
        response.setSize(resultDocuments.size());
        ctx.setDocuments(resultDocuments);
        ctx.addErrors(errors);
        
        LOGGER.debug("Composite find: end");
        ctx.getHookManager().queueMediatorHooks(ctx);
        ctx.setStatus(OperationStatus.COMPLETE);
        return response;
    }
    
    private List<DocCtx> retrieveDocuments(OperationContext ctx,
                                           Execution rootNode) {
        LOGGER.debug("Retrieving {} documents",rootNode.docs.size());

        // Create a new query plan for retrieval. This one will have
        // the root document at the root.
        QueryPlanChooser chooser=new QueryPlanChooser(root,new First(),new SimpleScorer(),null);
        QueryPlan retrievalQPlan=chooser.choose();
        // This query plan has only one source
        QueryPlanNode retrievalPlanRoot=retrievalQPlan.getSources()[0];
        CompositeFindImpl cfi=new CompositeFindImpl(root,retrievalQPlan,factory);
        // The root node documents are already known
        retrievalPlanRoot.getProperty(Execution.class).docs=rootNode.docs;

        // Now execute rest of the retrieval plan
        QueryPlanNode[] nodeOrdering=qplan.getBreadthFirstNodeOrdering();
        
        CRUDFindRequest req=new CRUDFindRequest();
        for(int i=1;i<nodeOrdering.length;i++) {
            LOGGER.debug("Composite retrieval: {}",nodeOrdering[i].getName());
            Execution exec=nodeOrdering[i].getProperty(Execution.class);
            exec.execute(ctx,req);
        }

        List<DocCtx> ret=new ArrayList<>(rootNode.docs.size());
        for(DataGraphDoc dgd:rootNode.docs) {
            retrieveFragments(dgd,rootNode);
            PredefinedFields.updateArraySizes(factory.getNodeFactory(),dgd.doc);
            DocCtx dctx=new DocCtx(dgd.doc);
            dctx.setOutputDocument(dgd.doc);
            dctx.addErrors(dgd.errors);
            ret.add(dctx);
        }
        
        return ret;
    }

    private void retrieveFragments(DataGraphDoc doc,
                                   Execution execution) {
        // We only process child nodes.
        for(Edge outgoingEdge:execution.outgoingEdges) {
            QueryPlanNode childNode=outgoingEdge.destNode;
            Execution childExecution=childNode.getProperty(Execution.class);
            CompositeMetadata childMd=childNode.getMetadata();
            Path insertInto=childMd.getEntityPath();
            JsonNode insertionNode=doc.doc.get(insertInto);
            if(insertionNode==null)
                doc.doc.modify(insertInto,insertionNode=factory.getNodeFactory().arrayNode(),true);
            List<DataGraphDoc> children=doc.getChildren(childNode);
            for(DataGraphDoc childDoc:children) {
                ((ArrayNode)insertionNode).add(childDoc.doc.getRoot());
                retrieveFragments(doc,childExecution);
            }
        }
    }

}
