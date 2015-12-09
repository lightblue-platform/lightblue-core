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
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.CopyOnWriteIterator;

/**
 * Rewrite queries so that any clause referring to a field outside the
 * current entity is removed from the query, and any clause comparing
 * a field in this entity with a field in another entity is bound to a
 * slot.
 *
 * It is not possible to rewrite a query clause referring to a field
 * in another entity. For instance a query of the form x=1 would
 * become meaningless, as 'x' has no value while evaluating this
 * entity. So the clause is removed from the query, and replaced with
 * a placeholder.
 */
public class RewriteQuery extends QueryIterator {

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
    
    /**
     * The root composite metadata
     */
    private final CompositeMetadata root;

    /**
     * The entity for which the query is being rewritten
     */
    private final CompositeMetadata currentEntity;

    private final List<QueryFieldInfo> fieldInfo;

    private final List<FieldBinding> bindings=new ArrayList<>();

    public RewriteQuery(CompositeMetadata root,
                        CompositeMetadata currentEntity,
                        List<QueryFieldInfo> fieldInfo) {
        this.root=root;
        this.currentEntity=currentEntity;
        this.fieldInfo=fieldInfo;
    }

    public List<FieldBinding> getBindings() {
        return bindings;
    }

    /**
     * Searches for the field info for a field in the given clase. The
     * fieldInfo is looked up in fieldInfo list. Object reference
     * equivalence is used to compare query clauses to find out the
     * field information, so the same query clauses that used to build
     * the fieldInfo list must be used here.
     */
    private QueryFieldInfo findFieldInfo(Path field,QueryExpression clause) {
        for(QueryFieldInfo fi:fieldInfo) {
            if(fi.getClause()==clause)
                if(fi.getFieldNameInClause().equals(field))
                    return fi;
        }
        throw Error.get(AssocConstants.ERR_REWRITE,field.toString()+"@"+clause.toString());
    }

    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        QueryFieldInfo qfi=findFieldInfo(q.getField(),q);
        if(qfi.getFieldEntity()!=currentEntity) {
            return new TruePH();
        } else {
            if(qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()))
                return q;
            else
                return new ValueComparisonExpression(qfi.getEntityRelativeFieldName(),q.getOp(),q.getRvalue());
        }
    }
    
    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        QueryFieldInfo qfi=findFieldInfo(q.getField(),q);
        if(qfi.getFieldEntity()!=currentEntity) {
            return new TruePH();
        } else {
            if(qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()))
                return q;
            else
                return new RegexMatchExpression(qfi.getEntityRelativeFieldName(),
                                                q.getRegex(),
                                                q.isCaseInsensitive(),
                                                q.isMultiline(),
                                                q.isExtended(),
                                                q.isDotAll());
        }
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        QueryFieldInfo qfi=findFieldInfo(q.getField(),q);
        if(qfi.getFieldEntity()!=currentEntity) {
            return new TruePH();
        } else {
            if(qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()))
                return q;
            else
                return new NaryValueRelationalExpression(qfi.getEntityRelativeFieldName(),q.getOp(),q.getValues());
        }
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        QueryFieldInfo qfi=findFieldInfo(q.getArray(),q);
        if(qfi.getFieldEntity()!=currentEntity) {
            return new TruePH();
        } else {
            if(qfi.getFieldNameInClause().equals(qfi.getEntityRelativeFieldName()))
                return q;
            else
                return new ArrayContainsExpression(qfi.getEntityRelativeFieldName(),q.getOp(),q.getValues());
        }
    }

    @Override
    public QueryExpression iterate(QueryExpression q,Path context) {
        // Don't send classes the base iterator doesn't recognize
        if(q instanceof TruePH || q instanceof FalsePH)
            return q;
        else
            return super.iterate(q,context);
    }

    /**
     * Binds the given field to a value.
     */
    private BoundValue bind(QueryFieldInfo fi) {
        BoundValue value=new BoundValue();
        bindings.add(new ValueBinding(fi,value));
        return value;
    }

    /**
     * Binds the given field to a list of values
     */
    private BoundValueList bindList(QueryFieldInfo fi) {
        BoundValueList value=new BoundValueList();
        bindings.add(new ListBinding(fi,value));
        return value;
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
     * If both f1 and f2 are in the current entity, the query is returned unmodified.
     *
     * If f1 is in current entity, but f2 is in a different entity, then the query is rewritten as:
     * <pre>
     *    { field: f1, op:'=', rvalue: <value> }
     * </pre>
     * and f2 is bound. 
     *
     * If f2 is in current entity, but f1 is in a different entity, then the query is rewritten as:
     * <pre>
     *   { field: f2, op: '=', rvalue: <value> }
     * </pre>
     * and f1 is bound. Also, the operator is inverted (i.e. If >, it is converted to <, etc.).
     *
     * If both f1 and f2 are not in the current entity, a placeholder for TRUE is returned.
     * 
     */
    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        QueryFieldInfo lfi=findFieldInfo(q.getField(),q);
        QueryFieldInfo rfi=findFieldInfo(q.getRfield(),q);
        if(lfi.getFieldEntity()==currentEntity) {
            if(rfi.getFieldEntity()!=currentEntity) {
                BoundValue value=bind(rfi);
                return new ValueComparisonExpression(lfi.getEntityRelativeFieldName(),q.getOp(),value);
            } else {
                return q;
            }
        } else if(rfi.getFieldEntity()==currentEntity) {
            BoundValue value=bind(lfi);
            return new ValueComparisonExpression(rfi.getEntityRelativeFieldName(),q.getOp().invert(),value);
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
     * If both f1 and f2 are in the current entity, the query is returned unmodified.
     *
     * If f1 is in current entity, but f2 is in a different entity, then the query is rewritten as:
     * <pre>
     *    { field: f1, op:'$in', rvalue: <value> }
     * </pre>
     * and f2 is bound to a list. 
     *
     * If f2 is in current entity, but f1 is in a different entity, then the query is rewritten as:
     * <pre>
     *   { field: f2, op: '$all', rvalue: [<value>] }
     * </pre>
     * and f1 is bound. 
     *
     * If both f1 and f2 are not in the current entity, a placeholder for TRUE is returned.
     * 
     */
    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        QueryFieldInfo lfi=findFieldInfo(q.getField(),q);
        QueryFieldInfo rfi=findFieldInfo(q.getRfield(),q);
        if(lfi.getFieldEntity()==currentEntity) {
            if(rfi.getFieldEntity()!=currentEntity) {
                BoundValueList value=bindList(rfi);
                return new NaryValueRelationalExpression(lfi.getEntityRelativeFieldName(),q.getOp(),value);
            } else {
                return q;
            }
        } else if(rfi.getFieldEntity()==currentEntity) {
            BoundValue value=bind(lfi);
            List<Value> list=new ArrayList<>(1);
            list.add(value);
            return new ArrayContainsExpression(rfi.getEntityRelativeFieldName(),
                                               q.getOp()==NaryRelationalOperator._in?ContainsOperator._all:ContainsOperator._none, 
                                               list);
        } else {
            return new TruePH();
        }
    }

    /**
     * If the enclosed query is a placeholder (TruePH or FalsePH), it negates the placeholder, 
     * otherwise, the query remains as is
     */
    @Override
    protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        UnaryLogicalExpression nq=(UnaryLogicalExpression)super.itrUnaryLogicalExpression(q,context);
        if(nq.getQuery() instanceof TruePH)
            return new FalsePH();
        else if(nq.getQuery() instanceof FalsePH)
            return new TruePH();
        else
            return nq;
    }

    /**
     * All TruePH placeholders are removed from an AND expression.
     *
     * All FalsePH placeholders are removed from an OR expression.
     *
     * If an AND expression contains a FalsePH, the expression is replaced with a FalsePH
     *
     * If an OR expression contains a TruePH, the expression is replaced with a TruePH
     */
    @Override
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        NaryLogicalExpression nq=(NaryLogicalExpression)super.itrNaryLogicalExpression(q,context);
        CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<QueryExpression>(nq.getQueries());
        while (itr.hasNext()) {
            QueryExpression nestedq = itr.next();
            if(q.getOp()==NaryLogicalOperator._and) {
                if(nestedq instanceof TruePH) {
                    itr.remove();
                } else if(nestedq instanceof FalsePH) {
                    return new FalsePH();
                } 
            } else {
                if(nestedq instanceof TruePH) {
                    return new TruePH();
                } else if(nestedq instanceof FalsePH) {
                    itr.remove();
                } 
            }
        }
        if(itr.isCopied()) {
            List<QueryExpression> newList=itr.getCopiedList();
            if(newList.size()==1)
                return newList.get(0);
            else
                return new NaryLogicalExpression(q.getOp(),newList);
        } else {
            return nq;
        }
    }

    /**
     * Several possibilities exist when rewriting an array query.
     *
     * The array field itself may be pointing to a parent entity.
     *
     * If the nested query contains a placeholder, the query is
     * replaced with that. Otherwise, the query remains as is
     */
    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        ArrayMatchExpression e=(ArrayMatchExpression)super.itrArrayMatchExpression(q,context);
        QueryExpression em=e.getElemMatch();
        if(em instanceof TruePH||
           em instanceof FalsePH)
            return em;
        else
            return e;
    }
}
