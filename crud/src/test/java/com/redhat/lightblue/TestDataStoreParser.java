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
package com.redhat.lightblue;

import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

public class TestDataStoreParser<T> implements DataStoreParser<T> {

    @Override
    public DataStore parse(String name, MetadataParser<T> p, T node) {
        return new DataStore() {
            public String getType() {
                return "mongo";
            }
        };
    }

    @Override
    public void convert(MetadataParser<T> p, T emptyNode, DataStore object) {
    }

    @Override
    public String getDefaultName() {
        return "mongo";
    }
}
