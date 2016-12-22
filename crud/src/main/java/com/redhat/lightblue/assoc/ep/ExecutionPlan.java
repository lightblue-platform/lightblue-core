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
package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.FieldCursor;

import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.eval.SortFieldInfo;

import com.redhat.lightblue.util.Path;

/**
 * Execution plan is a tree of execution blocks. Every node in the query plan is
 * converted to an execution block, and connected in the same way query plan
 * nodes are connected. Each execution block contains a pipeline, and every step
 * of the pipeline performs a step of the operation.
 *
 * The search plan and retrieval plan are constructed differently.
 *
 * In the retrieval plan, the root entity is at the root, and every destination
 * node is a child node. So, the assembler first runs the parent node, gets the
 * docs, and then runs the child nodes and attaches the documents to the slots.
 *
 * Query plan is optional, and it has a different layout than the retrieval
 * plan. All that we do with the query plan is to retrieve the root node
 * documents, so query plan does not retrieve all associated entities.
 */
public class ExecutionPlan {

    static private final Logger LOGGER = LoggerFactory.getLogger(ExecutionPlan.class);

    private Step<ResultDocument> resultStep;

    /**
     * Creates an execution plan
     *
     * @param requestQuery query requested by the client
     * @param requestProjection projection requested by the client
     * @param requestSort sort requested by the client.
     * @param from request.from
     * @param to request.to
     * @param rootMd Root entity composite metadata
     * @param searchQueryPlan if the results of a search is to be retrieved in a
     * second pass, not null. Otherwise, null.
     * @param retrievalQueryPlan Never null. Contains the plan for the retrieval
     * of found documents. If the searchQueryPlan is not null, retrieves the
     * documents found by that search. If searchQueryPlan is null, this plan
     * performs the search and retrieval.
     */
    public ExecutionPlan(QueryExpression requestQuery,
                         Projection requestProjection,
                         Sort requestSort,
                         Long from,
                         Long to,
                         CompositeMetadata rootMd,
                         QueryPlan searchQueryPlan,
                         QueryPlan retrievalQueryPlan) {

        // Specifies if a filtering layer is needed at the end of the
        // pipeline.  This is needed if searchplan returns more than
        // what's necessary (e.g. if there are query clauses that are
        // not assigned to query plan nodes or edges)
        boolean needsFinalFiltering=false;

        // This is set to true when we identify the step that sets matchcount
        boolean matchCountSet=false;


        // This is set if there are queries associated with the nodes
        // that are not the root node of the retrieval plan
        boolean queries_in_non_root_nodes=false;
        for(QueryPlanNode node:retrievalQueryPlan.getAllNodes()) {
            if(node.getSources().length!=0&& // non-root
               !node.getData().getConjuncts().isEmpty()) {
                queries_in_non_root_nodes=true;
                break;
            }
        }
        // First, create execution blocks for every node in the search and
        // retrieval plans. We keep a map of query plan nodes to execution
        // blocks to keep query plan immutable.
        // This needs to be done in two steps: first we create the
        // nodes, then we attach them to each other.
        ExecutionBlock rootEntityInQueryPlan = null;
        Map<QueryPlanNode, ExecutionBlock> qp2BlockMap = new HashMap<>();
        if (searchQueryPlan != null) {
            // Create nodes
            for (QueryPlanNode node : searchQueryPlan.getAllNodes()) {
                ExecutionBlock block = new ExecutionBlock(rootMd, node);
                if (node.getMetadata().getParent() == null) {
                    rootEntityInQueryPlan = block;
                }
                qp2BlockMap.put(node, block);
            }
        }
        ExecutionBlock retrievalRoot = null;
        for (QueryPlanNode node : retrievalQueryPlan.getAllNodes()) {
            ExecutionBlock block = new ExecutionBlock(rootMd, node);
            if (node.getMetadata().getParent() == null) {
                retrievalRoot = block;
            }
            qp2BlockMap.put(node, block);
        }
        // Connect the blocks
        for (Map.Entry<QueryPlanNode, ExecutionBlock> entry : qp2BlockMap.entrySet()) {
            for (QueryPlanNode source : entry.getKey().getSources()) {
                entry.getValue().addSourceBlock(qp2BlockMap.get(source));
            }
        }

        List<QueryFieldInfo> qfi=null;
        if (searchQueryPlan != null) {
            LOGGER.debug("Building execution plan from search query plan:{}", searchQueryPlan);
            List<Conjunct> unassigned = searchQueryPlan.getUnassignedClauses();
            
            // If query root has destinations in the search query plan, then
            // those destinations are inaccessible, becase we evaluate the search plan
            // up to root. That means, we'll need to filter
            if(!unassigned.isEmpty()||rootEntityInQueryPlan.getQueryPlanNode().getDestinations().length>0)
            	needsFinalFiltering=true;

            qfi = getAllQueryFieldInfo(searchQueryPlan);
            // Lets see if the root entity is the only source of this plan
            QueryPlanNode[] qpSources = searchQueryPlan.getSources();
            boolean rootIsTheOnlySource = qpSources.length == 1 && qpSources[0].getMetadata().getParent() == null;

            for (QueryPlanNode node : searchQueryPlan.getAllNodes()) {
                ExecutionBlock block = qp2BlockMap.get(node);
                AbstractSearchStep search;
                if (block.getSourceBlocks().isEmpty()) {
                    search = new Search(block);
                    block.setResultStep(search);
                } else {
                    for (ExecutionBlock source : block.getSourceBlocks()) {
                        QueryPlanData edgeData = searchQueryPlan.getEdgeData(source.getQueryPlanNode(),
                                block.getQueryPlanNode());
                        if (edgeData != null) {
                            AssociationQuery aq = new AssociationQuery(rootMd,
                                    block.getMetadata(),
                                    block.getReference(),
                                    edgeData.getConjuncts());
                            block.setAssociationQuery(source, aq);
                        }

                    }

                    // Block has sources. Join them
                    List<Source<ResultDocument>> list = (List<Source<ResultDocument>>) block.getSourceBlocks().stream().
                            map(Source<ResultDocument>::new).
                            collect(Collectors.toList());
                    Join join = new Join(block, list.toArray(new Source[list.size()]));
                    search = new JoinSearch(block, new Source<>(join));
                    block.setResultStep(search);
                }
                // Now that we have the search, we set the queries, projection, and limits

                // The queries for the search are already set in the query plan node
                search.setQueries(node.getData().getConjuncts());
                if (block == rootEntityInQueryPlan) {
                    // This is the block for the root entity
                    if (rootIsTheOnlySource) {
                        if (unassigned.isEmpty()&&!queries_in_non_root_nodes) {
                            // Root is the only source, and there are no unassigned clauses
                            // We can sort/limit here
                            search.setLimit(from, to);
                            search.setSort(requestSort);
                            // We record the match count here
                            search.setRecordResultSetSize(true);
                            matchCountSet=true;
                            // For all other cases, match count is set in the retrieval plan
                        } else {
                            // There are unassigned clauses. We can sort during search, but we can't do filter or limit
                            search.setSort(requestSort);
                        }
                    } else {
                        // Root is not the only source
                        // Root can be an intermediate query plan node, or there may be more than one sources

                        // Make sure we have unique docs
                        Unique u=new Unique(block, new Source<>(search));
                        if(unassigned.isEmpty()&&!matchCountSet) {
                            u.setRecordResultSetSize(true);
                            matchCountSet=true;
                        }
                        Source<ResultDocument> last = new Source<>(u);
                        // Sort the results
                        if (requestSort != null) {
                            last = new Source<>(new SortResults(block, last, requestSort));
                        }
                        if(!needsFinalFiltering) {
                        	if (from != null) {
                        		last = new Source<>(new Skip(block, from.intValue(), last));
                        	}
                        	if (to != null) {
                        		last = new Source<>(new Limit(block, to.intValue() - (from==null?0:from.intValue()) + 1, last));
                        	}
                        }
                        block.setResultStep(last);
                    }
                    // Set the root projection
                    Set<Path> fields = getIncludedFieldsOfEntityForSearch(block, qfi);
                    fields.addAll(getIncludedFieldsOfEntityForProjection(block, rootMd, requestProjection));
                    fields.addAll(getIncludedFieldsOfRootEntityForSort(rootMd, requestSort));
                    Projection p = writeProjection(fields);
                    LOGGER.debug("Projection for block {}:{}", block.getQueryPlanNode().getName(), p);
                    search.setProjection(p);
                } else {
                    // An intermediate node. No sort/limit/unique is necessary
                    // Set the projection
                    Set<Path> fields = getIncludedFieldsOfEntityForSearch(block, qfi);
                    fields.addAll(getIncludedFieldsOfEntityForProjection(block, rootMd, null));
                    Projection p = writeProjection(fields);
                    LOGGER.debug("Projection for block {}:{}", block.getQueryPlanNode().getName(), p);
                    search.setProjection(p);
                }
            }

        }

        // Done with the search plan. Now we build the execution plan for retrieval
        LOGGER.debug("Building execution plan from retrieval query plan:{}", retrievalQueryPlan);
        List<Conjunct> unassigned = retrievalQueryPlan.getUnassignedClauses();
        if(qfi==null) {
            qfi = getAllQueryFieldInfo(retrievalQueryPlan);
        } else {
            qfi.addAll(getAllQueryFieldInfo(retrievalQueryPlan));
        }
        for (QueryPlanNode node : retrievalQueryPlan.getAllNodes()) {
            ExecutionBlock block = qp2BlockMap.get(node);
            QueryPlanNode[] destinationNodes = node.getDestinations();
            ExecutionBlock[] destinationBlocks = new ExecutionBlock[destinationNodes.length];
            for (int i = 0; i < destinationNodes.length; i++) {
                destinationBlocks[i] = qp2BlockMap.get(destinationNodes[i]);
            }
            if (block == retrievalRoot) {
                // Processing the root node
                // If there is a search plan, then we already have the root documents, so simply stream
                // those docs from the search plan. If there is not a search plan, we search the documents
                // here
                Source<ResultDocument> last;
                AbstractSearchStep search;
                if (rootEntityInQueryPlan != null) {
                    // There is a search plan. The search root contains the documents
                    search = new Copy(block, new Source<>(rootEntityInQueryPlan.getResultStep()));
                    if(!matchCountSet&&unassigned.isEmpty()) {
                        search.setRecordResultSetSize(true);
                        matchCountSet=true;
                    }
                    last = new Source(search);
                } else {
                    //There is not a search plan. We'll search documents here
                    search = new Search(block);
                    last = new Source<>(search);
                    if (unassigned.isEmpty()&&!queries_in_non_root_nodes) {
                        // There are no unassigned clauses or queries in non-root nodes
                        // We can sort/limit here
                        search.setLimit(from, to);
                        search.setSort(requestSort);
                        if(!matchCountSet) {
                            search.setRecordResultSetSize(true);
                            matchCountSet=true;
                        }    
                    } else {
                        // There are unassigned clauses. We can sort during search, but we have to filter and limit
                        search.setSort(requestSort);
                        needsFinalFiltering=true;
                    }
                }
                
                Set<Path> fields = getIncludedFieldsOfEntityForSearch(block, qfi);
                fields.addAll(getIncludedFieldsOfEntityForProjection(block, rootMd, requestProjection));
                search.setProjection(writeProjection(fields));
                search.setQueries(node.getData().getConjuncts());
                resultStep = new Assemble(block, last, destinationBlocks);
                if(needsFinalFiltering) {
                    resultStep = new Filter(block, new Source<>(resultStep), requestQuery);
                    ((Filter)resultStep).setRecordResultSetSize(true);
                    matchCountSet=true;
                    if (from != null) {
                        resultStep = new Skip(block, from.intValue(), new Source<>(resultStep));
                    }
                    if (to != null) {
                        resultStep = new Limit(block, to.intValue() - (from==null?0:from.intValue()) + 1, new Source<>(resultStep));
                    }
                }
                resultStep = new Project(block, new Source<>(resultStep), requestProjection);
                block.setResultStep(resultStep);
            } else {
                // Processing one of the associated entity nodes
                // Reuse the batching algorithm in join                
                ExecutionBlock source = block.getSourceBlocks().get(0);
                QueryPlanData edgeData = retrievalQueryPlan.getEdgeData(source.getQueryPlanNode(),
                        block.getQueryPlanNode());
                if (edgeData != null) {
                    AssociationQuery aq = new AssociationQuery(rootMd,
                            block.getMetadata(),
                            block.getReference(),
                            edgeData.getConjuncts());
                    block.setAssociationQuery(source, aq);
                }
                Retrieve search = new Retrieve(block);
                search.setQueries(node.getData().getConjuncts());
                Set<Path> fields = getIncludedFieldsOfEntityForSearch(block, qfi);
                fields.addAll(getIncludedFieldsOfEntityForProjection(block, rootMd, requestProjection));
                search.setProjection(writeProjection(fields));
                block.setResultStep(new Assemble(block, new Source<>(search), destinationBlocks));
            }
        }

        for (ExecutionBlock block : qp2BlockMap.values()) {
            block.linkBlocks();
        }
        for (ExecutionBlock block : qp2BlockMap.values()) {
            block.initializeSteps();
        }
    }

    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        return resultStep.getResults(ctx);
    }

    private List<QueryFieldInfo> getAllQueryFieldInfo(QueryPlan qp) {
        // Build a list of all query field info to determine projections
        List<QueryFieldInfo> qfi = new ArrayList<>();
        qp.getUnassignedClauses().stream().forEach(c -> qfi.addAll(c.getFieldInfo()));
        QueryPlanNode[] allNodes = qp.getAllNodes();
        for (QueryPlanNode node : allNodes) {
            if (node.getData() != null) {
                node.getData().getConjuncts().stream().forEach(c -> qfi.addAll(c.getFieldInfo()));
            }
            for (QueryPlanNode source : node.getSources()) {
                QueryPlanData edgeData = qp.getEdgeData(source, node);
                if (edgeData != null) {
                    edgeData.getConjuncts().stream().forEach(c -> qfi.addAll(c.getFieldInfo()));
                }
            }
        }
        return qfi;
    }

    /**
     * Writes a projection to include the given set of fields
     */
    public static Projection writeProjection(Collection<Path> fields) {
        List<Projection> list = new ArrayList<>(fields.size());
        for (Path p : fields) {
            list.add(new FieldProjection(p, true, false));
        }
        return list.size() == 1 ? list.get(0) : new ProjectionList(list);
    }

    /**
     * Returns the included fields of an entity based on query information. This
     * is to be used for search plan nodes only
     */
    public static Set<Path> getIncludedFieldsOfEntityForSearch(ExecutionBlock block,
                                                               List<QueryFieldInfo> fieldInfo) {
        CompositeMetadata md = block.getMetadata();
        Set<Path> ret = new HashSet<>();
        for (QueryFieldInfo fi : fieldInfo) {
            if (md == fi.getFieldEntity() && !(fi.getFieldMd() instanceof ArrayField
                    || fi.getFieldMd() instanceof ObjectField)) {
                ret.add(fi.getEntityRelativeFieldNameWithContext());
            }
        }
        // Add the identities
        for (Path p : block.getIdentityFields()) {
            ret.add(p);
        }
        return ret;
    }

    /**
     * Returns the fields included based on the requested projection and
     * reference field projection
     */
    public static Set<Path> getIncludedFieldsOfEntityForProjection(ExecutionBlock block,
                                                                   CompositeMetadata root,
                                                                   Projection requestProjection) {
        Set<Path> fields = new HashSet<>();
        // What is the path prefix for this entity?
        CompositeMetadata md = block.getMetadata();
        Path entityPath = md.getEntityPath();
        Path globalPrefix = entityPath.isEmpty() ? Path.EMPTY : new Path(entityPath, Path.ANYPATH);
        // entityPath is either empty, meaning this is the root, or a path to a reference field
        // If entityPath is not empty, then find the projection in the reference field
        ResolvedReferenceField reference = block.getReference();
        Projection localProjection;
        if (reference != null) {
            localProjection = reference.getReferenceField().getProjection();
        } else {
            localProjection = null;
        }
        FieldCursor cursor = md.getFieldCursor();
        Path skipPrefix = null;
        Path globalField;
        while (cursor.next()) {
            Path localField = cursor.getCurrentPath();
            globalField = globalPrefix.isEmpty() ? localField : new Path(globalPrefix, localField);
            FieldTreeNode node = cursor.getCurrentNode();
            if (skipPrefix != null) {
                if (!localField.matchingDescendant(skipPrefix)) {
                    skipPrefix = null;
                }
            }
            if (skipPrefix == null) {
                if (node instanceof ResolvedReferenceField
                        || node instanceof ReferenceField) {
                    skipPrefix = localField;
                } else if ((node instanceof ObjectField)
                        || (node instanceof ArrayField && ((ArrayField) node).getElement() instanceof ObjectArrayElement)
                        || (node instanceof ArrayElement)) {
                    // include its member fields
                } else if(node instanceof ArrayField && ((ArrayField) node).getElement() instanceof SimpleArrayElement) {
                    fields.add(new Path(localField,Path.ANYPATH));
                } else {
                    if (localProjection != null && localProjection.isFieldRequiredToEvaluateProjection(localField)) {
                        LOGGER.debug("{}: required", localField);
                        fields.add(localField);
                    }
                    if (requestProjection != null && requestProjection.isFieldRequiredToEvaluateProjection(globalField)) {
                        LOGGER.debug("{}: required", localField);
                        fields.add(localField);
                    }
                }
            }
        }
        return fields;
    }

    public static Set<Path> getIncludedFieldsOfRootEntityForSort(CompositeMetadata root,
                                                                 Sort rootSort) {
        Set<Path> ret = new HashSet<>();
        if (rootSort != null) {
            SortFieldInfo[] sfi = SortFieldInfo.buildSortFields(rootSort, root.getFieldTreeRoot());
            for (SortFieldInfo fi : sfi) {
                ret.add(fi.getName());
            }
        }
        return ret;
    }

    public static Set<Path> getIncludedFieldsOfEntityForSort(ExecutionBlock block) {
        Set<Path> ret = new HashSet<>();
        ResolvedReferenceField reference = block.getReference();
        if (reference != null) {
            Sort sort = reference.getReferenceField().getSort();
            SortFieldInfo[] sfi = SortFieldInfo.buildSortFields(sort, block.getMetadata().getFieldTreeRoot());
            for (SortFieldInfo fi : sfi) {
                ret.add(fi.getName());
            }
        }
        return ret;
    }

    public JsonNode toJson() {
        return resultStep.toJson();
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JsonNode explain(ExecutionContext ctx) {
        return resultStep.explain(ctx);
    }
}
