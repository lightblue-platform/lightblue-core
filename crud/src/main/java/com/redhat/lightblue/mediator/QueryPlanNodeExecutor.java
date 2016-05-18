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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.ResultDoc;
import com.redhat.lightblue.assoc.ChildDocReference;
import com.redhat.lightblue.assoc.DocReference;
import com.redhat.lightblue.assoc.ParentDocReference;
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
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.util.Tuples;

public class QueryPlanNodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryPlanNodeExecutor.class);

    private final QueryPlanNode node;
    private final List<QueryPlanNodeExecutor> sources = new ArrayList<>();
    private final DocIdExtractor docIdx;
    private final Finder finder;
    private final CompositeMetadata root;
    private QueryPlan qplan;

    private final List<ResolvedFieldBinding> sourceBindings = new ArrayList<>();
    private QueryExpression runExpression;

    private final ResolvedReferenceField resolvedReference;

    private Long fromIndex;
    private Long toIndex;

    private List<ResultDoc> docs = new ArrayList<>();

    public QueryPlanNodeExecutor(QueryPlanNode node,
                                 Factory factory,
                                 CompositeMetadata root) {
        this.node = node;
        this.finder = new SimpleFindImpl(node.getMetadata(), factory);
        LOGGER.debug("Creating finder for {} for node {}", node.getMetadata().getName(), node.getName());
        docIdx = new DocIdExtractor(node.getMetadata());
        LOGGER.debug("ID extractor:{}", docIdx);
        this.root = root;

        if (node.getMetadata().getParent() != null) {
            resolvedReference = root.getResolvedReferenceOfField(node.getMetadata().getEntityPath());
        } else {
            resolvedReference = null;
        }
        LOGGER.debug("ctor {}: resolved reference={}", node.getName(), resolvedReference);
    }

    public void setRange(Long from, Long to) {
        if (node.getMetadata().getParent() == null) {
            fromIndex = from;
            toIndex = to;
        } else {
            throw new UnsupportedOperationException("Can set range for root node only");
        }
    }

    public void init(QueryPlan qplan) {
        this.qplan = qplan;
        QueryPlanNode[] sourceNodes = node.getSources();
        for (QueryPlanNode s : sourceNodes) {
            sources.add(s.getProperty(QueryPlanNodeExecutor.class));
        }

        List<QueryExpression> queryClauses = new ArrayList<>();
        List<Conjunct> clauses = node.getData().getConjuncts();
        if (clauses != null) {
            for (Conjunct c : clauses) {
                QueryExpression relative = new ResolvedFieldBinding.RelativeRewriter(c, root, node.getMetadata()).iterate(c.getClause());
                queryClauses.add(relative);
                LOGGER.debug("Conjunct: {}, relativeq:{}", c, relative);
            }
        }

        // Rewrite source edge conjuncts relative to this node, and keep binding information
        for (QueryPlanNode d : sourceNodes) {
            QueryPlanData edgeData = qplan.getEdgeData(node, d);
            if (edgeData != null) {
                List<Conjunct> edgeClauses = edgeData.getConjuncts();
                if (edgeClauses != null && !edgeClauses.isEmpty()) {
                    ResolvedFieldBinding.BindResult result = ResolvedFieldBinding.bind(edgeClauses, node, root);
                    queryClauses.add(result.getRelativeQuery());
                    sourceBindings.addAll(result.getBindings());
                }
            }
        }

        if (queryClauses.size() == 1) {
            runExpression = queryClauses.get(0);
        } else if (queryClauses.size() > 0) {
            runExpression = new NaryLogicalExpression(NaryLogicalOperator._and, queryClauses);
        } else {
            runExpression = null;
        }
        LOGGER.debug("Node expression for {}: {}", node.getName(), runExpression);
    }

    /**
     * Returns true if this query plan node executor produces documents that are
     * contained in 'otherNode' If otherNode is a source of the current node,
     * returning 'true' from this function means that the parent node documents
     * are to be contained in this node documents
     *
     */
    private boolean isParentOfThis(QueryPlanNode otherNode) {
        return node.getMetadata().getParent() == otherNode.getMetadata();
    }

    public CRUDFindResponse execute(OperationContext ctx,
                                    Sort sort) {
        LOGGER.debug("execute {}: start", node.getName());

        CRUDFindRequest findRequest = new CRUDFindRequest();
        findRequest.setQuery(runExpression);
        // TODO: Project only those fields we need
        findRequest.setProjection(FieldProjection.ALL);

        if (sort != null) {
            findRequest.setSort(sort);
        } else if (resolvedReference != null) {
            findRequest.setSort(resolvedReference.getReferenceField().getSort());
        }

        LOGGER.debug("execute {}: findRequest.query={}, projection={}, sort={}", node.getName(),
                findRequest.getQuery(), findRequest.getProjection(), findRequest.getSort());

        CRUDFindResponse response;
        if (sources.isEmpty()) {
            findRequest.setFrom(fromIndex);
            findRequest.setTo(toIndex);
            response = execute(ctx, findRequest, null);
            if (ctx.hasErrors()) {
                if (response != null) {
                    response.setSize(0);
                }
                docs.clear();
            }
        } else {
            // We will evaluate this node for every possible combination of parent docs
            // Some of those parent docs are docs that include the docs of this node.
            // Some of those parent docs are the docs included in the docs of this node.
            // The two cases require different treatment.
            //  1) Straight case: parent node docs contain this node docs
            //     Add all the parent doc child references in a tuple iterator
            //     Refresh bindings for each parent doc, and retrieve
            //  2) Reverse case: parent node docs are contained in this node docs
            //     We can't use child references, because we don't have the parent docs yet
            //     Add all parent docs in a tuple iterator
            //     

            Tuples<DocReference> tuples = new Tuples<>();
            for (QueryPlanNodeExecutor source : sources) {
                List<DocReference> list;
                if (isParentOfThis(source.getNode())) {
                    // The parent query plan node contains documents
                    // that are parents of the documents of this node
                    list = ResultDoc.getChildren(source.docs, node);
                } else {
                    // The parent query plan node contains documents
                    // that are children of the documents of this node
                    list = new ArrayList<>(source.docs.size());
                    for (ResultDoc sourceDoc : source.docs) {
                        for (ResolvedFieldBinding binding : sourceBindings) {
                            list.addAll(binding.getParentDocReferences(sourceDoc));
                        }
                    }
                }
                LOGGER.debug("Adding {} docs from node {} to node {}, source has {} docs",
                        list.size(),
                        source.node.getName(),
                        node.getName(),
                        source.docs.size());
                tuples.add(list);
            }
            response = new CRUDFindResponse();
            // Iterate n-tuples
            for (Iterator<List<DocReference>> tupleItr = tuples.tuples(); tupleItr.hasNext();) {
                List<DocReference> tuple = tupleItr.next();
                LOGGER.debug("Processing a {}-tuple", tuple.size());
                // Tuple elements are ordered the same way as the
                // sources. tuple[i] is from sources[i]
                LOGGER.debug("execute {}: refreshing bindings", node.getName());
                Iterator<DocReference> qpdItr = tuple.iterator();
                while (qpdItr.hasNext()) {
                    DocReference docReference = qpdItr.next();
                    if (docReference instanceof ChildDocReference) {
                        for (ResolvedFieldBinding binding : sourceBindings) {
                            binding.refresh((ChildDocReference) docReference);
                        }
                    } else {
                        for (ResolvedFieldBinding binding : sourceBindings) {
                            if (binding == ((ParentDocReference) docReference).getBinding()) {
                                binding.refresh((ParentDocReference) docReference);
                            }
                        }
                    }
                }

                CRUDFindResponse findResponse = execute(ctx, findRequest, tuple);
                if (ctx.hasErrors()) {
                    response.setSize(0);
                    docs.clear();
                } else {
                    response.setSize(response.getSize() + (findResponse == null ? 0 : findResponse.getSize()));
                }

            }
            // Once all documents are collected, if there are any reversed relationships (i.e. parent node
            // has child documents), we associate them here
            if (!ctx.hasErrors()) {
                for (QueryPlanNodeExecutor source : sources) {
                    if (!isParentOfThis(source.getNode())) {
                        QueryPlanData edgeData = qplan.getEdgeData(source.getNode(), node);
                        if (edgeData != null) {
                            List<Conjunct> edgeClauses = edgeData.getConjuncts();
                            ResultDoc.associateDocs(docs, source.getDocs(), edgeClauses, root);
                        }
                    }
                }
            }
        }
        return response;
    }

    public List<ResultDoc> getDocs() {
        return docs;
    }

    public void setDocs(List<ResultDoc> docs) {
        this.docs = docs;
    }

    public QueryPlanNode getNode() {
        return node;
    }

    private CRUDFindResponse execute(OperationContext ctx,
                                     CRUDFindRequest findRequest,
                                     List<DocReference> parents) {
        OperationContext nodeCtx = ctx.getDerivedOperationContext(node.getMetadata().getName(), findRequest);
        LOGGER.debug("execute {}: entity={}, findRequest.query={}, projection={}, sort={}", node.getName(),
                nodeCtx.getEntityName(),
                findRequest.getQuery(), findRequest.getProjection(), findRequest.getSort());
        // note the response is not used, but find method changes the supplied context.
        CRUDFindResponse response = finder.find(nodeCtx, findRequest);
        LOGGER.debug("execute {}: storing {} documents", node.getName(), nodeCtx.getDocuments().size());
        if (nodeCtx.hasErrors()) {
            ctx.addErrors(nodeCtx.getErrors());
        } else {
            for (DocCtx doc : nodeCtx.getDocuments()) {
                DocId id = docIdx.getDocId(doc.getOutputDocument());
                ResultDoc resultDoc = new ResultDoc(doc.getOutputDocument(), id, node);

                if (parents != null && !parents.isEmpty()) {
                    for (DocReference parent : parents) {
                        if (parent instanceof ChildDocReference) {
                            LOGGER.debug("Adding document to its parent");
                            resultDoc.setParentDoc(parent.getDocument().getQueryPlanNode(), (ChildDocReference) parent);
                            ((ChildDocReference) parent).getChildren().add(resultDoc);
                            LOGGER.debug("Linked parent ref:{}", parent);
                        }
                    }
                }

                LOGGER.debug("Adding {}", id);
                docs.add(resultDoc);
            }
        }
        return response;
    }
}
