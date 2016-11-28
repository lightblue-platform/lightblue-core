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
package com.redhat.lightblue.assoc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.CopyOnWriteIterator;

/**
 * Rewrite queries so that any clause referring to a field outside the current
 * entity is removed from the query, and any clause comparing a field in this
 * entity with a field in another entity is bound to a slot.
 *
 * It is not possible to rewrite a query clause referring to a field in another
 * entity. For instance a query of the form x=1 would become meaningless, as 'x'
 * has no value while evaluating this entity. So the clause is removed from the
 * query, and replaced with a placeholder.
 */
public class RewriteQuery extends QueryIterator {

    /**
     * The entity for which the query is being rewritten
     */
    private final CompositeMetadata currentEntity;

    /**
     * Placeholder that always evaluates to true
     */
    public static final class TruePH extends QueryExpression {
        @Override
        public JsonNode toJson() {
            return JsonNodeFactory.instance.booleanNode(true);
        }
    }

    /**
     * Placeholder that always evaluates to false
     */
    public static final class FalsePH extends QueryExpression {
        @Override
        public JsonNode toJson() {
            return JsonNodeFactory.instance.booleanNode(false);
        }
    }

    public static final class RewriteQueryResult {
        public final QueryExpression query;
        public final List<BoundObject> bindings;

        public RewriteQueryResult(QueryExpression query, List<BoundObject> bindings) {
            this.query = query;
            this.bindings = bindings;
        }
    }

    /**
     * Construct a class to rewrite queries to retrieve an entity, potentially
     * evaluated at another entity
     *
     * @param root The root entity
     * @param currentEntity The query will be rewritten relative to this entity
     *
     * The root entity is the entity that will be retrieved ultimately. That
     * entity may contain associations to other entities, and the association
     * queries need to be rewritten based on the query plan node they are
     * evaluated. The currentEntity gives that node relative to which the
     * queries will be rewritten.
     */
    public RewriteQuery(CompositeMetadata root,
                        CompositeMetadata currentEntity) {
        this.currentEntity = currentEntity;
    }

    /**
     * Rewrites a query using field information obtained from AnalyzeQuery
     *
     * @return Returns a result object containing the new rewritten query, and
     * the field bindings that will be set to values from already retrieved
     * documents.
     */
    public RewriteQueryResult rewriteQuery(QueryExpression q, List<QueryFieldInfo> fieldInfo) {
        RewriteQueryImpl w = new RewriteQueryImpl(fieldInfo);
        QueryExpression newq = w.iterate(q);
        return new RewriteQueryResult(newq, w.bindings);
    }

    private class RewriteQueryImpl extends QueryIterator {

        // Set by rewriteQuery before calling
        private final List<QueryFieldInfo> fieldInfo;
        // Set by rewriteQuery before calling
        private final List<BoundObject> bindings = new ArrayList<>(16);

        // This is prefixed to all field names
        private Path nestedFieldPrefix = Path.EMPTY;
        // This is the field info the the field of the current array context
        private QueryFieldInfo contextField=null;

        /**
         * Rewrites a query for a query plan node
         *
         * @param root The root entity metadata
         * @param currentEntity The entity for the query plan node for which the
         * query is being rewritten
         * @param fieldInfo Query field
         */
        public RewriteQueryImpl(List<QueryFieldInfo> fieldInfo) {
            this.fieldInfo = fieldInfo;
        }

        /**
         * Searches for the field info for a field in the given clase. The
         * fieldInfo is looked up in fieldInfo list. Object reference
         * equivalence is used to compare query clauses to find out the field
         * information, so the same query clauses that used to build the
         * fieldInfo list must be used here.
         */
        private QueryFieldInfo findFieldInfo(Path field, QueryExpression clause) {
            for (QueryFieldInfo fi : fieldInfo) {
                if (fi.getClause() == clause) {
                    if (fi.getFieldNameInClause().equals(field)) {
                        return fi;
                    }
                }
            }
            throw Error.get(AssocConstants.ERR_REWRITE, field.toString() + "@" + clause.toString());
        }

        private Path addPrefix(Path fieldName) {
            if (nestedFieldPrefix.isEmpty()) {
                return fieldName;
            } else {
                return new Path(nestedFieldPrefix, fieldName);
            }
        }

        /**
         * Remove the context prefix from the field. If the field is under an array elemMatch, and if the field
         * shares the same prefix as the array field, remove that prefix. This is only useful if the field
         * and the array are in the same entity
         */
        private Path removeContext(CompositeMetadata fieldEntity,Path fieldName) {
            if(contextField!=null&&fieldEntity==contextField.getFieldEntity()) {
                Path prefix=new Path(contextField.getEntityRelativeFieldName(),Path.ANYPATH);
                if(fieldName.numSegments()>=prefix.numSegments()) {
                    Path fieldPrefix=fieldName.prefix(prefix.numSegments()); // Also include the *
                    if(fieldPrefix.equals(prefix)) {
                        // Remove fieldPrefix
                        Path relativeFieldName=fieldName.suffix(-prefix.numSegments());
                        if(relativeFieldName.isEmpty())
                            relativeFieldName=new Path(Path.THIS);
                        return relativeFieldName;
                    }
                }
            } 
            return fieldName;
        }

        @Override
        protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
            QueryFieldInfo qfi = findFieldInfo(q.getField(), q);
            if (qfi.getFieldEntity() != currentEntity) {
                return new TruePH();
            } else if (qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()) && nestedFieldPrefix.isEmpty()) {
                return q;
            } else {
                return new ValueComparisonExpression(addPrefix(qfi.getEntityRelativeFieldName()), q.getOp(), q.getRvalue());
            }
        }

        @Override
        protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
            QueryFieldInfo qfi = findFieldInfo(q.getField(), q);
            if (qfi.getFieldEntity() != currentEntity) {
                return new TruePH();
            } else if (qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()) && nestedFieldPrefix.isEmpty()) {
                return q;
            } else {
                return new RegexMatchExpression(addPrefix(qfi.getEntityRelativeFieldName()),
                        q.getRegex(),
                        q.isCaseInsensitive(),
                        q.isMultiline(),
                        q.isExtended(),
                        q.isDotAll());
            }
        }

        @Override
        protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
            QueryFieldInfo qfi = findFieldInfo(q.getField(), q);
            if (qfi.getFieldEntity() != currentEntity) {
                return new TruePH();
            } else if (qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()) && nestedFieldPrefix.isEmpty()) {
                return q;
            } else {
                return new NaryValueRelationalExpression(addPrefix(qfi.getEntityRelativeFieldName()), q.getOp(), q.getValues());
            }
        }

        @Override
        protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
            QueryFieldInfo qfi = findFieldInfo(q.getArray(), q);
            if (qfi.getFieldEntity() != currentEntity) {
                return new TruePH();
            } else if (qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()) && nestedFieldPrefix.isEmpty()) {
                return q;
            } else {
                return new ArrayContainsExpression(addPrefix(qfi.getEntityRelativeFieldName()), q.getOp(), q.getValues());
            }
        }

        @Override
        public QueryExpression iterate(QueryExpression q, Path context) {
            // Don't send classes the base iterator doesn't recognize
            if (q instanceof TruePH || q instanceof FalsePH) {
                return q;
            } else {
                return super.iterate(q, context);
            }
        }

        /**
         * Binds the given field to a value.
         */
        private Value bind(QueryFieldInfo fi) {
            BoundValue b = new BoundValue(fi);
            bindings.add(b);
            return b;
        }

        /**
         * Binds the given field to a list of values
         */
        private List<Value> bindList(QueryFieldInfo fi) {
            BoundList b = new BoundList(fi);
            bindings.add(b);
            return b;
        }

        /**
         * Rewrites a field comparison
         *
         * A field comparison is of the form:
         * <pre>
         *  { field: f1, op:'=', rfield: f2 }
         * </pre>
         *
         * There are four possible ways this can be rewritten:
         *
         * If both f1 and f2 are in the current entity, the query is returned
         * unmodified.
         *
         * If f1 is in current entity, but f2 is in a different entity, then the
         * query is rewritten as:
         * <pre>
         *    { field: f1, op:'=', rvalue: <value> }
         * </pre> and f2 is bound.
         *
         * If f2 is in current entity, but f1 is in a different entity, then the
         * query is rewritten as:
         * <pre>
         *   { field: f2, op: '=', rvalue: <value> }
         * </pre> and f1 is bound. Also, the operator is inverted (i.e. If >, it
         * is converted to <, etc.).
         *
         * If both f1 and f2 are not in the current entity, a placeholder for
         * TRUE is returned.
         *
         */
        @Override
        protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
            QueryFieldInfo lfi = findFieldInfo(q.getField(), q);
            QueryFieldInfo rfi = findFieldInfo(q.getRfield(), q);
            if (lfi.getFieldEntity() == currentEntity) {
                if (rfi.getFieldEntity() != currentEntity) {
                    Value value = bind(rfi);
                    return new ValueComparisonExpression(removeContext(lfi.getFieldEntity(),addPrefix(lfi.getEntityRelativeFieldName())), q.getOp(), value);
                } else if (nestedFieldPrefix.isEmpty()) {
                    return q;
                } else {
                    return new FieldComparisonExpression(addPrefix(q.getField()), q.getOp(), addPrefix(q.getRfield()));
                }
            } else if (rfi.getFieldEntity() == currentEntity) {
                Value value = bind(lfi);
                return new ValueComparisonExpression(removeContext(rfi.getFieldEntity(),addPrefix(rfi.getEntityRelativeFieldName())), q.getOp().invert(), value);
            } else {
                return new TruePH();
            }
        }

        /**
         * Rewrites an nary- field comparison
         *
         * An nary- field comparison is of the form:
         * <pre>
         *  { field: f1, op:'$in', rfield: f2 }
         * </pre>
         *
         * There are four possible ways this can be rewritten:
         *
         * If both f1 and f2 are in the current entity, the query is returned
         * unmodified.
         *
         * If f1 is in current entity, but f2 is in a different entity, then the
         * query is rewritten as:
         * <pre>
         *    { field: f1, op:'$in', rvalue: <value> }
         * </pre> and f2 is bound to a list.
         *
         * If f2 is in current entity, but f1 is in a different entity, then the
         * query is rewritten as:
         * <pre>
         *   { field: f2, op: '$all', rvalue: [<value>] }
         * </pre> and f1 is bound.
         *
         * If both f1 and f2 are not in the current entity, a placeholder for
         * TRUE is returned.
         *
         */
        @Override
        protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
            QueryFieldInfo lfi = findFieldInfo(q.getField(), q);
            QueryFieldInfo rfi = findFieldInfo(q.getRfield(), q);
            if (lfi.getFieldEntity() == currentEntity) {
                if (rfi.getFieldEntity() != currentEntity) {
                    List<Value> value = bindList(rfi);
                    return new NaryValueRelationalExpression(removeContext(lfi.getFieldEntity(),addPrefix(lfi.getEntityRelativeFieldName())), q.getOp(), value);
                } else if (nestedFieldPrefix.isEmpty()) {
                    return q;
                } else {
                    return new NaryFieldRelationalExpression(removeContext(lfi.getFieldEntity(),addPrefix(q.getField())), q.getOp(),
                                                             removeContext(rfi.getFieldEntity(),addPrefix(q.getRfield())));
                }
            } else if (rfi.getFieldEntity() == currentEntity) {
                Value value = bind(lfi);
                List<Value> list = new ArrayList<>(1);
                list.add(value);
                return new ArrayContainsExpression(removeContext(rfi.getFieldEntity(),addPrefix(rfi.getEntityRelativeFieldName())),
                        q.getOp() == NaryRelationalOperator._in ? ContainsOperator._all : ContainsOperator._none,
                        list);
            } else {
                return new TruePH();
            }
        }

        /**
         * If the enclosed query is a placeholder (TruePH or FalsePH), it
         * negates the placeholder, otherwise, the query remains as is
         */
        @Override
        protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
            UnaryLogicalExpression nq = (UnaryLogicalExpression) super.itrUnaryLogicalExpression(q, context);
            if (nq.getQuery() instanceof TruePH) {
                return new FalsePH();
            } else if (nq.getQuery() instanceof FalsePH) {
                return new TruePH();
            } else {
                return nq;
            }
        }

        /**
         * All TruePH placeholders are removed from an AND expression.
         *
         * All FalsePH placeholders are removed from an OR expression.
         *
         * If an AND expression contains a FalsePH, the expression is replaced
         * with a FalsePH
         *
         * If an OR expression contains a TruePH, the expression is replaced
         * with a TruePH
         */
        @Override
        protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
            NaryLogicalExpression nq = (NaryLogicalExpression) super.itrNaryLogicalExpression(q, context);
            CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<QueryExpression>(nq.getQueries());
            while (itr.hasNext()) {
                QueryExpression nestedq = itr.next();
                if (q.getOp() == NaryLogicalOperator._and) {
                    if (nestedq instanceof TruePH) {
                        itr.remove();
                    } else if (nestedq instanceof FalsePH) {
                        return new FalsePH();
                    }
                } else if (nestedq instanceof TruePH) {
                    return new TruePH();
                } else if (nestedq instanceof FalsePH) {
                    itr.remove();
                }
            }
            QueryExpression ret;
            if (itr.isCopied()) {
                List<QueryExpression> newList = itr.getCopiedList();
                if (newList.size() == 0) {
                    ret=new TruePH();
                } else if (newList.size() == 1) {
                    ret=newList.get(0);
                } else {
                    ret=new NaryLogicalExpression(q.getOp(), newList);
                }
            } else {
                ret=nq;
            }
            return ret;
        }

        /**
         * Several possibilities exist when rewriting an array query.
         *
         * The array field itself may be pointing to a different entity than the
         * currentEntity. This includes the case where the array is the
         * reference field itself. If this is the case, this is no longer an
         * array elem match expression. The nested query is moved outside and
         * rewritten.
         *
         * The array field itself may be pointing to an array in currentEntity.
         * In this case, the array field is rewritten as the local field, and
         * the nested query is rewritten recursively.
         *
         * If the nested query contains a placeholder, the query is replaced
         * with that. Otherwise, the query remains as is
         */
        @Override
        protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
            Path newContext=context.equals(Path.EMPTY)?new Path(q.getArray(),Path.ANYPATH):
                new Path(context,new Path(q.getArray(),Path.ANYPATH));
            QueryFieldInfo oldContextField=contextField;
            QueryFieldInfo qfi = findFieldInfo(q.getArray(), q);
            try {
                if (qfi.getFieldEntity() == currentEntity) {
                    if (qfi.getFieldMd() instanceof ResolvedReferenceField) {
                        // The array is pointing to a reference field
                        // This could be a simple reference, like {array:ref, elemMatch: Q }
                        // In that case, the return value is Q
                        // Example: in: { array: ref, elemMatch:{ field:_id, op:$eq, rfield: $parent.ref_id} }
                        //         out: { field: ref_id, op: $eq, rvalue: <_id value> }
                        //
                        // This could also be a nested array reference, like {array: x.*.ref, elemMatch: Q }
                        // Example: in: { array: x.*.obj.ref, elemMatch: { field: _id, op:$eq, rfield: $parent.ref_id } }
                        //         out: { array: x, elemMatch: { field: obj.ref_id, op: $eq, rvalue: < _id value> } }
                        
                        // Find the closest array containing the reference
                        Path arrName = qfi.getEntityRelativeFieldName();
                        int lastAny = -1;
                        int n = arrName.numSegments();
                        for (int i = 0; i < n; i++) {
                            if (arrName.tail(i).equals(Path.ANY)) {
                                lastAny = i;
                                break;
                            }
                        }
                        if (lastAny == -1) {
                            // Reference field is not in an array.
                            QueryExpression em = iterate(q.getElemMatch(), newContext);
                            return em;
                        } else {
                            // If arrName is x.*.ref, lastAny will be 1, so a prefix of -2 will be x
                            Path closestArray = arrName.prefix(-(lastAny + 1));
                            // Any remaining part between the closest array and reference should be a prefix of all the nested fields
                            // For instance, if arrName is x.*.obj.ref, all fields should be prefixed by obj
                            Path oldPrefix = nestedFieldPrefix;
                            nestedFieldPrefix = arrName.suffix(-(closestArray.numSegments() + 1)).prefix(-1);
                            QueryExpression em = iterate(q.getElemMatch(), newContext);
                            nestedFieldPrefix = oldPrefix;
                            if (em instanceof TruePH
                                || em instanceof FalsePH) {
                                return em;
                            } else {
                                // Update context only if we're writing a new arraymatch expression
                                contextField=qfi;
                                return new ArrayMatchExpression(addPrefix(closestArray), em);
                            }
                        }
                    } else if (currentEntity.getParent() != null) {
                        // The array is pointing to a field in current entity, and current entity is not the root
                        // Remove any parts of the array field before the reference
                        Path relative = qfi.getEntityRelativeFieldName();
                        // Update context only if we're writing a new arraymatch expression
                       contextField=qfi;
                        return new ArrayMatchExpression(relative, iterate(q.getElemMatch(), newContext));
                    } else {
                        QueryExpression em = iterate(q.getElemMatch(), newContext);
                        if (em instanceof TruePH
                            || em instanceof FalsePH) {
                            return em;
                        } else {
                            // Update context only if we're writing a new arraymatch expression
                           contextField=qfi;
                            return new ArrayMatchExpression(addPrefix(q.getArray()), em);
                        }
                    }
                } else {
                    QueryExpression em = iterate(q.getElemMatch(), newContext);
                    return em;
                }
            } finally {
                contextField=oldContextField;
            }
        }
    }
}
