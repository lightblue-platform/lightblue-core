package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.assoc.QueryPlanNode;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.DocIdExtractor;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.Path;

/**
 * A execution block contains the state information required to evaluate a query
 * plan node. Each block is associated with several execution steps that define
 * a pipeline of operations that will be performed on documents.
 */
public class ExecutionBlock {

    public final CompositeMetadata rootMd;

    /**
     * The query plan node corresponding to this execution block
     */
    private final QueryPlanNode qpNode;

    /**
     * Source execution blocks
     */
    private final List<ExecutionBlock> sourceBlocks = new ArrayList<>();

    /**
     * The edge queries
     */
    private final Map<ExecutionBlock, AssociationQuery> associationQueries = new HashMap<>();

    /**
     * Result step of this block
     */
    protected Step<ResultDocument> resultStep;

    /**
     * All steps of this block
     */
    protected final List<Step<?>> steps = new ArrayList<Step<?>>();

    /**
     * Document ID extractor for the documents of this block
     */
    private final DocIdExtractor idx;

    /**
     * The resolved reference pointing to the entity of this block
     */
    private final ResolvedReferenceField reference;

    /**
     * List of all reference fields pointing to child docs
     */
    private List<ChildSlot> childSlots = new ArrayList<>();

    private boolean slotsHaveAnys = false;

    public ExecutionBlock(CompositeMetadata root, QueryPlanNode qpNode) {
        this.rootMd = root;
        this.qpNode = qpNode;
        Field[] f = getMetadata().getEntitySchema().getIdentityFields();
        Path[] identityFields = new Path[f.length];
        for (int i = 0; i < f.length; i++) {
            identityFields[i] = getMetadata().getEntityRelativeFieldName(f[i]);
        }
        idx = new DocIdExtractor(identityFields);
        Path entityPath = getMetadata().getEntityPath();
        if (entityPath.isEmpty()) {
            reference = null;
        } else {
            reference = root.getDescendantReference(entityPath);
        }
    }

    /**
     * This should be called after all execution blocks are built
     */
    public void linkBlocks() {
        // Build a list of all reference fields that need to be populated for the docs produces by this block
        // But to do that, we need the destination nodes of this block. We don't have that.
        // What we have is the sources. So, we populate the reference fields of our sources instead.
        for (ExecutionBlock source : sourceBlocks) {
            if (reference != null) {
                // Is this node really a child of the source node?
                if (getMetadata().getParent() == source.getMetadata()) {
                    source.addChildSlot(reference);
                }
            }
        }
    }

    public void initializeSteps() {
        steps.stream().forEach(Step::initialize);
    }

    /**
     * Returns the slots for the child documents of the documents of this block
     */
    public List<ChildSlot> getChildSlots() {
        return childSlots;
    }

    /**
     * This returns true if at least one of the child slots contains an array
     * reference
     */
    public boolean childSlotsHaveArrays() {
        return slotsHaveAnys;
    }

    public void addChildSlot(ResolvedReferenceField reference) {
        ChildSlot slot = new ChildSlot(rootMd, reference);
        if (slot.hasAnys()) {
            slotsHaveAnys = true;
        }
        childSlots.add(slot);
    }

    /**
     * Adds a source block
     */
    public void addSourceBlock(ExecutionBlock source) {
        sourceBlocks.add(source);
    }

    /**
     * Returns the query plan node for this block
     */
    public QueryPlanNode getQueryPlanNode() {
        return qpNode;
    }

    /**
     * Returns the resolved reference pointing to the entity of this block
     */
    public ResolvedReferenceField getReference() {
        return reference;
    }

    /**
     * Returns the metadata for the entity associated with this block
     */
    public CompositeMetadata getMetadata() {
        return qpNode.getMetadata();
    }

    /**
     * Returns the identity fields for the entity
     */
    public Path[] getIdentityFields() {
        return idx.getIdentityFields();
    }

    /**
     * Returns the source execution blocks
     */
    public List<ExecutionBlock> getSourceBlocks() {
        return sourceBlocks;
    }

    /**
     * Returns an ID extractor for the documents produced by this execution
     * block
     */
    public DocIdExtractor getIdExtractor() {
        return idx;
    }

    /**
     * Returns a step of the given type
     */
    public <X> X getStep(Class<X> clazz) {
        for (Step<?> x : steps) {
            if (clazz.isAssignableFrom(x.getClass())) {
                return (X) x;
            }
        }
        return null;
    }

    public void registerStep(Step<?> step) {
        steps.add(step);
    }

    /**
     * Returns the result step of this block
     */
    public Step<ResultDocument> getResultStep() {
        return resultStep;
    }

    /**
     * Sets the result step of this block
     */
    public void setResultStep(Step<ResultDocument> resultStep) {
        this.resultStep = resultStep;
    }

    public void setResultStep(Source<ResultDocument> resultStep) {
        this.resultStep = resultStep.getStep();
    }

    /**
     * Returns the association query for the edge coming from the source block
     * into this block
     */
    public AssociationQuery getAssociationQueryForEdge(ExecutionBlock sourceBlock) {
        return associationQueries.get(sourceBlock);
    }

    /**
     * Sets an edge query
     */
    public void setAssociationQuery(ExecutionBlock sourceBlock, AssociationQuery q) {
        associationQueries.put(sourceBlock, q);
    }

    @Override
    public String toString() {
        return qpNode.toString();
    }

    public JsonNode toJson() {
        return resultStep.toJson();
    }

    public JsonNode explain(ExecutionContext ctx) {
        return resultStep.explain(ctx);
    }
}
