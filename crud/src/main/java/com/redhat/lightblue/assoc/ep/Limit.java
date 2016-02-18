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

/**
 * Limits the resultset to at most n
 */
public class Limit<T> implements Step<T> {

    private final int limit;
    private final Step<T> source;
    private int at;
    
    public Limit(int n,Step<T> source) {
        this.source=source;
        limit=n;
        at=0;
    }

    public ResultStream<T> getResults(ExecutionContext ctx) {
        
        return new StreamWrapper<T>(source.getResults(ctx)) {
            @Override
            public T next() {
                T ret;
                if(at<limit)
                    ret=sourceStream.next();
                else
                    ret=null;
                at++;
                return ret;
            }
        };
    }
}
