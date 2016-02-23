package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.assoc.QueryPlanNode;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.DocIdExtractor;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.Path;

/**
 * A execution block contains the state information required to
 * evaluate a query plan node. Each block is associated with several
 * execution steps that define a pipeline of operations that will be
 * performed on documents.
 */
public class ExecutionBlock {
    /**
     * The query plan node corresponding to this execution block
     */
    private final QueryPlanNode qpNode;

    /**
     * Source execution blocks
     */
    private final List<ExecutionBlock> sourceBlocks=new ArrayList<>();

    /**
     * The edge queries
     */
    private final Map<ExecutionBlock,AssociationQuery> associationQueries=new HashMap<>();

    /**
     * Final step is set by Step constructor. 
     */
    protected Step<?> finalStep;

    /**
     * Document ID extractor for the documents of this block
     */
    private final DocIdExtractor idx;

    /**
     * The resolved reference pointing to the entity of this block
     */
    private final ResolvedReferenceField reference;

    public ExecutionBlock(CompositeMetadata root,QueryPlanNode qpNode) {
        this.qpNode=qpNode;
        Field[] f=getMetadata().getEntitySchema().getIdentityFields();
        Path[] identityFields=new Path[f.length];
        for(int i=0;i<f.length;i++)
            identityFields[i]=f[i].getFullPath();
        idx=new DocIdExtractor(identityFields);
        Path entityPath=getMetadata().getEntityPath();
        if(entityPath.isEmpty()) {
            reference=null;
        } else {
            reference=root.getChildReference(entityPath);
        }
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
     * Returns an ID extractor for the documents produced by this execution block
     */
    public DocIdExtractor getIdExtractor() {
        return idx;
    }

    /**
     * Returns the final step of this block
     */
    public Step<?> getFinalStep() {
        return finalStep;
    }


    /**
     * Returns the association query for the edge coming from the
     * source block into this block
     */
    public AssociationQuery getAssociationQueryForEdge(ExecutionBlock sourceBlock) {
        return associationQueries.get(sourceBlock);        
    }

    /**
     * Sets an edge query
     */
    public void setAssociationQuery(ExecutionBlock sourceBlock,AssociationQuery q) {
        associationQueries.put(sourceBlock,q);
    }
}
