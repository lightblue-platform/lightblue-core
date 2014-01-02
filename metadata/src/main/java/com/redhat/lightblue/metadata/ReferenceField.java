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
package com.redhat.lightblue.metadata;

import java.util.Iterator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.metadata.types.ReferenceType;

public class ReferenceField extends Field {

    private String entityName;
    private String version;
    private Projection projection;
    private QueryExpression query;
    private Sort sort;

    public ReferenceField(String name) {
        super(name, ReferenceType.TYPE);
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public String getVersionValue() {
        return version;
    }

    public void setVersionValue(String v) {
        version = v;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection p) {
        this.projection = p;
    }

    public QueryExpression getQuery() {
        return query;
    }

    public void setQuery(QueryExpression q) {
        query = q;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort s) {
        sort = s;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public Iterator<? extends FieldTreeNode> getChildren() {
        return FieldTreeNode.EMPTY;
    }

    @Override
    protected FieldTreeNode resolve(Path p, int level) {
        if (p.numSegments() == level) {
            return this;
        } else {
            throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
        }
    }

}
