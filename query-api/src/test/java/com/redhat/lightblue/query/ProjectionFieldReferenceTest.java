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

public class ProjectionFieldReferenceTest {
    final String p_field_inclusion = "{\"field\":\"field.x\", \"include\": true}";
    final String p_field_inclusion_recursive = "{\"field\":\"field.x\",\"include\": true, \"recursive\": true}";
    final String p_field_exclusion = "{\"field\":\"field.y.x\",\"include\": false}";
    final String p_field_exclusion_recursive = "{\"field\":\"field.y.x\",\"include\": false, \"recursive\": true}";
    final String p_array_match = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.z\",\"op\":\"$eq\",\"rvalue\":1},\"project\":{\"field\":\"member\"}}";
    final String p_array_range = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"project\":{\"field\":\"member\"}}";
    final String p_array_match_p = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.z\",\"op\":\"$eq\",\"rvalue\":1},\"projection\":{\"field\":\"member\"}}";
    final String p_array_range_p = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"projection\":{\"field\":\"member\"}}";
    final String p_list = "[{\"field\":\"field.x\", \"include\": true},{\"field\":\"field.x.z\",\"include\": true, \"recursive\": true},{\"field\":\"field.y\",\"include\": false, \"recursive\": true}]";

    @Test
    public void field_inclusion() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_field_inclusion));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.x.y")));
    }

    @Test
    public void field_inclusion_recursive() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_field_inclusion_recursive));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.implicit_inclusion, p.getFieldInclusion(new Path("field.x.y")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.z")));
    }

    @Test
    public void field_exclusion() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_field_exclusion));
        Assert.assertEquals(Projection.Inclusion.explicit_exclusion, p.getFieldInclusion(new Path("field.y.x")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.y.x.z")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.y")));
    }

    @Test
    public void field_exclusion_recursive() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_field_exclusion_recursive));
        Assert.assertEquals(Projection.Inclusion.explicit_exclusion, p.getFieldInclusion(new Path("field.y.x")));
        Assert.assertEquals(Projection.Inclusion.implicit_exclusion, p.getFieldInclusion(new Path("field.y.x.z")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.y")));
    }

    @Test
    public void array_match() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_array_match));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x.*.member")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.a")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.x.*.field.z")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.field.z")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.a")));
    }

    @Test
    public void array_range() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_array_range));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x.*.member")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.a")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.x.*.b")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.member")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.a")));
    }

    @Test
    public void array_match_p() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_array_match_p));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x.*.member")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.a")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.x.*.field.z")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.field.z")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.a")));
    }

    @Test
    public void array_range_p() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_array_range_p));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x.*.member")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.a")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.x.*.b")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.*.member")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.a")));
    }

    @Test
    public void projection_list() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(p_list));
        Assert.assertTrue(p instanceof ProjectionList);
        ProjectionList pl = (ProjectionList) p;
        //"[{\"field\":\"field.x\", \"include\": true},{\"field\":\"field.x.z\",\"include\": true, \"recursive\": true},{\"field\":\"field.y\",\"include\": false, \"recursive\": true}]";

        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field.x.z")));
        Assert.assertEquals(Projection.Inclusion.implicit_inclusion, p.getFieldInclusion(new Path("field.x.z.b")));
        Assert.assertEquals(Projection.Inclusion.explicit_exclusion, p.getFieldInclusion(new Path("field.y")));
        Assert.assertEquals(Projection.Inclusion.implicit_exclusion, p.getFieldInclusion(new Path("field.y.q")));
        Assert.assertEquals(Projection.Inclusion.undecided, p.getFieldInclusion(new Path("field.t")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.z")));
        Assert.assertTrue(p.isFieldRequiredToEvaluateProjection(new Path("field.x.z.b")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.y")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.y.q")));
        Assert.assertFalse(p.isFieldRequiredToEvaluateProjection(new Path("field.t")));
    }

    @Test
    public void explicit_inclusion_override() throws Exception {
        Projection p=Projection.fromJson(JsonUtils.json("[{\"field\":\"x\"},{\"field\":\"*\",\"recursive\":true}]"));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("x")));
    }
}
