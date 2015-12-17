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
package com.redhat.lightblue.eval;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class FieldAccessRoleEvaluatorTest extends AbstractJsonNodeTest {
    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void testDateComparison() throws Exception {
        Set<String> roles=new HashSet<>();
        roles.add("somerole");
        FieldAccessRoleEvaluator eval=new FieldAccessRoleEvaluator(md,roles);
        JsonDoc newDoc = EvalTestContext.getDoc("./dateCmp-1.json");
        JsonDoc oldDoc = EvalTestContext.getDoc("./dateCmp-2.json");
        List<Path> list=eval.getInaccessibleFields_Update(newDoc,oldDoc);
        Assert.assertEquals(0,list.size());
    }
}
