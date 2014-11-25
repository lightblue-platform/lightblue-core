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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.query.FieldBinding;
import com.redhat.lightblue.query.QueryInContext;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.RelativeRewriteIterator;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.assoc.Conjunct;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.util.Path;

/**
 * This class represents a field binding interpreted based on the
 * metadata. It contains the original field binding information, the
 * field type information, and the field name relative to an entity in
 * a query plan that denotes the field name containing the value that
 * is bound in the field binding.
 *
 * For example:
 * <pre>
 *    query={'field':'field1','op':'=','rfield':'$parent.referencedField'}
 * </pre>
 * 
 * Assume <code>referencedField</code> is bound to a value in a
 * different entity. Then, <code>binding</code> contains the binding
 * information for <code>referencedField</code>, <code>type</code>
 * contains the type of <code>referencedField</code>, and valueField
 * contains <code>referencedField</code>, the field relative to the
 * entity it is contained in.
 */
public class ResolvedFieldBinding implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(ResolvedFieldBinding.class);
    
    private final FieldBinding binding;
    private final Type type;
    private final Path valueField;
    private final CompositeMetadata entity;

    /**
     * This class is used to return binding results. It contains the
     * bindings, and the query rewritten relative to the node the
     * query is bound,
     */
    public static final class BindResult {
        private final List<ResolvedFieldBinding> bindings;
        private final QueryExpression runExpression;

        private BindResult(List<ResolvedFieldBinding> bindings,
                           QueryExpression runExpression) {
            this.bindings=bindings;
            this.runExpression=runExpression;
        }

        private BindResult(QueryExpression runExpression) {
            this(null,runExpression);
        }
        
        public List<ResolvedFieldBinding> getBindings() {
            return bindings;
        }

        public QueryExpression getRelativeQuery() {
            return runExpression;
        }
    }

    /**
     * Construct a resolved field binding using the given binding and the root composite metadata
     */
    public ResolvedFieldBinding(FieldBinding b,
                                CompositeMetadata root) {
        this.binding=b;
        FieldTreeNode field=root.resolve(b.getField());
        if(field==null)
            throw new IllegalArgumentException("Cannot resolve "+b.getField());
        this.type=field.getType();
        this.entity=root.getEntityOfField(field);
        this.valueField=root.getEntityRelativeFieldName(field);
    }

    /**
     * Binds a list of clauses for execution at a particular query plan node
     *
     * @param clauses The query conjuncts that will be bound
     * @param atNode The query plan node at which the query will be run
     *
     * @return Returns the binding result containing the bindings, and
     * the query expression rewritten to be run at the given query
     * plan node. Returns null if there are no bindings or query.
     */
    public static BindResult bind(List<Conjunct> clauses,
                                  QueryPlanNode atNode,
                                  CompositeMetadata root) {
        BindResult ret;
        if(clauses!=null&&!clauses.isEmpty()) {
            QueryExpression query;
            if(clauses.size()==1) {
                query=clauses.get(0).getClause();
            } else {
                List<QueryExpression> cl=new ArrayList<>(clauses.size());
                for(Conjunct c:clauses)
                    cl.add(c.getClause());
                query=new NaryLogicalExpression(NaryLogicalOperator._and,cl);
            }

            LOGGER.debug("Resolving bindings for {}",query);
            List<QueryInContext> bindable=query.getBindableClauses();
            LOGGER.debug("Bindable clauses:{}",bindable);
            
            if(!bindable.isEmpty()) {
                LOGGER.debug("Building bind request");
                Set<Path> bindRequest=new HashSet<Path>();
                for(QueryInContext qic:bindable) {
                    FieldComparisonExpression fce=(FieldComparisonExpression)qic.getQuery();
                    // Find the field that refers to a node before
                    // the destination
                    Path lfield=new Path(qic.getContext(),fce.getField());
                    QueryPlanNode lfieldNode=null;
                    for(Conjunct c:clauses) {
                        lfieldNode=c.getFieldNode(lfield);
                        if(lfieldNode!=null)
                            break;
                    }
                    Path rfield=new Path(qic.getContext(),fce.getRfield());
                    QueryPlanNode rfieldNode=null;
                    for(Conjunct c:clauses) {
                        rfieldNode=c.getFieldNode(rfield);
                        if(rfieldNode!=null)
                            break;
                    }
                    LOGGER.debug("lfield={}, rfield={}",lfieldNode==null?null:lfieldNode.getName(),
                                 rfieldNode==null?null:rfieldNode.getName());
                    // If lfieldNode points to the destination node,
                    // rfieldNode points to an ancestor, or vice versa
                    if(lfieldNode.getName().equals(atNode.getName())) {
                        bindRequest.add(rfield);
                    } else {
                        bindRequest.add(lfield);
                    }
                }
                
                LOGGER.debug("Bind fields:{}",bindRequest);
                List<FieldBinding> fb=new ArrayList<>();
                QueryExpression boundQuery=query.bind(fb,bindRequest);
                List<ResolvedFieldBinding> bindings=new ArrayList<>();
                for(FieldBinding b:fb) {
                    bindings.add(new ResolvedFieldBinding(b,root));
                }
                LOGGER.debug("Bound query:{}",boundQuery);
                // If destination node is not the root node, we need to rewrite the query relative to that node
                // Otherwise, absolute query will work for the root node
                QueryExpression runExpression;
                if(atNode.getMetadata().getParent()==null) {
                    runExpression=boundQuery;
                } else {
                    runExpression=new RelativeRewriteIterator(new Path(atNode.getMetadata().getEntityPath(),
                                                                       Path.ANYPATH)).iterate(boundQuery);
                }
                LOGGER.debug("Run expression:{}",runExpression);
                ret=new BindResult(bindings,runExpression);
            } else {
                QueryExpression runExpression;
                if(atNode.getMetadata().getParent()==null) {
                    runExpression=query;
                } else {
                    runExpression=new RelativeRewriteIterator(new Path(atNode.getMetadata().getEntityPath(),
                                                                       Path.ANYPATH)).iterate(query);
                }
                ret=new BindResult(runExpression);
            }
        } else
            ret=null;
        return ret;
    }

    public static void refresh(List<ResolvedFieldBinding> bindings,QueryPlanDoc doc) {
        for(ResolvedFieldBinding binding:bindings) {
            binding.refresh(doc);
        }
    }

    /**
     * Attempts to refresh this binding starting at the given document, and ascending to its parents
     */
    public boolean refresh(QueryPlanDoc doc) {
        if(doc.getMetadata()==entity) {
            JsonNode valueNode=doc.getDoc().get(valueField);
            if(valueNode==null)
                binding.getValue().setValue(null);
            else
                binding.getValue().setValue(type.fromJson(valueNode));
            return true;
        } else {
            for(QueryPlanDoc parent: doc.getParents()) {
                if(refresh(parent))
                    return true;
            }
            return false;
        }
    }
}
