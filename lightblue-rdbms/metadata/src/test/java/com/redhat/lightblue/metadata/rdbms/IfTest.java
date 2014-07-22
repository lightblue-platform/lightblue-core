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
package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfTest {

    @Test
    public void testGetSetConditions() {
        If instance = new IfImpl();
        instance.setConditions(null);
        List expResult = null;
        List result = instance.getConditions();
        assertEquals(expResult, result);
        expResult = new ArrayList();
        instance.setConditions(expResult);
        result = instance.getConditions();
        assertEquals(expResult, result);
    }

    public class IfImpl extends If {
        @Override
        public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
            throw new UnsupportedOperationException("Not supported");
        }
    }

}
