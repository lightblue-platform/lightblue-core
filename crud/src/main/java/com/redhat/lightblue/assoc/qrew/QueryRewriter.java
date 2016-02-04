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
package com.redhat.lightblue.assoc.qrew;

import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;

import com.redhat.lightblue.util.CopyOnWriteIterator;

import com.redhat.lightblue.assoc.qrew.rules.*;

/**
 * Implementation of Rewriter that orchestrates rewriting rules with registered Rewriter instances.
 */
public final class QueryRewriter extends Rewriter {

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryRewriter.class);

    private static final Set<Rewriter> rewriteRules=new HashSet<>(16);

    public QueryRewriter() {
        this(true);
    }

    public QueryRewriter(boolean withDefaultRules) {
        if(withDefaultRules)
            registerDefaultRules();
    }

    /**
     * Register a Rewriter instance.  If attempt to register QueryRewriter returns without doing anything.
     * @param rule the rewriter to register
     */
    public void register(Rewriter rule) {
        if (rule instanceof QueryRewriter) {
            return;
        }
        rewriteRules.add(rule);
    }

    /**
     * Register default rules.
     */
    protected void registerDefaultRules() {
        register(CombineANDsToNIN.INSTANCE);
        register(CombineINsInOR.INSTANCE);
        register(CombineNINsInAND.INSTANCE);
        register(CombineORsToIN.INSTANCE);
        register(EliminateNOT.INSTANCE);
        register(EliminateNOTNOT.INSTANCE);
        register(EliminateNOTOR.INSTANCE);
        register(EliminateSingleANDOR.INSTANCE);
        register(ExtendINsInOR.INSTANCE);
        register(ExtendNINsInAND.INSTANCE);
        register(PromoteNestedAND.INSTANCE);
        register(SimpleElemMatchIsComparison.INSTANCE);
    }

    protected QueryExpression rewriteIteration(QueryExpression q) {
        LOGGER.debug("Rewrite iteration begins for q={}",q);
        QueryExpression newq=q;
        if (q instanceof UnaryLogicalExpression) {
            LOGGER.debug("q is a unary logical expression, rewriting nested query");
            QueryExpression nestedq=((UnaryLogicalExpression)q).getQuery();
            QueryExpression newNestedq=rewriteIteration(nestedq);
            LOGGER.debug("Rewritten nested query={}",newNestedq);
            if(newNestedq!=nestedq)
                newq=new UnaryLogicalExpression( ((UnaryLogicalExpression)q).getOp(), newNestedq);
        } else if (q instanceof NaryLogicalExpression) {
            LOGGER.debug("q is a n-ary logical expression, rewriting nested terms");
            CopyOnWriteIterator<QueryExpression> cowr=new CopyOnWriteIterator<>( ((NaryLogicalExpression)q).getQueries());
            while(cowr.hasNext()) {
                QueryExpression nestedq=cowr.next();
                QueryExpression newNestedq=rewriteIteration(nestedq);
                if(newNestedq!=nestedq)
                    cowr.set(newNestedq);
            }
            if(cowr.isCopied())
                newq=new NaryLogicalExpression( ((NaryLogicalExpression)q).getOp(),cowr.getCopiedList());
        } else if (q instanceof ArrayMatchExpression) {
            LOGGER.debug("q is an array match expression, rewriting nested query");
            QueryExpression nestedq=((ArrayMatchExpression)q).getElemMatch();
            QueryExpression newNestedq=rewriteIteration(nestedq);
            LOGGER.debug("Rewritten nested query={}",newNestedq);
            if(newNestedq!=nestedq)
                newq=new ArrayMatchExpression( ((ArrayMatchExpression)q).getArray(),newNestedq);
        } 
        LOGGER.debug("Applying rewrite rules to q");
        newq=applyRules(newq);
        LOGGER.debug("Rewritten q={}",newq);
        
        return newq;
    }

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        QueryExpression trc=q;
        QueryExpression newq;
        boolean done=false;
        do {
            newq=rewriteIteration(trc);
            LOGGER.debug("Rewrite iteration pre={}",trc);
            LOGGER.debug("Rewrite iteration post={}",newq);
            if(newq==trc)
                done=true;
            trc=newq;
        } while(!done);
        return newq;
    }

    private QueryExpression applyRules(QueryExpression q) {
        QueryExpression newq=q;
        for(Rewriter r:rewriteRules)
            newq=r.rewrite(newq);
        return newq;
    }
}

