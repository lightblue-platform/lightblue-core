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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ProjectorProfileTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setup() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }


    @Test
    public void fieldProjectorTest_arr_query() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'*','recursive':1},{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}]");
        Projector projector = Projector.getInstance(p, md);
        long l=System.currentTimeMillis();
        for(int i=0;i<1000;i++)
            projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(System.currentTimeMillis()-l);

        System.out.println(projector.measure.toString());
    }
}
