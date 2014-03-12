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

import org.junit.Assert;
import org.junit.Test;

public class ParserRegistryTest {

    class TestParser implements Parser<Object, Object> {

        @Override
        public Object parse(String name, MetadataParser<Object> p, Object node) {
            return null;
        }

        @Override
        public void convert(MetadataParser<Object> p, Object emptyNode, Object object) {
        }
    }

    @Test
    public void get() {
        ParserRegistry<Object, Object> reg = new ParserRegistry<>();

        TestParser parser = new TestParser();

        String name = "foo";

        reg.add(name, parser);

        Assert.assertEquals(parser, reg.find(name));
    }
}
