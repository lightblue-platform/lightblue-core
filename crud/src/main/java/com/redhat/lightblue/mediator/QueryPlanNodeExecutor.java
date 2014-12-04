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
import java.util.Map;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanDoc;
import com.redhat.lightblue.assoc.ResolvedFieldBinding;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.metadata.DocIdExtractor;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.RelativeRewriteIterator;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Tuples;

public class QueryPlanNodeExecutor {

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryPlanNodeExecutor.class);
       
    private final QueryPlanNode node;
    private final List<QueryPlanNodeExecutor> sources=new ArrayList<>();
    private final DocIdExtractor docIdx;
    private final Finder finder;
    private final CompositeMetadata root;

    private final List<ResolvedFieldBinding> sourceBindings=new ArrayList<>();
    private QueryExpression runExpression;

    private final Map<DocId,JsonDoc> documentCache;

    private final ResolvedReferenceField resolvedReference;

    private Long fromIndex;
    private Long toIndex;

    private List<QueryPlanDoc> docs=new ArrayList<>();

    public QueryPlanNodeExecutor(QueryPlanNode node,
                                 Factory factory,
                                 CompositeMetadata root,
                                 Map<DocId,JsonDoc> documentCache) {
        this.node=node;
        this.finder=new SimpleFindImpl(node.getMetadata(),factory);
        LOGGER.debug("Creating finder for {} for node {}",node.getMetadata().getName(),node.getName());
        docIdx=new DocIdExtractor(node.getMetadata());
        this.root=root;
        this.documentCache=documentCache;

        if(node.getMetadata().getParent()!=null) {
            resolvedReference=root.getResolvedReferenceOfField(node.getMetadata().getEntityPath());
        } else {
            resolvedReference=null;
        }
        LOGGER.debug("ctor {}: resolved reference={}",node.getName(),resolvedReference);
    }

    public void setRange(Long from,Long to) {
        if(node.getMetadata().getParent()==null) {
            fromIndex=from;
            toIndex=to;
        } else
            throw new UnsupportedOperationException("Can set range for root node only");
    }

    public void init(QueryPlan qplan) {
        QueryPlanNode[] sourceNodes=node.getSources();
        for(QueryPlanNode s:sourceNodes)
            sources.add(s.getProperty(QueryPlanNodeExecutor.class));
        List<QueryExpression> queryClauses=new ArrayList<>();

        // Rewrite node conjuncts relative to this node
        List<Conjunct> clauses=node.getData().getConjuncts();
        if(clauses!=null) {
            RelativeRewriteIterator rritr=new RelativeRewriteIterator(new Path(node.getMetadata().getEntityPath(),
                                                                               Path.ANYPATH));
            for(Conjunct c:clauses) {
                if(node.getMetadata().getParent()==null) {
                    queryClauses.add(c.getClause());
                } else {
                    queryClauses.add(rritr.iterate(c.getClause()));
                }
            }
        }

        // Rewrite source edge conjuncts relative to this node, and keep binding information
        for(QueryPlanNode d:sourceNodes) {
            QueryPlanData edgeData=qplan.getEdgeData(node,d);
            if(edgeData!=null) {
                List<Conjunct> edgeClauses = edgeData.getConjuncts();
                if (edgeClauses != null && !edgeClauses.isEmpty()) {
                    ResolvedFieldBinding.BindResult result = ResolvedFieldBinding.bind(edgeClauses, node, root);
                    queryClauses.add(result.getRelativeQuery());
                    sourceBindings.addAll(result.getBindings());
                }
            }
        }
        
        if(queryClauses.size()==1) {
            runExpression=queryClauses.get(0);
        } else if(queryClauses.size()>0) {
            runExpression=new NaryLogicalExpression(NaryLogicalOperator._and,queryClauses);
        } else {
            runExpression=null;
        }
        LOGGER.debug("Node expression: {}",runExpression);
    }


    public void execute(OperationContext ctx,
                        Sort sort) {
        LOGGER.debug("execute {}: start",node.getName());
        
        CRUDFindRequest findRequest=new CRUDFindRequest();
        findRequest.setQuery(runExpression);
        // TODO: Project only those fields we need
        findRequest.setProjection(FieldProjection.ALL);

        if(sort!=null) {
            findRequest.setSort(sort);
        } else if(resolvedReference!=null) {
            findRequest.setSort(resolvedReference.getReferenceField().getSort());
        }

        findRequest.setFrom(fromIndex);
        findRequest.setTo(toIndex);
        LOGGER.debug("execute {}: findRequest.query={}, projection={}, sort={}", node.getName(),
                     findRequest.getQuery(),findRequest.getProjection(),findRequest.getSort());

        if(sources.isEmpty()) {
            execute(ctx,findRequest,null);
        } else {
            // We will evaluate this node for every possible combination of parent docs
                
            Tuples<QueryPlanDoc> tuples=new Tuples<>();
            for(QueryPlanNodeExecutor source:sources) {
                tuples.add(source.docs);
            }
           
            // Iterate n-tuples
            for(Iterator<List<QueryPlanDoc>> tupleItr=tuples.tuples();tupleItr.hasNext();) {
                List<QueryPlanDoc> tuple=tupleItr.next();
                LOGGER.debug("Processing an {}-tuple",tuple.size());
                // Tuple elements are ordered the same way as the
                // sources. tuple[i] is from sources[i]
                
                LOGGER.debug("execute {}: refreshing bindings",node.getName());
                Iterator<QueryPlanDoc> qpdItr = tuple.iterator();
                // TODO can this for loop change to a while(qpdItr.hasNext())?
                for(int i=0;i<sources.size();i++) {
                    QueryPlanDoc parentDoc=qpdItr.next();
                    ResolvedFieldBinding.refresh(sourceBindings,parentDoc);
                }
               execute(ctx,findRequest,tuple);
            }
       }
    }

    public List<QueryPlanDoc> getDocs() {
        return docs;
    }

    public void setDocs(List<QueryPlanDoc> docs) {
        this.docs=docs;
    }

    public QueryPlanNode getNode() {
        return node;
    }

    private void execute(OperationContext ctx,
                         CRUDFindRequest findRequest,
                         List<QueryPlanDoc> parents) {
        OperationContext nodeCtx=ctx.getDerivedOperationContext(node.getMetadata().getName(),findRequest);
        LOGGER.debug("execute {}: entity={}, findRequest.query={}, projection={}, sort={}", node.getName(),
                     nodeCtx.getEntityName(),
                     findRequest.getQuery(),findRequest.getProjection(),findRequest.getSort());
        // note the response is not used, but find method changes the supplied context.
        CRUDFindResponse response=finder.find(nodeCtx,findRequest);
        LOGGER.debug("execute {}: storing documents", node.getName());
        for(DocCtx doc:nodeCtx.getDocuments()) {
            DocId id=docIdx.getDocId(doc.getOutputDocument());
            if(documentCache!=null) {
                JsonDoc jdoc=documentCache.get(id);
                if(jdoc==null) {
                    jdoc=doc.getOutputDocument();
                    documentCache.put(id,jdoc);
                }
            }
            QueryPlanDoc qplanDoc=new QueryPlanDoc(doc.getOutputDocument(),id,node);
            docs.add(qplanDoc);
        }
        if(parents!=null) {
            for(QueryPlanDoc parent:parents) {
                parent.addChildren(node,docs);
            }
        }                      
    }
}
