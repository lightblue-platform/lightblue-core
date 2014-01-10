package com.redhat.lightblue.metadata.constraints;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
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
        assertTrue(constraint.getType().equals(UniqueConstraint.UNIQUE));
    }

    @Test
    public void testGetFields() {
        assertTrue(constraint.getFields().equals(paths));
    }

    @Test
    public void testSetFields() {
        Path path = new Path();
        path.add(Path.EMPTY);
        paths = new ArrayList<Path>();
        constraint.setFields(paths);
        assertTrue(constraint.getFields().equals(paths));
    }

    @Test
    public void testSetFieldsNull() {
        constraint.setFields(null);
        paths.clear();
        assertTrue(constraint.getFields().equals(paths));
    }
}
