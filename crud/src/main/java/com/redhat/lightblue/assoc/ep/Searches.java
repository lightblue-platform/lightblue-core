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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.assoc.Binder;
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

    private static final Logger LOGGER=LoggerFactory.getLogger(Searches.class);
    
    private Searches() {}

    /**
     * Given a parent document and an association query, returns a query for each slot that will be used to
     * retrieve the child documents that will be attached to that slot
     */
    public static Map<ChildSlot,QueryExpression> writeChildQueriesFromParentDoc(AssociationQuery aq,
                                                                                ResultDocument parentDocument) {
        Map<ChildSlot,List<Binder>> binders=parentDocument.getBindersForChild(aq);
        Map<ChildSlot,QueryExpression> queries=new HashMap<>();
        for(Map.Entry<ChildSlot,List<Binder>> entry:binders.entrySet()) {
            if(aq.getQuery()!=null) {
                BindQuery bq=new BindQuery(entry.getValue());
                queries.put(entry.getKey(),bq.iterate(aq.getQuery()));
            } else {
                queries.put(entry.getKey(),null);
            }
        }
        return queries;
    }

    public static List<QueryExpression> writeQueriesForJoinTuple(JoinTuple tuple,ExecutionBlock childBlock) {
        Tuples<List<Binder>> btuples=null;
        List<Binder> parentBinders=null;
        if(tuple.getParentDocument()!=null) {
            AssociationQuery aq=childBlock.getAssociationQueryForEdge(tuple.getParentDocument().getBlock());
            parentBinders=tuple.getParentDocument().getBindersForSlot(tuple.getParentDocumentSlot(),aq);
            ArrayList<List<Binder>> l=new ArrayList<>();
            l.add(parentBinders);
            btuples.add(l);
        }
        if(tuple.getChildTuple()!=null) {
            btuples=new Tuples<>();
            for(ResultDocument childDoc:tuple.getChildTuple()) {
                AssociationQuery aq=childBlock.getAssociationQueryForEdge(childDoc.getBlock());
                List<List<Binder>> binders=childDoc.getBindersForParent(aq);
                btuples.add(binders);
            }
        }
        List<QueryExpression> queries=new ArrayList<>();
        for(ExecutionBlock sourceBlock:tuple.getBlocks()) {
            AssociationQuery aq=childBlock.getAssociationQueryForEdge(sourceBlock);
            if(aq.getQuery()!=null)
                queries.add(aq.getQuery());
        }
        QueryExpression query;
        if(queries.size()>1)
            query=new NaryLogicalExpression(NaryLogicalOperator._and,queries);
        else if(queries.size()==1)
            query=queries.get(0);
        else
            query=null;
        ArrayList<QueryExpression> ret=new ArrayList<>();
        if(query!=null) {
            for(Iterator<List<List<Binder>>> itr=btuples.tuples();itr.hasNext();) {
                List<List<Binder>> binders=itr.next();
                ArrayList<Binder> allBinders=new ArrayList<>();
                for(List<Binder> x:binders)
                    allBinders.addAll(x);
                BindQuery bq=new BindQuery(allBinders);
                ret.add(bq.iterate(query));
            }
        }
        return ret;
    }
    
}
