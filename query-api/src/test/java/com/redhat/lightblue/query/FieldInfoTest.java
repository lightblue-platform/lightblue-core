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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;

public class FieldInfoTest {

    private QueryExpression getq(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void dotest() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','op':'=','rvalue':'value'}").getQueryFields();
        check(fields, "x", "");

        fields = getq("{'$not':{'field':'x','op':'=','rvalue':'value'}}").getQueryFields();
        check(fields, "x", "");

        fields = getq("{'$not':{'field':'x','regex':'value'}}").getQueryFields();
        check(fields, "x", "");

        fields = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'y','op':'=','rvalue':'value'}]}").getQueryFields();
        check(fields, "x", "", "y", "");

        fields = getq("{'$not':{'array':'x','elemMatch':{'field':'y','op':'=','rvalue':'value'}}}").getQueryFields();
        check(fields, "x", "", "x.*.y", "x.*");

        fields = getq("{'field':'x','op':'=','rfield':'y'}").getQueryFields();
        check(fields, "x", "", "y", "");

        fields = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'y','op':'=','rfield':'z'}]}").getQueryFields();
        check(fields, "x", "", "y", "", "z", "");

        fields = getq("{'array':'a','elemMatch':{'field':'x','op':'>=','rfield':'y'}}").getQueryFields();
        check(fields, "a", "", "a.*.x", "a.*", "a.*.y", "a.*");
    }

    private void check(List<FieldInfo> fields, String... paths) {
        int n = paths.length / 2;
        Assert.assertEquals(n, fields.size());
        for (int i = 0; i < n; i += 2) {
            Path field = new Path(paths[i]);
            Path ctx = new Path(paths[i + 1]);
            boolean found = false;
            for (FieldInfo fi : fields) {
                if (fi.getAbsFieldName().equals(field) && fi.getContext().equals(ctx)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found);
        }
    }
}
