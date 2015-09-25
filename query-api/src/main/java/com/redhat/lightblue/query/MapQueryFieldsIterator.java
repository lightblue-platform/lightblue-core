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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.Path;

/**
 * Maps query fields
 */
public class MapQueryFieldsIterator extends QueryIterator {

    /**
     * Override this method to map fields.
     */
    protected Path map(Path p) {
        return p;
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        Path p=map(applyContext(context,q.getField()));
        if(p!=null)
            return new ValueComparisonExpression(removeContext(context,p,q.getField()),q.getOp(),q.getRvalue());
        else
            return q;
    }

    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
    	Path r=map(applyContext(context,q.getRfield()));
        Path l=map(applyContext(context,q.getField()));
        if(r!=null||l!=null)
            return new FieldComparisonExpression(l==null?q.getField():removeContext(context,l,q.getField()),
                                                 q.getOp(),
                                                 r==null?q.getRfield():removeContext(context,r,q.getRfield()));
        else
            return q;
    }

    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        Path p=map(applyContext(context,q.getField()));
        if(p!=null)
            return new RegexMatchExpression(removeContext(context,p,q.getField()),
                                            q.getRegex(),q.isCaseInsensitive(),q.isMultiline(),q.isExtended(),q.isDotAll());
        else
            return q;
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        Path p=map(applyContext(context,q.getField()));
        if(p!=null)
            return new NaryValueRelationalExpression(removeContext(context,p,q.getField()),q.getOp(),q.getValues());
        else
            return q;
    }

    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        Path r=map(applyContext(context,q.getRfield()));
        Path l=map(applyContext(context,q.getField()));
        if(r!=null||l!=null)
            return new NaryFieldRelationalExpression(l==null?q.getField():removeContext(context,l,q.getField()),
                                                     q.getOp(),
                                                     r==null?q.getRfield():removeContext(context,r,q.getRfield()));
        else
            return q;
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        Path p=applyContext(context,q.getArray());
        if(p!=null)
            return new ArrayContainsExpression(removeContext(context,p,q.getArray()),q.getOp(),q.getValues());
        else
            return q;
    }

    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        ArrayMatchExpression x=(ArrayMatchExpression)super.itrArrayMatchExpression(q,context);
        Path p=map(applyContext(context,x.getArray()));
        if(p!=null)
            return new ArrayMatchExpression(removeContext(context,p,x.getArray()),x.getElemMatch());
        else
            return x;
    }
    
    private Path applyContext(Path context,Path p) {
    	if(context.isEmpty())
            return p;
    	else
            return new Path(context,p);
    }

    private Path removeContext(Path context,Path p,Path original) {
        if(context.isEmpty())
            return p;
        else if(context.matchingPrefix(p))
            return p.suffix(-context.numSegments());
        else
        	return original;
    }
}
