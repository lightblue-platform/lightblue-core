package com.redhat.lightblue.metadata.constraints;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.util.Path;

public class UniqueConstraintTest {

    UniqueConstraint constraint;

    List<Path> paths;

    @Before
    public void setUp() throws Exception {
        constraint = new UniqueConstraint();
        Path path = new Path();
        path.add(Path.ANYPATH);
        paths = new ArrayList<Path>();
        constraint.setFields(paths);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetType() {
        Assert.assertTrue(constraint.getType().equals(UniqueConstraint.UNIQUE));
    }

    @Test
    public void testGetFields() {
        Assert.assertTrue(constraint.getFields().equals(paths));
    }

    @Test
    public void testSetFields() {
        Path path = new Path();
        path.add(Path.EMPTY);
        paths = new ArrayList<Path>();
        constraint.setFields(paths);
        Assert.assertTrue(constraint.getFields().equals(paths));
    }

    @Test
    public void testSetFieldsNull() {
        constraint.setFields(null);
        paths.clear();
        Assert.assertTrue(constraint.getFields().equals(paths));
    }
}
