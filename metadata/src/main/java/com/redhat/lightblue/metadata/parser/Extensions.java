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
import com.redhat.lightblue.metadata.HookConfiguration;

/**
 * Parser extensions where T is the node type of the underlying object tree (for JSon, T is JsonNode).
 */
public class Extensions<T> {

    private final ParserRegistry<T, DataStore> dataStoreParsers = new ParserRegistry<>();

    private final ParserRegistry<T, EntityConstraint> entityConstraintParsers = new ParserRegistry<>();

    private final ParserRegistry<T, FieldConstraint> fieldConstraintParsers = new ParserRegistry<>();

    private final ParserRegistry<T, HookConfiguration> hookConfigurationParsers = new ParserRegistry<T, HookConfiguration>();

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

    /**
     * Adds a hook configuration parser for a given hook name
     *
     * @param name Name of the hook
     * @param parser The parser for the hook configuration
     *
     */
    public void registerHookConfigurationParser(String name, HookConfigurationParser<T> parser) {
        hookConfigurationParsers.add(name, parser);
    }

    /**
     * Returns a parser that parses the given hook configuration
     *
     * @param hookName Name of the hook
     *
     * @return The parser that parses the hook configuration, or null if parser is not found
     */
    public HookConfigurationParser<T> getHookConfigurationParser(String hookName) {
        return (HookConfigurationParser<T>) hookConfigurationParsers.find(hookName);
    }

    @Override
    public String toString() {
        return dataStoreParsers.toString() + "\n"
            + entityConstraintParsers.toString() + "\n"
            + fieldConstraintParsers.toString() + "\n"
            + hookConfigurationParsers.toString();
    }
}
