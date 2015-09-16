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

import com.redhat.lightblue.metadata.types.ReferenceType;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import java.util.Iterator;

public class ReferenceField extends Field {

    private static final long serialVersionUID = 1L;

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
    public FieldTreeNode resolve(Path p, int level) {
        if (p.numSegments() == level) {
            return this;
        } else if (Path.PARENT.equals(p.head(level))) {
            return this.getParent().resolve(p, level + 1);
        } else {
            throw Error.get(MetadataConstants.ERR_INVALID_FIELD_REFERENCE,p.head(level)+" in "+p.toString());
        }
    }

    @Override
    public String toString() {
        return getName()+"->"+entityName+"@"+version;
    }
}
