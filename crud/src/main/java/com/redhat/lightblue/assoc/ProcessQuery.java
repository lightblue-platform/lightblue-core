package com.redhat.lightblue.assoc;

import java.util.ArrayList;
import java.util.List;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Error;

public class ProcessQuery extends QueryIterator {

    public static class QueryFieldInfo {
        private final Path fieldNameInClause;
        private final Path fullFieldName;
        private final FieldTreeNode fieldMd;
        private final CompositeMetadata fieldEntity;
        private final Path entityRelativeFieldName;
        private final QueryExpression clause;

        public QueryFieldInfo(Path fieldNameInClause,
                              Path fullFieldName,
                              FieldTreeNode fieldMd,
                              CompositeMetadata fieldEntity,
                              Path entityRelativeFieldName,
                              QueryExpression clause) {
            this.fieldNameInClause=fieldNameInClause;
            this.fullFieldName=fullFieldName;
            this.fieldMd=fieldMd;
            this.fieldEntity=fieldEntity;
            this.entityRelativeFieldName=entityRelativeFieldName;
            this.clause=clause;
        }

        public Path getFieldNameInClause() {
            return fieldNameInClause;
        }

        public Path getFullFieldName() {
            return fullFieldName;
        }

        public FieldTreeNode getFieldMd() {
            return fieldMd;
        }

        public CompositeMetadata getFieldEntity() {
            return fieldEntity;
        }

        public Path getEntityRelativeFieldName() {
            return entityRelativeFieldName;
        }

        public QueryExpression getClause() {
            return clause;
        }
    }
    
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

    private List<QueryFieldInfo> fieldInfo=new ArrayList<>();

    public ProcessQuery(CompositeMetadata currentEntity,
                        CompositeMetadata root,
                        ResolvedReferenceField referenceField) {
        this.currentEntity=currentEntity;
        this.root=root;
        this.ref=referenceField;
    }

    public List<QueryFieldInfo> getFieldInfo() {
        return fieldInfo;
    }
    
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
            throw Error.get(AssocConstants.ERR_CANNOT_FIND_FIELD,clauseFieldName.toString());
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

        // Now compute the relative field name within its entity
        // start from fieldNode, go backwards until the entity boundary
        // copy the indexes from the normalizedFieldName
        FieldTreeNode trc=fieldNode;
        int n=normalizedFieldName.numSegments()-1;
        ArrayList<String> list=new ArrayList<>(n);
        do {
            String name=trc.getName();
            if(Path.ANY.equals(name)) {
                list.add(normalizedFieldName.head(n));
            } else {
                list.add(name);
            }
            trc=trc.getParent();
            if( (trc instanceof ArrayElement&&
                 ((ArrayElement)trc).getParent() instanceof ResolvedReferenceField) ||
                trc.getParent()==null ) {
                // Entity boundary, or root
                trc=null;
            }
        } while(trc!=null);
        MutablePath p=new MutablePath();
        n=list.size();
        for(int i=n-1;i>=0;i--)
            p.push(list.get(i));
        Path entityRelativeFieldName=p.immutableCopy();
        fieldInfo.add(new QueryFieldInfo(clauseFieldName,
                                         fullFieldName,
                                         fieldNode,
                                         fieldMd,
                                         entityRelativeFieldName,
                                         clause));
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        resolveField(q.getField(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        resolveField(q.getField(),context,q);
        resolveField(q.getRfield(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        resolveField(q.getField(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        resolveField(q.getField(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        resolveField(q.getField(),context,q);
        resolveField(q.getRfield(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        resolveField(q.getArray(),context,q);
        return q;
    }

    @Override
    protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        return super.itrUnaryLogicalExpression(q,context);
    }

    @Override
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        return super.itrNaryLogicalExpression(q,context);
    }

    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        return super.itrArrayMatchExpression(q,context);
    }

}
