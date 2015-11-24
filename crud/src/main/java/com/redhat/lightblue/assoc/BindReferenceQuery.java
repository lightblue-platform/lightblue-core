public class BindReferenceQuery extends BindFields<FieldBinding> {

    /**
     * The composite metadata for the entity relative to which we're evaluating the query
     */
    private final CompositeMetadata currentEntity;

    /**
     * The root composite metadata
     */
    private final CompositeMetadata root;

    /**
     * The reference field containing the query. Null if the query is a request query
     */
    private final ResolvedReferenceField ref;

    private void resolveField(Path clauseFieldName,
                              Path context,
                              QueryExpression clause) {
        // fullFieldName: The name of the field, including any enclosing elemMatch queries
        Path fullFieldName=context.isEmpty()?clauseFieldName:new Path(context,clauseFieldName);
        // The field node in metadata. Resolved relative to the
        // reference field if the query is for a reference, or
        // resolved relative to the root if the query is a request
        // query
        FieldTreeNode fieldNode=ref==null?root.resolve(fullFieldName):
            ref.getElement().resolve(fullFieldName);
        if(fieldNode==null)
            throw Error.get(AssocConstants.ERR_CANNOT_FIND_FIELD,clauseFieldName);
        // rr: The first resolved reference ancestor of the field
        ResolvedReferenceField rr=root.getResolvedReferenceOfField(fieldNode);
        // fieldMd: The composite metadata containing the field
        CompositeMetadata fieldMd=rr==null?root:rr.getReferencedMetadata();
        // Now compute entity relative field name
        // The field name may contain array indexes in it. The metadata field loses that info,
        // replacing all indexes with '*'. We have to put the indexes back into the entity
        // relative field name.
        
        // normalizedFieldName: the field name where $parent can only appear at the beginning
        // No $this can appear
        Path normalizedFieldName=fullFieldName.normalize();
        // normalize
        
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        resolveField(q.getField(),context,q);
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to recursively iterate the nested query. If nested
     * processing returns an object different from the original nested query,
     * this method creates a new unary logical expression using the new query
     * expression, and returns that.
     */
    protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        QueryExpression newq = iterate(q.getQuery(), context);
        if (newq != q.getQuery()) {
            return new UnaryLogicalExpression(q.getOp(), newq);
        } else {
            return q;
        }
    }

    /**
     * Default behavior is to recursively iterate the nested quereies. If nested
     * processing returns objects different from the original nested queries,
     * this method creates a new n-ary logical expression using the new query
     * expressions and returns that.
     */
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<QueryExpression>(q.getQueries());
        while (itr.hasNext()) {
            QueryExpression nestedq = itr.next();
            QueryExpression newq = iterate(nestedq, context);
            if (newq != nestedq) {
                itr.set(newq);
            }
        }
        if (itr.isCopied()) {
            return new NaryLogicalExpression(q.getOp(), itr.getCopiedList());
        } else {
            return q;
        }
    }

    /**
     * Default behavior is to recursively iterate the nested query. If nested
     * processing returns an object different from the original nested query,
     * this method creates a new array match expression using the new query
     * expression, and returns that.
     */
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {



        
    @Override
    protected T newValueBinding(Path field,Path ctx,BoundValue value,QueryExpression originalQuery,QueryExpression newQuery) {
        return (T)new ValueBinding(field,value,originalQuery,newQuery);
    }

    @Override
    protected T newListBinding(Path field,Path ctx,BoundValueList value,QueryExpression originalQuery,QueryExpression newQuery) {
        return (T)new ListBinding(field,value,originalQuery,newQuery);
    }
    
}
