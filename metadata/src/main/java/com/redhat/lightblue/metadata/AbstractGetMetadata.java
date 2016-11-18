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

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of CompositeMetadata.GetMetadata interface that limits
 * the metadata retrieval using the query and projections. To extend it to a
 * concrete implementation, override the retrieveMetadata method.
 *
 * This implementation will return metadata for entities that are required to
 * evaluate a set of projections and queries. The expected usage of this class
 * is to initialize it using projections and queries from the request. Once
 * initialized and passed to CompositeMetadata, this class limits the depth of
 * the composite metadata to the minimal tree that includes all entities
 * sufficient to evaluate all projections and queries.
 *
 */
public abstract class AbstractGetMetadata implements CompositeMetadata.GetMetadata, Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGetMetadata.class);

    private final List<Projection> projections = new ArrayList<>();
    private final List<QueryExpression> queries = new ArrayList<>();

    public AbstractGetMetadata() {
    }

    public AbstractGetMetadata(Projection projection,
                               QueryExpression query) {
        if (projection != null) {
            projections.add(projection);
        }
        if (query != null) {
            queries.add(query);
        }
    }

    public AbstractGetMetadata(List<Projection> projections,
                               List<QueryExpression> queries) {
        if (projections != null) {
            this.projections.addAll(projections);
        }
        if (queries != null) {
            this.queries.addAll(queries);
        }
    }

    public void add(Projection p) {
        projections.add(p);
    }

    public void add(QueryExpression q) {
        queries.add(q);
    }

    /**
     * This implementation will return the metadata for an entity if that entity
     * is required by the given projections and queries.
     */
    @Override
    public EntityMetadata getMetadata(Path injectionField,
                                      String entityName,
                                      String version) {
        // See if injectionField is projected or used in a query
        LOGGER.debug("Check if {} is needed based on field {}",entityName,injectionField);
        if (isProjected(injectionField)
                || isQueried(injectionField)) {
            LOGGER.debug("{} is needed based on field {}",entityName,injectionField);
            return retrieveMetadata(injectionField, entityName, version);
        }
        LOGGER.debug("{} is not needed based on field {}",entityName,injectionField);
        return null;
    }

    /**
     * The implementation should retrieve and return the metadata for the given
     * version of the given entity
     */
    protected abstract EntityMetadata retrieveMetadata(Path injectionField,
                                                       String entityName,
                                                       String version);

    /**
     * Returns true if field inclusion is explicit.
     *
     * @param field the path to check
     * @return
     */
    private boolean isProjected(Path field) {
        LOGGER.debug("Checking if {} is explicitly projected", field);
        for (Projection p : projections) {
            Projection.Inclusion inc = p.getFieldInclusion(field);
            if (inc == Projection.Inclusion.explicit_inclusion) {
                LOGGER.debug("{} is explicitly projected by {}", field, p);
                return true;
            } else {
                LOGGER.debug("{} is not projected by {}", field,p);
            }
        }
        return false;
    }

    private boolean isQueried(Path field) {
        LOGGER.debug("Checking if {} is queried", field);
        for (QueryExpression q : queries) {
            if (q.isRequired(field)) {
                LOGGER.debug("{} is queried by {}", field, q);
                return true;
            } else {
                LOGGER.debug("{} is not queried by {}", field,q);
            }                
        }
        return false;
    }

}
