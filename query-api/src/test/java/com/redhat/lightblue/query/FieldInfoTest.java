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

import java.util.List;

public class FieldInfoTest {

    private QueryExpression getq(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void test_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','op':'=','rvalue':'value'}").getQueryFields();
        check(fields, "x", "");
    }

    @Test
    public void test_nary_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','op':'$in','values':[1,2,3]}").getQueryFields();
        check(fields, "x", "");
    }

    @Test
    public void test_nary_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','op':'$in','rfield':'y'}").getQueryFields();
        check(fields, "x", "","y","");
    }

    @Test
    public void test_not_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$not':{'field':'x','op':'=','rvalue':'value'}}").getQueryFields();
        check(fields, "x", "");
    }

    @Test
    public void test_regex_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','regex':'value'}").getQueryFields();
        check(fields, "x", "");
    }

    @Test
    public void test_not_regex_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$not':{'field':'x','regex':'value'}}").getQueryFields();
        check(fields, "x", "");
    }

    @Test
    public void test_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'field':'x','op':'=','rfield':'y'}").getQueryFields();
        check(fields, "x", "", "y", "");
    }

    @Test
    public void test_not_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$not': {'field':'x','op':'=','rfield':'y'}}").getQueryFields();
        check(fields, "x", "", "y", "");
    }

    @Test
    public void test_not_regex_and_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'y','op':'=','rvalue':'value'}]}").getQueryFields();
        check(fields, "x", "", "y", "");
    }

    @Test
    public void test_not_regex_and_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$and':[{'$not':{'field':'x','regex':'value'}},{'field':'y','op':'=','rfield':'z'}]}").getQueryFields();
        check(fields, "x", "", "y", "", "z", "");
    }

    @Test
    public void test_array_elemMatch_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'array':'a','elemMatch':{'field':'x','op':'>=','rvalue':'value'}}").getQueryFields();
        check(fields, "a", "", "a.*.x", "a.*");
    }

    @Test
    public void test_not_array_elemMatch_value_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$not':{'array':'a','elemMatch':{'field':'x','op':'=','rvalue':'value'}}}").getQueryFields();
        check(fields, "a", "", "a.*.x", "a.*");
    }

    @Test
    public void test_array_elemMatch_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'array':'a','elemMatch':{'field':'x','op':'>=','rfield':'y'}}").getQueryFields();
        check(fields, "a", "", "a.*.x", "a.*", "a.*.y", "a.*");
    }

    @Test
    public void test_not_array_elemMatch_field_comparison() throws Exception {
        List<FieldInfo> fields = getq("{'$not':{'array':'a','elemMatch':{'field':'x','op':'=','rfield':'y'}}}").getQueryFields();
        check(fields, "a", "", "a.*.x", "a.*", "a.*.y", "a.*");
    }

    /**
     * @param fields the fields to check
     * @param paths pairs of paths where first in pair (odd index, i) is the
     * field path and the second (even index, i+1) is the context
     */
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
