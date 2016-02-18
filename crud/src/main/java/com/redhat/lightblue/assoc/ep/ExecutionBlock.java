package com.redhat.lightblue.assoc.ep;

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
    private List<ExecutionBlock> sourceBlocks;

    /**
     * The edge queries
     */
    private Map<ExecutionBlock,AssociationQuery> associationQueries=new HashMap<>();

    /**
     * Returns the query plan node for this block
     */
    public QueryPlanNode getQueryPlanNode() {
        return qpNode;
    }

    /**
     * Returns the source execution blocks
     */
    public List<ExecutionBlock> getSourceBlocks() {
        return sourceBlocks;
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
