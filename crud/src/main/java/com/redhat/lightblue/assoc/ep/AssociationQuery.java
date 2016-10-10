package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.assoc.BoundObject;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.RewriteQuery;

import com.redhat.lightblue.query.QueryExpression;

/**
 * Keeps an edge query along with its binding information.
 */
public class AssociationQuery {

    private final List<BoundObject> fieldBindings = new ArrayList<>();
    private final QueryExpression query;
    private final ResolvedReferenceField reference;
    // If non-null, query is either always true or always false
    private final Boolean always;

    public AssociationQuery(CompositeMetadata root,
                            CompositeMetadata currentEntity,
                            ResolvedReferenceField reference,
                            List<Conjunct> conjuncts) {
        this.reference = reference;
        RewriteQuery rewriter = new RewriteQuery(root, currentEntity);
        List<QueryExpression> queries = new ArrayList<>(conjuncts.size());
        int numTrue=0;
        int numFalse=0;
        for (Conjunct c : conjuncts) {
            RewriteQuery.RewriteQueryResult result = rewriter.rewriteQuery(c.getClause(), c.getFieldInfo());
            if(result.query instanceof RewriteQuery.TruePH) {
                // Don't add this into the query
                numTrue++;
            } else if(result.query instanceof RewriteQuery.FalsePH) {
                numFalse++;
            } else {
                queries.add(result.query);
            }
            fieldBindings.addAll(result.bindings);
        }
        if(queries.isEmpty()) {
            query=null;
            if(numTrue>0&&numFalse==0) {
                always=Boolean.TRUE;
            } else if(numFalse>0) {
                always=Boolean.FALSE;
            } else {
                always=null;
            }
        } else {
            query = Searches.and(queries);
            always=null;
        }
    }

    public Boolean getAlways() {
        return always;
    }

    public boolean isAlwaysTrue() {
        return always!=null&&always;
    }

    public boolean isAlwaysFalse() {
        return always!=null&&!always;
    }

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

    @Override
    public String toString() {
        return query.toString() + " [ " + fieldBindings + " ]";
    }
}
