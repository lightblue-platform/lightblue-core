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

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class EmptyIteratorTest {

    @Test
    public void hasNext() {
        EmptyIterator<Object> i = new EmptyIterator<>();

        Assert.assertFalse(i.hasNext());
    }

    @Test
    public void next() {
        EmptyIterator<Object> i = new EmptyIterator<>();
        try {
            i.next();
            Assert.fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void remove() {
        EmptyIterator<Object> i = new EmptyIterator<>();
        try {
            i.remove();
            Assert.fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }
}
