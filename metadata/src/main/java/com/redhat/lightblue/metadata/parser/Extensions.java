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

import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityConstraint;
import com.redhat.lightblue.metadata.FieldConstraint;

public class Extensions<T> {

    private final ParserRegistry<T,DataStore> dataStoreParsers=
        new ParserRegistry<T,DataStore>();

    private final ParserRegistry<T,EntityConstraint> entityConstraintParsers=
        new ParserRegistry<T,EntityConstraint>();

    private final ParserRegistry<T,FieldConstraint> fieldConstraintParsers=
        new ParserRegistry<T,FieldConstraint>();

    /**
     * Initializes this to include the default extensions
     */
    public void addDefaultExtensions() {
        fieldConstraintParsers.add(new DefaultFieldConstraintParsers());
        entityConstraintParsers.add(new DefaultEntityConstraintParsers());
    }

    public void registerDataStoreParser(String name,DataStoreParser<T> parser) {
        dataStoreParsers.add(name,parser);
    }

    public DataStoreParser<T> getDataStoreParser(String dataStoreName) {
        return (DataStoreParser<T>)dataStoreParsers.get(dataStoreName);
    }

    public void registerEntityConstraintParser(String name,EntityConstraintParser<T> parser) {
        entityConstraintParsers.add(name,parser);
    }

    public EntityConstraintParser<T> getEntityConstraintParser(String constraintName) {
        return (EntityConstraintParser<T>)entityConstraintParsers.get(constraintName);
    }

    public void registerFieldConstraintParser(String name,FieldConstraintParser<T> parser) {
        fieldConstraintParsers.add(name,parser);
    }

    public FieldConstraintParser<T> getFieldConstraintParser(String constraintName) {
        return (FieldConstraintParser<T>)fieldConstraintParsers.get(constraintName);
    }
}
