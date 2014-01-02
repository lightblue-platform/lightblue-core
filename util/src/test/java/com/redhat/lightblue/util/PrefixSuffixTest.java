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

public class PrefixSuffixTest {

    @Test
    public void prefixText() {
        Path p = new Path("zero.one.two.three.four");
        Assert.assertEquals("", p.prefix(0).toString());
        Assert.assertEquals("zero", p.prefix(1).toString());
        Assert.assertEquals("zero.one", p.prefix(2).toString());
        Assert.assertEquals("zero.one.two", p.prefix(3).toString());
        Assert.assertEquals("zero.one.two.three", p.prefix(4).toString());
        Assert.assertEquals("zero.one.two.three.four", p.prefix(5).toString());
        Assert.assertEquals("zero.one.two.three.four", p.prefix(6).toString());
    }

    @Test
    public void negPrefixText() {
        Path p = new Path("zero.one.two.three.four");
        Assert.assertEquals("zero.one.two.three", p.prefix(-1).toString());
        Assert.assertEquals("zero.one.two", p.prefix(-2).toString());
        Assert.assertEquals("zero.one", p.prefix(-3).toString());
        Assert.assertEquals("zero", p.prefix(-4).toString());
        Assert.assertEquals("", p.prefix(-5).toString());
        Assert.assertEquals("", p.prefix(-6).toString());
    }

    @Test
    public void suffixText() {
        Path p = new Path("zero.one.two.three.four");
        Assert.assertEquals("", p.suffix(0).toString());
        Assert.assertEquals("four", p.suffix(1).toString());
        Assert.assertEquals("three.four", p.suffix(2).toString());
        Assert.assertEquals("two.three.four", p.suffix(3).toString());
        Assert.assertEquals("one.two.three.four", p.suffix(4).toString());
        Assert.assertEquals("zero.one.two.three.four", p.suffix(5).toString());
        Assert.assertEquals("zero.one.two.three.four", p.suffix(6).toString());
    }

    @Test
    public void negSuffixText() {
        Path p = new Path("zero.one.two.three.four");
        Assert.assertEquals("one.two.three.four", p.suffix(-1).toString());
        Assert.assertEquals("two.three.four", p.suffix(-2).toString());
        Assert.assertEquals("three.four", p.suffix(-3).toString());
        Assert.assertEquals("four", p.suffix(-4).toString());
        Assert.assertEquals("", p.suffix(-5).toString());
        Assert.assertEquals("", p.suffix(-6).toString());
    }
}
