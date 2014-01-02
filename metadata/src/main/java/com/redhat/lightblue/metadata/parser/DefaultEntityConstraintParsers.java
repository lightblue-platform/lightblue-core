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
package com.redhat.lightblue.metadata.parser;

import com.redhat.lightblue.util.DefaultResolver;

import com.redhat.lightblue.metadata.EntityConstraint;

import com.redhat.lightblue.metadata.constraints.UniqueConstraint;
import com.redhat.lightblue.metadata.constraints.ReferencesConstraint;

/**
 * Convenience class to register all predefined constraint parsers to MetadataParser
 */
public class DefaultEntityConstraintParsers<N>
        extends DefaultResolver<String, Parser<N, EntityConstraint>> {

    public DefaultEntityConstraintParsers() {
        addValue(UniqueConstraint.UNIQUE, new UniqueConstraintParser<N>());
        addValue(ReferencesConstraint.REFERENCES, new ReferencesConstraintParser<N>());
    }
}
