/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public abstract class AbstractTreeCursorTest<T> {

    private AbstractTreeCursor<T> cursor;
    protected Path path = null;

    @Before
    public void setup() {
        cursor = createCursor(path);
    }

    @After
    public void tearDown() {
        cursor = null;
    }

    public abstract AbstractTreeCursor<T> createCursor(Path p);

    /*
     ABSTRACT
     nextSibling
     parent
     next
    
     JSONNODE
     getCursor
     hasChildren
     */
    @Test
    public void firstChild() {
        Assert.assertTrue(cursor.firstChild());
        Assert.assertNotNull(cursor.getCurrentNode());
    }

    @Test
    public void nextSibling() {
        Assert.assertTrue(cursor.firstChild());
        Assert.assertTrue(cursor.nextSibling());
        Assert.assertNotNull(cursor.getCurrentNode());
        while (cursor.hasChildren(cursor.getCurrentNode()) && cursor.nextSibling()) {
        }
        Assert.assertFalse(cursor.nextSibling());
    }

    @Test
    public void next() {
        Assert.assertTrue(cursor.next());
        Assert.assertNotNull(cursor.getCurrentNode());
        while (cursor.hasChildren(cursor.getCurrentNode()) && cursor.next()) {
            Assert.assertNotNull(cursor.getCurrentNode());
        }
        // this ends up popping the path. so just verify there's a current node
        Assert.assertNotNull(cursor.getCurrentNode());
    }

    @Test
    public void getCurrentPath() {
        Path p = cursor.getCurrentPath();
        Assert.assertNotNull(p);
        Assert.assertFalse(p instanceof MutablePath);
    }
}
