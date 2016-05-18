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
import java.util.stream.Collectors;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;

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
     * The original query clause
     */
    private final QueryExpression clause;

    private final List<QueryFieldInfo> fieldInfo;

    private final ResolvedReferenceField reference;

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
