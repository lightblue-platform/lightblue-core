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
        expected = new ArrayList<String>();
    }
    
    @Test(expected=InvalidPathException.class)
    public void leading_dot_in_path_throws_exception() {
        super.parse(".badPath");
    }
    
    @Test(expected=InvalidPathException.class)
    public void space_in_path_throws_exception() {
        super.parse("good.start.with bad space.");
    }
    
    @Test(expected=InvalidPathException.class)
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
