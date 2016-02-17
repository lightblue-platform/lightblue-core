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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.query.FieldBinding;
import com.redhat.lightblue.query.ValueBinding;
import com.redhat.lightblue.query.ListBinding;
import com.redhat.lightblue.query.QueryInContext;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryFieldRelationalExpression;
import com.redhat.lightblue.query.RelativeRewriteIterator;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.MapQueryFieldsIterator;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.KeyValueCursor;

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
 * entity it is contained in. If the binding is a list binding, then
 * type keeps the type of the list element.
 */
public class ResolvedFieldBinding implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(ResolvedFieldBinding.class);
    
    private final FieldBinding binding;
    /**
     * Value type of the bound field
     */
    private final Type type;
    /**
     * The entity relative field name of the field
     */
    private final Path valueField;
    /**
     * The entity of the bound field
     */
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
    public ResolvedFieldBinding(Path fieldName,
                                FieldBinding b,
                                CompositeMetadata emd) {
        this.binding=b;
        FieldTreeNode field=emd.resolve(fieldName);
        if(field==null)
            throw Error.get(AssocConstants.ERR_CANNOT_FIND_FIELD,b.getField().toString());
        if(b instanceof ListBinding) {
            if(field instanceof ArrayField) {
                this.type=((ArrayField)field).getElement().getType();
            } else
                throw Error.get(AssocConstants.ERR_ARRAY_EXPECTED,b.getField().toString());
        } else {
            this.type=field.getType();
        }
        this.entity=emd.getEntityOfField(field);
        this.valueField=emd.getEntityRelativeFieldName(field);
    }

    public static class RelativeRewriter extends MapQueryFieldsIterator {
        private final Conjunct conjunct;
        private final CompositeMetadata root;
        private final CompositeMetadata thisMd;
        // Once passed through an elemMatch expression
        // this can be set to a nonzero value to only include a
        // certain suffix of field names
        private int relativeSuffixLength=0;

        /**
         * @param c The query to rewrite
         * @param root The root metadata
         * @param thisMd Ths metadata of the node relative to which the query will be rewritten
         * @param requestQuery if the query is a request query, this
         * is true. If the query is associated to a query plan node,
         * this is false.
         */
        public RelativeRewriter(Conjunct c,CompositeMetadata root,CompositeMetadata thisMd) {
            this.conjunct=c;
            this.root=root;
            this.thisMd=thisMd;
        }

        /**
         * Array match expressions require special treatment when converting a query to relative.
         * Relative query may require removing the array clause to open up the elemMatch clause.
         * This is the case if, for instance, the clause is { array: a.b.c, elemMatch:{ } }, and 
         * a.b.c is a reference to the current entity.
         */
        @Override
        protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
            // When this is called, any field in the query that belongs to a different
            // entity should already be bound. So, (array + field) always points to the current
            // entity for which this query is being rewritten. How the array field defined
            // determines the outcome. If array field points to an array in this entity, we 
            // rewrite the query by getting a suffix of the array. If array field points to
            // an array in a parent entity, or if the array field points to the
            // reference field, this is no longer an elemMatch query. We return the
            // nested query instead.
            LOGGER.debug("Rewriting {}",q);
            Path arrayName=context.isEmpty()?q.getArray():new Path(context,q.getArray());
            // Find a field with the array name as the prefix
            ResolvedFieldInfo arrayDescendant=null;
            for(ResolvedFieldInfo rfi:conjunct.getFieldInfo()) {
                Path p=rfi.getFieldName();
                if(arrayName.matchingPrefix(p)) {
                    arrayDescendant=rfi;
                    break;
                }
            }
            
            if(arrayDescendant==null)
                throw new RuntimeException("Unknown field:"+arrayName);
            LOGGER.debug("Array descendant:{}",arrayDescendant);
            // arrayDescendant is a field in the query with the array being the prefix. From here, we can figure out which entity array belongs to
            CompositeMetadata fmd=arrayDescendant.getFieldEntityCompositeMetadata();
            while(fmd!=null&&fmd.getEntityPath().numSegments()>arrayName.numSegments())
                fmd=fmd.getParent();
            CompositeMetadata arrayMd=fmd;
            LOGGER.debug("Array metadata:{}",(arrayMd == null ? null : arrayMd.getName()));

            Path absoluteArray;
            if(conjunct.isRequestQuery()) {
                absoluteArray=arrayName;
            } else {
                absoluteArray=(arrayMd!=null&&arrayMd.getParent()!=null)?new Path(arrayMd.getEntityPath(),new Path(Path.ANYPATH,arrayName)):arrayName;
            }
            LOGGER.debug("Absolute array:{}",absoluteArray);
            if(arrayMd==thisMd&&!absoluteArray.equals(thisMd.getEntityPath())) {
                // Convert array to relative, and return an elem match query
                Path arrayPath;
                if(arrayMd.getParent()==null) {
                    arrayPath=absoluteArray;
                } else {
                    arrayPath=absoluteArray.suffix(-(arrayMd.getEntityPath().numSegments()+1));
                }
                QueryExpression newq = iterate(q.getElemMatch(),
                                               new Path(new Path(context, arrayPath), Path.ANYPATH));
                
                return new ArrayMatchExpression(arrayPath,newq);
            } else {
                // This is no longer an array match expression.
                // We need to find out where the entity boundary is crossed
                // So if the fields are like this, where c is a reference:
                //   a . b . c | d . e . f
                // and if the query is something like:
                //   { array: a.b, elemMatch{ field: c.d.e } }
                // then, the relative query is
                //    {field: d.e }
                // So: first find the length of the portion remaining in the other entity
                int delta=thisMd.getEntityPath().numSegments() - absoluteArray.numSegments();
                // We need to cut a prefix of 'delta' length from all fields
                relativeSuffixLength+=delta;
                QueryExpression ret=iterate(q.getElemMatch(),context);
                relativeSuffixLength-=delta;
                return ret;
            }
        }

        
        @Override
        protected Path map(Path p) {
            // Get a relative suffix
            Path suffix=relativeSuffixLength>0?p.suffix(-relativeSuffixLength):p;
            Path x=conjunct.mapOriginalFieldName(suffix);
            // If path is unchanged, return null, so the clause is kept unmodified
            if(x==p)
                return null;
            else
                return x;  
        }
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
        BindResult ret = null;
        if(clauses!=null&&!clauses.isEmpty()) {
            List<ResolvedFieldBinding> bindings = new ArrayList<>();
            List<QueryExpression> boundQueries=new ArrayList<>();
            for(Conjunct conjunct:clauses) {
                LOGGER.debug("Resolving bindings for {}",conjunct);
                QueryExpression query=conjunct.getClause();
                List<QueryInContext> bindable=query.getBindableClauses();
                LOGGER.debug("Bindable clauses:{}",bindable);

                // default the bound query to input query
                QueryExpression boundQuery = query;
                
                if(!bindable.isEmpty()) {
                    LOGGER.debug("Building bind request");
                    Set<Path> bindRequest=new HashSet<>();
                    for(QueryInContext qic:bindable) {
                        if(qic.getQuery() instanceof FieldComparisonExpression) {
                            FieldComparisonExpression fce=(FieldComparisonExpression)qic.getQuery();
                            Path lfield=new Path(qic.getContext(),fce.getField());
                            Path rfield=new Path(qic.getContext(),fce.getRfield());
                            ResolvedFieldInfo f=getFieldToBind(lfield,rfield,conjunct,atNode);
                            if (f != null) {
                                bindRequest.add(f.getFieldName());
                            }
                        } else if(qic.getQuery() instanceof NaryFieldRelationalExpression) {
                            NaryFieldRelationalExpression nfr=(NaryFieldRelationalExpression)qic.getQuery();
                            Path lfield=new Path(qic.getContext(),nfr.getField());
                            Path rfield=new Path(qic.getContext(),nfr.getRfield());
                            ResolvedFieldInfo f=getFieldToBind(lfield,rfield,conjunct,atNode);
                            if (f != null) {
                                bindRequest.add(f.getFieldName());
                            }
                        }
                    }

                    LOGGER.debug("Bind fields:{}",bindRequest);
                    List<FieldBinding> fb=new ArrayList<>();               
                    // get the bound query
                    boundQuery=query.bind(fb,bindRequest);
                    
                    // collect bindings
                    for(FieldBinding b:fb) {
                        ResolvedFieldInfo rfi=conjunct.getFieldInfoByOriginalFieldName(b.getField());
                        if (rfi != null) {
                            bindings.add(new ResolvedFieldBinding(rfi.getEntityRelativeFieldName(),
                                                                  b,
                                                                  rfi.getFieldQueryPlanNode().getMetadata()));
                        }
                    }
                }
                RelativeRewriter rw=new RelativeRewriter(conjunct,root,atNode.getMetadata());
                boundQueries.add(rw.iterate(boundQuery));
            }
            
            QueryExpression query;
            if(boundQueries.size()==1) {
                query=boundQueries.get(0);
            } else {
                query=new NaryLogicalExpression(NaryLogicalOperator._and,boundQueries);
            }
            
            LOGGER.debug("Bound query:{}",query);
            ret=new BindResult(bindings,query);
        }

        return ret;
    }

    /**
     * Returns the field to bind. One of the fields belong to the
     * current query plan node (atNode), and the other one belongs to
     * an ancestor of the current node, so we bind the field that
     * belongs to an ancestor.
     */
    private static ResolvedFieldInfo getFieldToBind(Path lfield,Path rfield,Conjunct clause,QueryPlanNode atNode) {
        ResolvedFieldInfo lInfo=clause.getFieldInfoByOriginalFieldName(lfield);
        ResolvedFieldInfo rInfo=clause.getFieldInfoByOriginalFieldName(rfield);
        // If lfieldNode points to the destination node,
        // rfieldNode points to an ancestor, or vice versa
        if(lInfo != null && lInfo.getFieldQueryPlanNode().getName().equals(atNode.getName())) {
            return rInfo;
        } else {
            return lInfo;
        }
    }
        
    /**
     * Attempts to refresh this binding starting at the given parent document, and ascending to its parents
     */
    public boolean refresh(ChildDocReference ref) {
        ResultDoc doc=ref.getDocument();
        if(doc.getMetadata()==entity) {
            refresh(doc,ref.getReferenceField());
            return true;
        } else {
            for(Map.Entry<CompositeMetadata,ChildDocReference> entry: doc.getParentDocs().entrySet()) {
                if(refresh(entry.getValue()))
                    return true;
            }
            return false;
        }
    }

    public List<ParentDocReference> getParentDocReferences(ResultDoc doc) {
    	List<ParentDocReference> list=new ArrayList<>();
    	KeyValueCursor<Path,JsonNode> nodeCursor=doc.getDoc().getAllNodes(valueField);
    	while(nodeCursor.hasNext()) {
    	    nodeCursor.next();
    	    Path p=nodeCursor.getCurrentKey();
    	    list.add(new ParentDocReference(doc,p,this));
    	}
    	return list;
    }
    
    public void refresh(ParentDocReference ref) {
        ResultDoc doc=ref.getDocument();
        if(doc.getMetadata()==entity) {
            refresh(doc.getDoc().get(ref.getField()));            
        } else
            throw new RuntimeException("Unsupported binding");
    }

    public void refresh(ResultDoc doc,Path referenceField) {
        // Reinterpret the value field using the indexes in refField
        Path field=valueField.mutableCopy().rewriteIndexes(referenceField).immutableCopy();
        LOGGER.debug("Refreshing bindings using {}",field);
        refresh(doc.getDoc().get(field));
    }

    public void refresh(JsonNode valueNode) {
        if(binding instanceof ValueBinding) {
            if(valueNode==null)
                ((ValueBinding)binding).getValue().setValue(null);
            else
                ((ValueBinding)binding).getValue().setValue(type.fromJson(valueNode));
        } else if(binding instanceof ListBinding) {
            if(valueNode==null) 
                ((ListBinding)binding).getList().setList(new ArrayList());
            else {
                List<Value> l=new ArrayList<Value>( ((ArrayNode)valueNode).size());
                for(Iterator<JsonNode> itr=((ArrayNode)valueNode).elements();itr.hasNext();) {
                    l.add(new Value(type.fromJson(itr.next())));
                }
                ((ListBinding)binding).getList().setList(l);
            }
        }
    }
}
