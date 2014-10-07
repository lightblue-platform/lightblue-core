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

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;

import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.CompositeMetadata;

/**
 * Abstract base class for query plan node. The node keeps the
 * composite metadata corresponding to this query plan node, and the
 * query clauses associated with it. The actual tree representation of
 * the node is managed by the implementation, which is an inner class
 * in QueryPlan.
 */
public abstract class QueryPlanNode implements Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(QueryPlanNode.class);

    protected final CompositeMetadata md;
    protected final QueryPlanData data;
    
    private final Map<String,Object> properties=new HashMap<>();

    /**
     * Creates a query plan node using the given composite metadata,
     * and an empty query clauses list
     */
    public QueryPlanNode(CompositeMetadata md,QueryPlanData data) {
        this.md=md;
        this.data=data;
    }

    /**
     * Copy constructor. A reference to the composite metadata, and a
     * copy of the query clauses is saved.
     */
    public QueryPlanNode(QueryPlanNode source) {
        this.md=source.md;
        this.data=source.data.newInstance();
        this.data.copyFrom(source.data);
        this.properties.putAll(source.properties);
    }

    /**
     * Returns the composite metadata associated with this node
     */
    public CompositeMetadata getMetadata() {
        return md;
    }

    /**
     * Returns the quey plan data used by the scorer
     */
    public QueryPlanData getData() {
        return data;
    }

    /**
     * Returns the property with the given property name
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Sets the property with the given property name
     */
    public void setProperty(String propertyName,Object value) {
        properties.put(propertyName,value);
    }

    /**
     * Returns the property whose property name is the given class name
     */
    public <T> T getProperty(Class<T> propertyClass) {
        return (T)properties.get(propertyClass.getName());
    }

    /**
     * Sets the property whose property name is the given class name
     */
    public <T> void setProperty(Class<T> propertyClass, T propertyValue) {
        properties.put(propertyClass.getName(),propertyValue);
    }

    /**
     * The implementation returns the unique name for this node. The
     * only practical use is for debugging
     */
    public abstract String getName();

    /**
     * Returns the immediate ancestors of this node. If there are none
     * (i.e. node is a source), returns an array with size 0
     */
    public abstract QueryPlanNode[] getSources();

    /**
     * Returns the immediate descendands of this node. If there are
     * node (i.e. node is a sink), returns an array with size 0
     */
    public abstract QueryPlanNode[] getDestinations();

}
