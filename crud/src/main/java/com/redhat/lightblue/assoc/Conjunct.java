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

import java.io.Serializable;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.redhat.lightblue.query.*;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.util.Path;

/**
 * A query clause that cannot be further broken into conjuncts of a conjunctive
 * normal form query. This class also keeps query analysis information along
 * with the clause
 *
 * Object identity of conjuncts are preserved during query plan processing. That
 * is, a Conjunct object created for a particular query plan is used again for
 * other incarnation of that query plan, only node associations are changed. So,
 * it is possible to keep maps that map a Conjunct to some other piece of data.
 * This is important in query scoring. Scoring can process conjuncts, and keep
 * data internally to prevent recomputing the cost associated with the conjunct
 * for every possible query plan.
 */
public class Conjunct implements Serializable {

    private static final long serialVersionUID = 1l;

    /**
     * Type of the conjunct. Value means the conjunct compares a field
     * to a value. relation means conjunct compares fields of an
     * entity to the fields of another entity. Complex means either
     * more than two entities are involved, or the comparison is an OR
     * comparison can cannot be evaluated as an association query
     */
    public enum ConjunctType { value, relation, complex };

    /**
     * The original query clause
     */
    private final QueryExpression clause;

    private final List<QueryFieldInfo> fieldInfo;

    private final ResolvedReferenceField reference;

    private static class ItrState {
        ConjunctType type;
        CompositeMetadata e1=null;
        CompositeMetadata e2=null;
        int numEntities=0;

        public ItrState(ConjunctType t) {
            type=t;
        }

        public void add(CompositeMetadata m) {
            if(m!=null) {
                if(m!=e1&&m!=e2) {
                    if(e1==null) {
                        e1=m;
                    } else {
                        e2=m;
                    }
                    numEntities++;
                }
            }
        }
    }
    
    private class ClauseIterator extends QueryIteratorSkeleton<ItrState> {

        private QueryFieldInfo findOne(QueryExpression clause) {
            for (QueryFieldInfo fi : fieldInfo) {
                if (fi.getClause() == clause) {
                    return fi;
                }
            }
            throw new RuntimeException("Query processing error: cannot find clause in query field info:"+clause.toString());
        }

        private QueryFieldInfo[] findTwo(QueryExpression clause) {
            QueryFieldInfo[] ret=new QueryFieldInfo[2];
            int i=0;
            for (QueryFieldInfo fi : fieldInfo) {
                if (fi.getClause() == clause) {
                    ret[i++]=fi;
                }
            }
            return ret;
        }
        
        @Override
        protected ItrState itrAllMatchExpression(AllMatchExpression q, Path context) {
            ItrState state=new ItrState(ConjunctType.value);
            state.numEntities=0;
            return state;
        }

        private ItrState oneField(QueryExpression q) {
            ItrState state=new ItrState(ConjunctType.value);
            state.e1=findOne(q).getFieldEntity();
            state.numEntities=1;
            return state;
        }

        private ItrState twoFields(QueryExpression q) {
            ItrState state=new ItrState(ConjunctType.value);
            QueryFieldInfo[] qfi=findTwo(q);
            state.e1=qfi[0].getFieldEntity();
            state.numEntities=1;
            if(qfi[1]!=null&&qfi[1].getFieldEntity()!=state.e1) {
                state.e2=qfi[1].getFieldEntity();
                state.numEntities=2;
                state.type=ConjunctType.relation;
            } 
            return state;
        }

        @Override
        protected ItrState itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
            return oneField(q);
        }

        @Override
        protected ItrState itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
            return twoFields(q);
        }

        @Override
        protected ItrState  itrRegexMatchExpression(RegexMatchExpression q, Path context) {
            return oneField(q);
        }

        @Override
        protected ItrState itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
            return oneField(q);
        }

        @Override
        protected ItrState  itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
            return twoFields(q);
        }

        @Override
        protected ItrState  itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
            return oneField(q);
        }

        @Override
        protected ItrState  itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
            return iterate(q.getQuery(),context);
        }
        
        @Override
        protected ItrState itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
            return iterate(q.getElemMatch(),context);
        }

        private void processRelation(ItrState ret, ItrState qstate, NaryLogicalOperator op) {
            if(ret.numEntities==2) {
                if( (ret.e1==qstate.e1||ret.e1==qstate.e2) &&
                    (ret.e2==qstate.e1||ret.e2==qstate.e2) ) {
                    if(op==NaryLogicalOperator._or) {
                        // An OR comparison makes this a complex conjunct
                        ret.type=ConjunctType.complex;
                    }
                } else {
                    // Incompatioble expressions
                    ret.type=ConjunctType.complex;
                }
            } else {
                ret.type=ConjunctType.complex;
            }
        }

        @Override
        protected ItrState itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
            ItrState ret=null;
            boolean first=true;
            for(QueryExpression subq:q.getQueries()) {
                if(first) {
                    ret=iterate(subq,context);
                    first=false;
                } else if(ret.type!=ConjunctType.complex) {                    
                    ItrState qstate=iterate(subq,context);
                    // We compare if this expression is compatible with existing assessment
                    switch(qstate.type) {
                    case value:
                        if(ret.type==ConjunctType.value) {
                            // Comparing value expression to another value expression
                            // At most one entity can be present
                            if(!(ret.numEntities==0||ret.e1==qstate.e1))  {
                                // Two expressions are comparing values at different entities
                                ret.add(qstate.e1);
                                ret.type=ConjunctType.complex;
                            }
                        } else {
                            // Then type=relation
                            processRelation(ret,qstate,q.getOp());
                        }
                        break;
                            
                    case relation:
                        if(ret.type==ConjunctType.value) {
                            if(ret.numEntities==0||ret.e1==qstate.e1||ret.e2==qstate.e2) {
                                ret.type=ConjunctType.relation; // Compatible expressions
                            } else {
                                ret.type=ConjunctType.complex;
                            }                            
                        } else {
                            processRelation(ret,qstate,q.getOp());
                        }
                        break;
                        
                    case complex:
                        ret.type=ConjunctType.complex;
                        break;
                    }
                } else {
                    break;
                }
            }
            return ret==null?new ItrState(ConjunctType.value):ret;
        }    
    }
        
    
    public Conjunct(QueryExpression q,
                    List<QueryFieldInfo> fieldInfo,
                    ResolvedReferenceField reference) {
        this.clause = q;
        this.reference = reference;
        this.fieldInfo = fieldInfo;
    }

    /**
     * Returns true if the clause belongs to a request query, not to a reference
     * field
     */
    public boolean isRequestQuery() {
        return reference == null;
    }

    public ResolvedReferenceField getReference() {
        return reference;
    }

    /**
     * Returns the field information about the fields in the conjunct
     */
    public List<QueryFieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    /**
     * Returns the query clause
     */
    public QueryExpression getClause() {
        return clause;
    }

    public ConjunctType getConjunctType() {
        Set<CompositeMetadata> md=getEntities();
        int n=md.size();
        if(n<=1) {
            // If there is at most one entity in the conjunct, then this is a value expression
            return ConjunctType.value;
        } else if(n>2) {
            return ConjunctType.complex;
        } else {
            // The problem arises when the expression uses two
            // entities This could be an expression that relates
            // values between two entities (in which case, it is a
            // relation), or this could be an expression that simply
            // contains values from two entities combines with an OR
            // (in which case, it is a complex conjunct).

            
            if(clause instanceof FieldComparisonExpression||
               clause instanceof ArrayContainsExpression||
               clause instanceof NaryFieldRelationalExpression) {
                // These are simple 2-term expressions
                return ConjunctType.relation;
            } else {
                // At this point, clause is one of:
                //   - ArrayMatchExpression
                //   - NaryLogicalExpression
                //   - UnaryLogicalExpression
                ClauseIterator itr=new ClauseIterator();
                return itr.iterate(clause).type;
            }
        }
    }

    /**
     * Returns a set of entities this conjunct refers to
     */
    public Set<CompositeMetadata> getEntities() {
        return fieldInfo.stream().
                filter(qfi -> qfi.isLeaf()).
                map(QueryFieldInfo::getFieldEntity).
                collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return clause.toString();
    }
}
