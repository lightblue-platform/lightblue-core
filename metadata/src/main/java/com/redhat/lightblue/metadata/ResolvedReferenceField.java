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

import java.util.ArrayList;
import java.util.Iterator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.metadata.types.ArrayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolvedReferenceField extends ArrayField {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvedReferenceField.class);

    private static final long serialVersionUID = 1L;

    private final ReferenceField reference;
    private final CompositeMetadata metadata;

    public ResolvedReferenceField(ReferenceField reference,
                                  CompositeMetadata metadata) {
        super(reference.getName());
        this.reference=reference;
        this.metadata=metadata;
        setElement(new ObjectArrayElement(metadata.getFields()));
    }

    public ReferenceField getReferenceField() {
        return reference;
    }

    public CompositeMetadata getReferencedMetadata() {
        return metadata;
    }
}
