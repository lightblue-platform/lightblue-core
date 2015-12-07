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
    private static final class TruePH extends QueryExpression {
        @Override
        public JsonNode toJson() {
            return JsonNodeFactory.instance.booleanNode(true);
        }
    }

    /**
     * Placeholder that always evaluates to false
     */
    private static final class FalsePH extends QueryExpression {
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

    /**
     * Handles query rewriting for single field expression. If the
     * field belongs to the current entity, the query expression is
     * used as is. If the field belongs to a different entity, there
     * is no evaluation we can do while retrieving the current entity,
     * so we assume the clause is true for whatever entity it is
     * referring to. This is done by returning a placeholder for TRUE
     * value.
     */
    private QueryExpression singleFieldExpression(Path field,QueryExpression q) {
        QueryFieldInfo qfi=findFieldInfo(field,q);
        if(qfi.getFieldEntity()!=currentEntity) {
            return new TruePH();
        } else {
            return q;
        }
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        return singleFieldExpression(q.getField(),q);
    }

    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        return singleFieldExpression(q.getField(),q);
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        return singleFieldExpression(q.getField(),q);
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        return singleFieldExpression(q.getArray(),q);
    }

    private BoundValue bind(QueryFieldInfo fi) {
        BoundValue value=new BoundValue();
        bindings.add(new ValueBinding(fi,value));
        return value;
    }

    private BoundValueList bindList(QueryFieldInfo fi) {
        BoundValueList value=new BoundValueList();
        bindings.add(new ListBinding(fi,value));
        return value;
    }

    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        QueryFieldInfo lfi=findFieldInfo(q.getField(),q);
        QueryFieldInfo rfi=findFieldInfo(q.getRfield(),q);
        if(lfi.getFieldEntity()==currentEntity) {
            if(rfi.getFieldEntity()!=currentEntity) {
                BoundValue value=bind(rfi);
                return new ValueComparisonExpression(q.getField(),q.getOp(),value);
            } else {
                return q;
            }
        } else if(rfi.getFieldEntity()==currentEntity) {
            BoundValue value=bind(lfi);
            return new ValueComparisonExpression(q.getRfield(),q.getOp().invert(),value);
        } else {
            return new TruePH();
        }
    }

    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        QueryFieldInfo lfi=findFieldInfo(q.getField(),q);
        QueryFieldInfo rfi=findFieldInfo(q.getRfield(),q);
        if(lfi.getFieldEntity()==currentEntity) {
            if(rfi.getFieldEntity()!=currentEntity) {
                BoundValueList value=bindList(rfi);
                return new NaryValueRelationalExpression(q.getField(),q.getOp(),value);
            } else {
                return q;
            }
        } else if(rfi.getFieldEntity()==currentEntity) {
            BoundValue value=bind(lfi);
            List<Value> list=new ArrayList<>(1);
            list.add(value);
            return new ArrayContainsExpression(q.getRfield(), q.getOp()==NaryRelationalOperator._in?
                                               ContainsOperator._all:ContainsOperator._none, 
                                               list);
        } else {
            return new TruePH();
        }
    }

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
    
    @Override
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        NaryLogicalExpression nq=(NaryLogicalExpression)super.itrNaryLogicalExpression(q,context);
        CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<QueryExpression>(nq.getQueries());
        while (itr.hasNext()) {
            QueryExpression nestedq = itr.next();
            QueryExpression newq = iterate(nestedq, context);
            if(q.getOp()==NaryLogicalOperator._and) {
                if(newq instanceof TruePH) {
                    itr.remove();
                } else if(newq instanceof FalsePH) {
                    return new FalsePH();
                } else {
                    itr.set(newq);
                }
            } else {
                if(newq instanceof TruePH) {
                    return new TruePH();
                } else if(newq instanceof FalsePH) {
                    itr.remove();
                } else {
                    itr.set(newq);
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
            return q;
        }
    }

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
