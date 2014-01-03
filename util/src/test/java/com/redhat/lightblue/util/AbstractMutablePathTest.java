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

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public abstract class AbstractMutablePathTest extends AbstractPathTest<MutablePath> {

    @Test
    public void pushNull() {
        MutablePath p = createPath();

        try {
            p.push((String) null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void pushString() {
        MutablePath p = createPath();

        String[] newSegments = null;
        if (segments != null) {
            newSegments = Arrays.copyOf(segments, segments.length + 1);
        } else {
            newSegments = new String[1];
        }
        String pushed = "q";
        newSegments[newSegments.length - 1] = pushed;

        p.push(pushed);

        assertEqual(p, new Path(createPathString(newSegments)));
    }

    @Test
    public void pushPath() {
        MutablePath p = createPath();

        String[] newSegments = null;
        if (segments != null) {
            newSegments = Arrays.copyOf(segments, segments.length + 1);
        } else {
            newSegments = new String[1];
        }
        String pushed = "q";
        newSegments[newSegments.length - 1] = pushed;

        p.push(new Path(pushed));

        assertEqual(p, new Path(createPathString(newSegments)));
    }

    @Test
    public void pushInt() {
        MutablePath p = createPath();

        String[] newSegments = null;
        if (segments != null) {
            newSegments = Arrays.copyOf(segments, segments.length + 1);
        } else {
            newSegments = new String[1];
        }
        String pushed = "1";
        newSegments[newSegments.length - 1] = pushed;

        p.push(1);

        assertEqual(p, new Path(createPathString(newSegments)));
    }

    @Test
    public void pushIntNegative() {
        MutablePath p = createPath();

        String[] newSegments = null;
        if (segments != null) {
            newSegments = Arrays.copyOf(segments, segments.length + 1);
        } else {
            newSegments = new String[1];
        }
        String pushed = "-1";
        newSegments[newSegments.length - 1] = pushed;

        p.push(-1);

        assertEqual(p, new Path(createPathString(newSegments)));
    }

    @Test
    public void pop() {
        MutablePath p = createPath();

        if (segments != null) {
            String[] newSegments = Arrays.copyOf(segments, segments.length - 1);
            p.pop();
            assertEqual(p, new Path(createPathString(newSegments)));
        } else {
            try {
                p.pop();
                Assert.fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }
    
    
    @Test
    public void cut() {
        MutablePath p = createPath();
        String[] newSegments = null;
        int cutLength = 2;
        
        if (segments != null) {
            newSegments = Arrays.copyOf(segments, cutLength);
        } else {
            newSegments = new String[0];
        }
        
        // cutting when there are no segments does nothing
        p.cut(cutLength);
        assertEqual(p, new Path(createPathString(newSegments)));
    }


    @Test
    public void setLastString() {
        MutablePath p = createPath();
        String last = "last";

        if (segments != null) {
            String[] newSegments = Arrays.copyOf(segments, segments.length);
            newSegments[newSegments.length - 1] = last;
            p.setLast(last);
            assertEqual(p, new Path(createPathString(newSegments)));
        } else {
            try {
                p.setLast(last);
                Assert.fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }

    @Test
    public void setLastInt() {
        MutablePath p = createPath();
        int lastInt = 1;
        String lastString = String.valueOf(lastInt);

        if (segments != null) {
            String[] newSegments = Arrays.copyOf(segments, segments.length);
            newSegments[newSegments.length - 1] = lastString;
            p.setLast(lastInt);
            assertEqual(p, new Path(createPathString(newSegments)));
        } else {
            try {
                p.setLast(lastInt);
                Assert.fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }
}
