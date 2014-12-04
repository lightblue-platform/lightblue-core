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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ProjectionFieldReferenceTest {
    final String doc1 = "{\"field\":\"field.x\", \"include\": true}";
    final String doc2 = "{\"field\":\"field.x\",\"include\": true, \"recursive\": true}";
    final String doc3 = "{\"field\":\"field.y.x\",\"include\": false, \"recursive\": true}";
    final String doc4 = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.z\",\"op\":\"$eq\",\"rvalue\":1},\"project\":{\"field\":\"member\"}}";
    final String doc5 = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"project\":{\"field\":\"member\"}}";

    @Test
    public void doc1Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc1));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided,p.getFieldInclusion(new Path("field.x.y")));
    }


    @Test
    public void doc2Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc2));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.implicit_inclusion,p.getFieldInclusion(new Path("field.x.y")));
    }

    @Test
    public void doc3Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc3));
        Assert.assertEquals(Projection.Inclusion.explicit_exclusion,p.getFieldInclusion(new Path("field.y.x")));
        Assert.assertEquals(Projection.Inclusion.undecided,p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided,p.getFieldInclusion(new Path("field.y")));
        Assert.assertEquals(Projection.Inclusion.implicit_exclusion,p.getFieldInclusion(new Path("field.y.x.z")));
    }

    @Test
    public void doc4Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc4));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided,p.getFieldInclusion(new Path("field.x.*.field.z")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.field.z")));
    }

    @Test
    public void doc5Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc5));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion,p.getFieldInclusion(new Path("field.x.*.member")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.member")));
    }

}
