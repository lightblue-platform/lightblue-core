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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

public class FindRequest extends Request {

    private QueryExpression query;
    private Projection projection;
    private Sort sort;
    private Long from;
    private Long to;

    public QueryExpression getQuery() {
        return query;
    }

    public void setQuery(QueryExpression q) {
        query=q;
    } 

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection x) {
        projection=x;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort s) {
        sort=s;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long l) {
        from=l;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long l) {
        to=l;
    }
}
