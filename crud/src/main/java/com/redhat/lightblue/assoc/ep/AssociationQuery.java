package com.redhat.lightblue.assoc.ep;

/**
 * Keeps an edge query along with its binding information. Provides bound copies of the query as needed.
 */
public class AssociationQuery {

    private final List<FieldBinding> fieldBindings;
    private final QueryExpression query;
    private final ResolvedReferenceField reference;
    
    public AssociationQuery(CompositeMetadata root,
                            ResolvedReferenceField reference,
                            List<Conjunct> conjuncts) {
        this.reference=reference;
        CompositeMetadata currentEntity=reference.getReferencedMetadata();
        RewriteQuery rewriter=new RewriteQuery(root,currentEntity);
        List<QueryExpression> queries=new ArrayList<>(conjuncts.size());
        for(Conjunct c:conjuncts) {
            RewriteQuery.RewriteQueryResult result=rewriter.rewriteQuery(c.getClause(),c.getFieldInfo());
            LOGGER.debug("Rewrote {} as {}, bindings {}",c.getClause(),result.query,result.bindings);
            queries.add(result.query);
            fieldBindings.addAll(result.bindings);
        }
        if(queries.size()>1)
            query=new NaryLogicalExpression(NaryLogicalOperator._and,queries);
        else
            query=queries.get(0);
    }

    public QueryExpression getQuery() {
        return query;
    }
    
    public List<FieldBinding> getFieldBindings() {
        return fieldBindings;
    }

    /**
     * Returns the reference field from the parent entity to current entity
     */
    public ResolvedReferenceField getReference() {
        return reference;
    }
    
    /**
     * Group bindings by entity
     */
    public static Map<CompositeMetadata,List<FieldBinding>> groupBindings(List<FieldBinding> bindings) {
        return bindings.stream().collect(Collectors.groupingBy(f -> f.getFieldInfo().getFieldEntity(),
                                                               Collectors.toList()));
    }            

    @Override
    public String toString() {
        return query.toString()+" [ "+fieldBindings+" ]";
    }
}
