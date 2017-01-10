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
package com.redhat.lightblue.assoc.ep;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.assoc.BindQuery;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.util.Tuples;

/**
 * This class contains the different variations of search and assembly
 * algorithmss as static methods
 */
public final class Searches {

    private static final Logger LOGGER = LoggerFactory.getLogger(Searches.class);

    private Searches() {
    }

    /**
     * Given a parent document and an association query, returns a query for
     * each slot that will be used to retrieve the child documents that will be
     * attached to that slot
     */
    public static Map<ChildSlot, QueryExpression> writeChildQueriesFromParentDoc(AssociationQuery aq,
                                                                                 ResultDocument parentDocument) {
        Map<ChildSlot, BindQuery> binders = parentDocument.getBindersForChild(aq);
        LOGGER.debug("writeChildQueriesForParentDoc: aq={} binders={}", aq, binders);
        Map<ChildSlot, QueryExpression> queries = new HashMap<>();
        for (Map.Entry<ChildSlot, BindQuery> entry : binders.entrySet()) {
            if (aq.getQuery() != null) {
                queries.put(entry.getKey(), entry.getValue().iterate(aq.getQuery()));
            } else {
                queries.put(entry.getKey(), null);
            }
        }
        LOGGER.debug("Queries:{}", queries);
        return queries;
    }

    /**
     * Writes queries form a join tuple
     */
    public static List<QueryExpression> writeQueriesForJoinTuple(JoinTuple tuple, ExecutionBlock childBlock) {
        LOGGER.debug("writeQueriesForJoinTuples tuple:{}, child:{}", tuple, childBlock);
        Tuples<BindQuery> btuples = new Tuples<>();
        if (tuple.getParentDocument() != null) {
            AssociationQuery aq = childBlock.getAssociationQueryForEdge(tuple.getParentDocument().getBlock());
            BindQuery parentb = tuple.getParentDocument().getBindersForSlot(tuple.getParentDocumentSlot(), aq);
            List<BindQuery> l = new ArrayList<>(1);
            l.add(parentb);
            btuples.add(l);
        }
        if (tuple.getChildTuple() != null) {
            // Add the child binders to the b-tuples
            for (ResultDocument childDoc : tuple.getChildTuple()) {
                AssociationQuery aq = childBlock.getAssociationQueryForEdge(childDoc.getBlock());
                List<BindQuery> binders = childDoc.getBindersForParent(aq);
                btuples.add(binders);
            }
        }
        List<QueryExpression> queries = new ArrayList<>();
        for (ExecutionBlock sourceBlock : tuple.getBlocks()) {
            AssociationQuery aq = childBlock.getAssociationQueryForEdge(sourceBlock);
            if (aq.getQuery() != null) {
                queries.add(aq.getQuery());
            }
        }
        QueryExpression query = and(queries);
        ArrayList<QueryExpression> ret = new ArrayList<>();
        if (query != null) {
            for (Iterator<List<BindQuery>> itr = btuples.tuples(); itr.hasNext();) {
                List<BindQuery> binders = itr.next();
                BindQuery allBinders = BindQuery.combine(binders);
                ret.add(allBinders.iterate(query));
            }
        }
        LOGGER.debug("queries={}", ret);
        return ret;
    }

    /**
     * Combines queries with AND. Queries can be null, but at least one of them
     * must be non-null
     */
    public static QueryExpression and(QueryExpression... q) {
        return combine(NaryLogicalOperator._and, Arrays.asList(q));
    }

    public static QueryExpression and(List<QueryExpression> list) {
        return combine(NaryLogicalOperator._and, list);
    }

    public static QueryExpression combine(NaryLogicalOperator op, List<QueryExpression> list) {
        List<QueryExpression> l = list.stream().filter(q -> q != null).collect(Collectors.toList());
        if (l.size() == 0) {
            return null;
        } else if (l.size() == 1) {
            return l.get(0);
        } else {
            return new NaryLogicalExpression(op, l);
        }
    }

}
