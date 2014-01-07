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

import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.DefaultEntityConstraintParsers;
import com.redhat.lightblue.metadata.parser.DefaultFieldConstraintParsers;
import com.redhat.lightblue.metadata.parser.EntityConstraintParser;
import com.redhat.lightblue.metadata.parser.FieldConstraintParser;
import com.redhat.lightblue.metadata.parser.ParserRegistry;

/**
 * Parser extensions where T is the node type of the underlying object tree (for JSon, T is JsonNode).
 */
public class Extensions<T> {

    private final ParserRegistry<T, DataStore> dataStoreParsers = new ParserRegistry<>();

    private final ParserRegistry<T, EntityConstraint> entityConstraintParsers = new ParserRegistry<>();

    private final ParserRegistry<T, FieldConstraint> fieldConstraintParsers = new ParserRegistry<>();

    /**
     * Initializes this to include the default extensions
     */
    public void addDefaultExtensions() {
        fieldConstraintParsers.add(new DefaultFieldConstraintParsers<T>());
        entityConstraintParsers.add(new DefaultEntityConstraintParsers<T>());
    }

    /**
     * Adds a parser that parses DataStore
     *
     * @param name Name of the datastore type (such as 'mongo')
     * @param parser The parser that parses datastores of the given type
     */
    public void registerDataStoreParser(String name, DataStoreParser<T> parser) {
        dataStoreParsers.add(name, parser);
    }

    /**
     * Gets a datastore parser
     *
     * @param dataStoreName Name of the datastore (such as 'mongo')
     *
     * @return The parser for the datastore, or null if it does not exist
     */
    public DataStoreParser<T> getDataStoreParser(String dataStoreName) {
        return (DataStoreParser<T>) dataStoreParsers.find(dataStoreName);
    }

    /**
     * Registers an entity constraint parser for a given constraint name.
     *
     * @param name Name of the constraint
     * @param parser The parser for the constraint
     */
    public void registerEntityConstraintParser(String name, EntityConstraintParser<T> parser) {
        entityConstraintParsers.add(name, parser);
    }

    /**
     * Returns an entity constraint parser
     *
     * @param constraintName
     *
     * @return The parser that parses the constraint, or null if a parser is not found
     */
    public EntityConstraintParser<T> getEntityConstraintParser(String constraintName) {
        return (EntityConstraintParser<T>) entityConstraintParsers.find(constraintName);
    }

    /**
     * Adds a contraint parser for a given constraint name
     *
     * @param name Name of the constraint
     * @param parser The parser for the constraint
     *
     */
    public void registerFieldConstraintParser(String name, FieldConstraintParser< T> parser) {
        fieldConstraintParsers.add(name, parser);
    }

    /**
     * Returns a parser that parses the given constraint
     *
     * @param constraintName Name of the constraint
     *
     * @return The parser that parses the constraint, or null if parser is not found
     */
    public FieldConstraintParser<T> getFieldConstraintParser(String constraintName) {
        return (FieldConstraintParser<T>) fieldConstraintParsers.find(constraintName);
    }

    @Override
    public String toString() {
        return dataStoreParsers.toString() + "\n"
                + entityConstraintParsers.toString() + "\n"
                + fieldConstraintParsers.toString();
    }
}
