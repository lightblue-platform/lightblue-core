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

import com.redhat.lightblue.query.QueryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolvedReferenceField extends ArrayField {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvedReferenceField.class);

    private static final long serialVersionUID = 1L;

    private final ReferenceField reference;
    private final CompositeMetadata metadata;
    private final EntityMetadata originalMetadata;
    private QueryExpression absQuery;

    public ResolvedReferenceField(ReferenceField reference,
                                  EntityMetadata originalMetadata,
                                  CompositeMetadata metadata) {
        super(reference.getName());
        this.reference = reference;
        this.originalMetadata = originalMetadata;
        this.metadata = metadata;
        setElement(ObjectArrayElement.withFields(metadata.getFields()));
    }

    public ReferenceField getReferenceField() {
        return reference;
    }

    public CompositeMetadata getReferencedMetadata() {
        return metadata;
    }

    /**
     * Returns the metadata for the referenced entity. This copy of metadata
     * isn't attached to the composite metadata tree, it is the unmodified
     * metadata of the referenced entity with unresolved references.
     */
    public EntityMetadata getOriginalMetadata() {
        return originalMetadata;
    }

    /**
     * Returns the query of the reference, reinterpreted based on the resolved
     * reference. All relative references in the original query are replaced by
     * absolute field references.
     */
    public QueryExpression getAbsQuery() {
        return absQuery;
    }

    /**
     * Sets the query of the reference, reinterpreted based on the resolved
     * reference. All relative references in the original query are replaced by
     * absolute field references.
     */
    public void setAbsQuery(QueryExpression q) {
        absQuery = q;
    }
}
