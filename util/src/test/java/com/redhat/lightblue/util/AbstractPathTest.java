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

import junit.framework.Assert;

import org.junit.Test;

/**
 * Implementing classes should setup arrays of data to suite their data and implement createPath().
 *
 * @author nmalik
 *
 * @param <T>
 */
public abstract class AbstractPathTest<T extends Path> {

    protected String[] segments = null;
    protected boolean[] isIndex = null;

    private String expectedHead(int x) {
        return segments == null ? null : segments[x];
    }

    private String expectedTail(int x) {
        return segments == null ? null : segments[segments.length - x - 1];
    }

    private int expectedSize() {
        return segments == null ? 0 : segments.length;
    }

    private boolean expectedIsIndex(int x) {
        return isIndex == null ? null : isIndex[x];
    }

    protected String createPathString(String[] tokens) {
        boolean first = true;
        StringBuilder buff = new StringBuilder();
        for (String s : tokens) {
            if (first) {
                first = false;
            } else {
                buff.append(".");
            }
            buff.append(s);
        }
        return buff.toString();
    }

    protected final void assertEqual(T source, Path copy) {
        Assert.assertTrue(copy.equals(source));

        Assert.assertEquals(source.numSegments(), copy.numSegments());
        for (int i = 0; i < source.numSegments(); i++) {
            Assert.assertEquals(source.head(i), copy.head(i));
        }
    }

    public abstract T createPath();

    @Test
    public void getLast() {
        T p = createPath();
        int x = 0;
        String expected = expectedTail(x);
        try {
            Assert.assertEquals(expected, p.getLast());
        } catch (IndexOutOfBoundsException e) {
            if (null == expected) {
                // this is ok
            } else {
                // not ok, throw the exception to force failure
                throw e;
            }
        }
    }

    @Test
    public void tail0() {
        T p = createPath();
        int x = 0;
        String expected = expectedTail(x);
        try {
            Assert.assertEquals(expected, p.tail(x));
        } catch (IndexOutOfBoundsException e) {
            if (null == expected) {
                // this is ok
            } else {
                // not ok, throw the exception to force failure
                throw e;
            }
        }
    }

    @Test
    public void head0() {
        T p = createPath();
        int x = 0;
        String expected = expectedHead(x);
        try {
            Assert.assertEquals(expected, p.head(x));
        } catch (IndexOutOfBoundsException e) {
            if (null == expected) {
                // this is ok
            } else {
                // not ok, throw the exception to force failure
                throw e;
            }
        }
    }

    @Test
    public void head() {
        T p = createPath();
        for (int i = 0; i < p.numSegments(); i++) {
            Assert.assertEquals(expectedHead(i), p.head(i));
        }
    }

    @Test
    public void tail() {
        T p = createPath();
        for (int i = 0; i < p.numSegments(); i++) {
            Assert.assertEquals(expectedTail(i), p.tail(i));
        }
    }

    @Test
    public void isIndex() {
        T p = createPath();
        for (int i = 0; i < p.numSegments(); i++) {
            Assert.assertEquals(expectedIsIndex(i), p.isIndex(i));
        }
    }

    @Test
    public void getIndex() {
        T p = createPath();
        boolean tested = p.numSegments() == 0;
        for (int i = 0; i < p.numSegments(); i++) {
            if (p.isIndex(i)) {
                int x = Integer.valueOf(expectedHead(i));
                Assert.assertEquals(x, p.getIndex(i));
                tested = true;
            }
        }
        Assert.assertTrue("wasn't tested", tested);
    }

    @Test
    public void prefix_1() {
        T p = createPath();
        int x = -1;
        try {
            Path prefix = p.prefix(x);

            Assert.assertEquals(Math.max(0, p.numSegments() + x), prefix.numSegments());
            for (int i = 0; i < prefix.numSegments(); i++) {
                Assert.assertEquals(p.head(i), prefix.head(i));
            }
        } catch (IndexOutOfBoundsException e) {
            if (0 == expectedSize()) {
                // this is ok
            } else {
                // not ok, throw the exception to force failure
                throw e;
            }
        }
    }

    @Test
    public void prefix1() {
        T p = createPath();
        int x = 1;
        try {
            Path prefix = p.prefix(x);

            if (p.numSegments() > 0) {
                Assert.assertEquals(x, prefix.numSegments());
                for (int i = 0; i < prefix.numSegments(); i++) {
                    Assert.assertEquals(p.head(i), prefix.head(i));
                }
            } else {
                Assert.assertEquals(0, prefix.numSegments());
            }
        } catch (IndexOutOfBoundsException e) {
            if (0 == expectedSize()) {
                // this is ok
            } else {
                // not ok, throw the exception to force failure
                throw e;
            }
        }
    }

    @Test
    public void compareToPathRep() {
        T p = createPath();

        // better match self..
        Path same = new Path(p);
        Assert.assertTrue(p.matches(same));

        // equals
        Assert.assertEquals(0, p.getData().compareTo(same.getData()));

        if (!p.isEmpty()) {
            // less than
            Path less = p.mutableCopy().pop();
            Assert.assertTrue(p.getData().compareTo(less.getData()) > 0);
        }

        // greater than
        Path more = new Path(p, new Path("new"));
        Assert.assertTrue(p.getData().compareTo(more.getData()) < 0);
    }

    @Test
    public void matches_true() {
        T p = createPath();

        // better match self..
        Path pattern = new Path(p);
        Assert.assertTrue(p.matches(pattern));

        if (expectedSize() > 0) {
            // replace each node in the path with the ANY wildcard, one at a time.
            for (int i = 0; i < p.numSegments(); i++) {
                String[] tokens = new String[p.numSegments()];
                for (int x = 0; x < p.numSegments(); x++) {
                    tokens[x] = expectedHead(x);
                }
                tokens[i] = Path.ANY;
                String pathString = createPathString(tokens);
                pattern = new Path(pathString);
                // verify nAnys count
                Assert.assertEquals(1, pattern.nAnys());
                // verify ANY in match
                Assert.assertTrue(p.matches(pattern));
            }
        }
    }

    @Test
    public void matches_false_sameLength() {
        T p = createPath();

        // better match self..
        Path pattern = new Path(p);
        Assert.assertTrue(p.matches(pattern));

        if (expectedSize() > 0) {
            // replace last node with something else and match should fail
            Path other = new Path(p.toString() + "NEW");
            Assert.assertFalse(p.matches(other));
        }
    }

    @Test
    public void matches_false_differentLength() {
        T p = createPath();

        // better match self..
        Path pattern = new Path(p);
        Assert.assertTrue(p.matches(pattern));

        if (expectedSize() > 0) {
            // replace last node with something else and match should fail
            Path other = new Path(p.toString() + ".new");
            Assert.assertFalse(p.matches(other));
        }
    }

    @Test
    public void suffix() {
        T p = createPath();

        // suffix of full path should match self
        Path pattern = p.suffix(p.numSegments());
        Assert.assertTrue(p.matches(pattern));

        if (expectedSize() > 0) {
            // grab last element in the path
            Path suffix = p.suffix(1);

            if (p instanceof MutablePath) {
                Assert.assertTrue(suffix instanceof MutablePath);
            }
        }
    }

    @Test
    public void notEqual() {
        T original = createPath();
        Path modified = new Path(original, new Path("new"));

        Assert.assertFalse(original.equals(modified));
    }

    @Test
    public void copy() {
        T p = createPath();
        Path copy = p.copy();

        assertEqual(p, copy);
    }

    @Test
    public void immutableCopy() {
        T p = createPath();
        Path copy = p.immutableCopy();

        assertEqual(p, copy);
    }

    @Test
    public void mutableCopy() {
        T p = createPath();
        MutablePath copy = p.mutableCopy();

        assertEqual(p, copy);
    }

    @Test
    public void numSegments() {
        T p = createPath();

        Assert.assertEquals(expectedSize(), p.numSegments());
    }

    @Test
    public void add() {
        T p1 = createPath();

        if (segments != null) {
            T p2 = createPath();

            Path concat1 = p1.add(p2);
            Path concat2 = new Path(p1.toString() + "." + p2.toString());
            Assert.assertEquals(concat1.toString(), concat2.toString());
            Assert.assertTrue(concat1.equals(concat2));
        } else {
            // for an empty original path expect the added path to simply replace the original
            Path p2 = new Path("not.empty");

            Path concat = p1.add(p2);
            Assert.assertEquals(p2.toString(), concat.toString());
            Assert.assertTrue(p2.equals(concat));
        }
    }

    @Test
    public void matchingDescendant_true_smallerLength() {
        T p = createPath();

        if (segments != null) {
            Path prefix = p.prefix(3);
            Assert.assertTrue(p.matchingDescendant(prefix));
        }
    }

    @Test
    public void matchingDescendant_true_sameLength() {
        T p = createPath();

        Assert.assertTrue(p.matchingDescendant(new Path(p.toString())));
    }

    @Test
    public void matchingDescendant_false_smallerLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = new Path(p.prefix(p.numSegments() / 2).toString() + ".nope");

            Assert.assertFalse(p.matchingDescendant(compare));
        }
    }

    @Test
    public void matchingDescendant_false_longerLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = p.add(new Path("nope"));

            Assert.assertFalse(p.matchingDescendant(compare));
        }
    }

    @Test
    public void matchingDescendant_false_sameLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = p.mutableCopy().setLast("nope");

            Assert.assertFalse(p.matchingDescendant(compare));
        }
    }

    @Test
    public void matchingPrefix_true_smallerLength() {
        T p = createPath();

        if (segments != null) {
            Path prefix = p.prefix(3);
            Assert.assertTrue(prefix.matchingPrefix(p));
        }
    }

    @Test
    public void matchingPrefix_true_sameLength() {
        T p = createPath();

        Assert.assertTrue(new Path(p.toString()).matchingPrefix(p));
    }

    @Test
    public void matchingPrefix_false_smallerLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = new Path(p.prefix(p.numSegments() / 2).toString() + ".nope");

            Assert.assertFalse(compare.matchingPrefix(p));
        }
    }

    @Test
    public void matchingPrefix_false_longerLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = p.add(new Path("nope"));

            Assert.assertFalse(compare.matchingPrefix(p));
        }
    }

    @Test
    public void matchingPrefix_false_sameLength() {
        T p = createPath();

        if (segments != null) {
            Path compare = p.mutableCopy().setLast("nope");

            Assert.assertFalse(compare.matchingPrefix(p));
        }
    }

}
