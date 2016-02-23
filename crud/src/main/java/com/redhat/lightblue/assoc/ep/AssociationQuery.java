package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.assoc.BoundObject;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.RewriteQuery;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

/**
 * Keeps an edge query along with its binding information. Provides bound copies of the query as needed.
 */
public class AssociationQuery {

    private final CompositeMetadata root;
    private final CompositeMetadata currentEntity;
    private final List<BoundObject> fieldBindings;
    private final QueryExpression query;
    private final ResolvedReferenceField reference;
    private final List<Conjunct> conjuncts;
    
    public AssociationQuery(CompositeMetadata root,
                            CompositeMetadata currentEntity,
                            ResolvedReferenceField reference,
                            List<Conjunct> conjuncts) {
        this.root=root;
        this.reference=reference;
        this.conjuncts=conjuncts;
        this.currentEntity=currentEntity;
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

    // /**
    //  * Returns an association query for the other side of the association
    //  */
    // public AssociationQuery flip() {
    //     CompositeMetadata flippedEntity;
    //     if(reference.getReferencedMetadata()==currentEntity) {
    //         flippedEntity=currentEntity().getParent();
    //     } else {
    //         flippedEntity=reference.getReferencedMetadata();
    //     }
    //     return new AssociationQuery(root,flippedEntity,reference,conjuncts;
    // }

    public QueryExpression getQuery() {
        return query;
    }
    
    public List<BoundObject> getFieldBindings() {
        return fieldBindings;
    }

    /**
     * Returns the reference field from the parent entity to current entity
     */
    public ResolvedReferenceField getReference() {
        return reference;
    }
    
    // /**
    //  * Group bindings by entity
    //  */
    // public static Map<CompositeMetadata,List<FieldBinding>> groupBindings(List<FieldBinding> bindings) {
    //     return bindings.stream().collect(Collectors.groupingBy(f -> f.getFieldInfo().getFieldEntity(),
    //                                                            Collectors.toList()));
    // }            

    @Override
    public String toString() {
        return query.toString()+" [ "+fieldBindings+" ]";
    }
}
