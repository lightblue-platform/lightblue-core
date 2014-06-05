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
package com.redhat.lightblue.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PathParseTest extends Path {

    private static final long serialVersionUID = 1L;
    private List<String> expected = null;

    @Before
    public void setup() {
        expected = new ArrayList<>();
    }

    @Test(expected = InvalidPathException.class)
    public void leading_dot_in_path_throws_exception() {
        super.parse(".badPath");
    }

    @Test(expected = InvalidPathException.class)
    public void space_in_path_throws_exception() {
        super.parse("good.start.with bad space.");
    }

    @Test(expected = InvalidPathException.class)
    public void two_spaces_at_end_of_path_segment_throws_exception() {
        super.parse("good.start.with  .");
    }

    @Test
    public void leading_space_in_path_removed_and_ok() {
        expected.add("good");
        expected.add("path");
        expected.add("here");

        Assert.assertEquals(expected, super.parse(" good.path.here"));
    }

    @Test
    public void trailing_space_in_path_removed_and_ok() {
        expected.add("good");
        expected.add("path");
        expected.add("here");

        Assert.assertEquals(expected, super.parse("good.path.here "));
    }

    @Test
    public void white_space_in_path_removed_and_ok() {
        expected.add("good");
        expected.add("path");
        expected.add("here");

        Assert.assertEquals(expected, super.parse("good.path .here"));
    }

    @Test
    public void normal_path_is_okay() {
        expected.add("good");
        expected.add("path");
        expected.add("with");
        expected.add("no");
        expected.add("array");

        Assert.assertEquals(expected, super.parse("good.path.with.no.array"));
    }

    @Test
    public void normal_path_with_array_indices_is_okay() {
        expected.add("good");
        expected.add("path");
        expected.add("with");
        expected.add("1");
        expected.add("array");

        Assert.assertEquals(expected, super.parse("good.path.with.1.array"));
    }

}
