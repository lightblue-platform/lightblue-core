/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import java.util.Iterator;
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

    public abstract T getRootNode();

    public abstract Iterator<T> getChildren(T node);

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
        Assert.assertNotSame(getRootNode(), cursor.getCurrentNode());

        // verify root node's first child IS the current node
        Iterator<T> itr = getChildren(getRootNode());
        if (itr != null && itr.hasNext()) {
            Assert.assertEquals(itr.next(), cursor.getCurrentNode());
        }
    }

    @Test
    public void nextSibling() {
        // test is only valid if there are more than one child node
        Iterator<T> itr = getChildren(getRootNode());
        int count = 0;
        T sibiling = null;
        while (itr != null && itr.hasNext()) {
            if (1 == count++) {
                // this is the first sibling
                sibiling = itr.next();
            } else {
                // don't assign, just iterate
                itr.next();
            }
        }

        Assert.assertTrue("Not enough nodes to test sibiling", count > 1);
        Assert.assertNotNull("no sibling to compare with", sibiling);

        // get first child, so can start getting siblings
        Assert.assertTrue(cursor.firstChild());
        Assert.assertNotNull(cursor.getCurrentNode());
        T firstChild = cursor.getCurrentNode();

        // get first sibling and verify
        Assert.assertTrue(cursor.nextSibling());
        Assert.assertNotNull(cursor.getCurrentNode());
        Assert.assertNotSame(firstChild, cursor.getCurrentNode());
        Assert.assertSame(sibiling, cursor.getCurrentNode());

        // consume ALL siblings and verify next sibling returns false after that.
        // use count in case the loop is broken and becomes infinite
        count -= 2;
        while (count > 0) {
            cursor.nextSibling();
        }
        Assert.assertFalse(cursor.nextSibling());
    }
    
    @Test
    public void parent() {
        // find child that has children
        boolean foundChild = false;
        
        Assert.assertTrue(cursor.firstChild());
        T parent = cursor.getCurrentNode();
        
        do {
            Iterator<T> itr = getChildren(cursor.getCurrentNode());
            if (itr != null && itr.hasNext()) {
                foundChild = true;
            }
        } while(!foundChild && cursor.nextSibling());
        
        Assert.assertTrue("Didn't find a valid node to test parent()", foundChild);
        
        // get the actual child (firstChild) of current node.  Will be first child of first child of root.
        Assert.assertTrue(cursor.firstChild());
        
        // get the parent and verify is root (grandparent of previous node)
        Assert.assertTrue(cursor.parent());
        Assert.assertSame(getRootNode(), cursor.getCurrentNode());
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
