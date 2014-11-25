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
import com.redhat.lightblue.assoc.QueryPlanDoc;

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

    private final Map<DocId,JsonDoc> documentCache=new HashMap<>();
    
    private final List<Error> errors=new ArrayList<>();

    public CompositeFindImpl(CompositeMetadata md,
                             QueryPlan qplan,
                             Factory factory) {
        this.root=md;
        this.qplan=qplan;
        this.factory=factory;

        init(qplan,factory);
    }


    private void init(QueryPlan q,
                      Factory factory) {
        // We need two separate for loops below. First associates an
        // QueryPlanNodeExecutor to each query plan node. Second adds
        // the edges between those execution data. Setting up the
        // edges requires all execution information readily available.
        
        //  Setup execution data for each node
        for(QueryPlanNode x:q.getAllNodes())
            x.setProperty(QueryPlanNodeExecutor.class,new QueryPlanNodeExecutor(x,factory,root,documentCache));
        
        // setup edges between execution data
        for(QueryPlanNode x:q.getAllNodes()) {
            x.getProperty(QueryPlanNodeExecutor.class).init(q);
        }
    }

    /**
     * The operation starts by evaluating source nodes, and
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
            QueryPlanNodeExecutor exec=node.getProperty(QueryPlanNodeExecutor.class);
            if(node.getMetadata().getParent()==null) {
                exec.execute(ctx,req.getSort());
                // Reached the root node. Terminate execution, and build documents
                rootNode=node;
                break;
            } else {
                exec.execute(ctx,null);
            }
        }

        LOGGER.debug("Composite find: retrieval of result set is complete, now building documents");

        if (rootNode == null) {
            // not likely, but just in case
            throw new IllegalStateException("No root QueryPlanNode selected to execute query against!");
        }
        
        CRUDFindResponse response=new CRUDFindResponse();
        List<DocCtx> resultDocuments=retrieveDocuments(ctx,rootNode.getProperty(QueryPlanNodeExecutor.class));
        response.setSize(resultDocuments.size());
        ctx.setDocuments(resultDocuments);
        ctx.addErrors(errors);
        
        LOGGER.debug("Composite find: end");
        return response;
    }
    
    private List<DocCtx> retrieveDocuments(OperationContext ctx,
                                           QueryPlanNodeExecutor rootNode) {
        LOGGER.debug("Retrieving {} documents",rootNode.getDocs().size());

        // Create a new query plan for retrieval. This one will have
        // the root document at the root.
        QueryPlanChooser chooser=new QueryPlanChooser(root,new First(),new SimpleScorer(),null);
        QueryPlan retrievalQPlan=chooser.choose();
        // This query plan has only one source
        QueryPlanNode retrievalPlanRoot=retrievalQPlan.getSources()[0];
        CompositeFindImpl cfi=new CompositeFindImpl(root,retrievalQPlan,factory);
        // The root node documents are already known
        retrievalPlanRoot.getProperty(QueryPlanNodeExecutor.class).setDocs(rootNode.getDocs());

        // Now execute rest of the retrieval plan
        QueryPlanNode[] nodeOrdering=qplan.getBreadthFirstNodeOrdering();
        
        CRUDFindRequest req=new CRUDFindRequest();
        for(int i=0;i<nodeOrdering.length;i++) {
            if(nodeOrdering[i].getMetadata().getParent()!=null) {
                LOGGER.debug("Composite retrieval: {}",nodeOrdering[i].getName());
                QueryPlanNodeExecutor exec=nodeOrdering[i].getProperty(QueryPlanNodeExecutor.class);
                exec.execute(ctx,null);
            }
        }

        List<DocCtx> ret=new ArrayList<>(rootNode.getDocs().size());
        for(QueryPlanDoc dgd:rootNode.getDocs()) {
            retrieveFragments(dgd,rootNode);
            PredefinedFields.updateArraySizes(factory.getNodeFactory(),dgd.getDoc());
            DocCtx dctx=new DocCtx(dgd.getDoc());
            dctx.setOutputDocument(dgd.getDoc());
            ret.add(dctx);
        }
        
        return ret;
    }

    private void retrieveFragments(QueryPlanDoc doc,
                                   QueryPlanNodeExecutor exec) {
        // We only process child nodes.
        QueryPlanNode[] destinations=exec.getNode().getDestinations();
        for(QueryPlanNode childNode:destinations) {
            QueryPlanNodeExecutor childExecutor=childNode.getProperty(QueryPlanNodeExecutor.class);
            CompositeMetadata childMd=childNode.getMetadata();
            Path insertInto=childMd.getEntityPath();
            JsonNode insertionNode=doc.getDoc().get(insertInto);
            if(insertionNode==null)
                doc.getDoc().modify(insertInto,insertionNode=factory.getNodeFactory().arrayNode(),true);
            List<QueryPlanDoc> children=doc.getChildren(childNode);
            if(children!=null) {
                for(QueryPlanDoc childDoc:children) {
                    ((ArrayNode)insertionNode).add(childDoc.getDoc().getRoot());
                    retrieveFragments(doc,childExecutor);
                }
            }
        }
    }

}
