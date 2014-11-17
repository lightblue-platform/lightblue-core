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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class TuplesTest {

    @Test
    public void test2() {
        List<String> l1=new ArrayList<>();
        List<String> l2=new ArrayList<>();
        Collections.addAll(l1,"0","1","2","3","4","5","6","7","8","9");
        Collections.addAll(l2,"A","B","C","D","E","F","G","H","I","J");

        Tuples<String> t = new Tuples(l1, l2);
        Iterator<List<String>> itr = t.tuples();

        int count = 0;
        int expectedCount = l1.size() * l2.size();

        for (String x1 : l1) {
            for (String x2 : l2) {
                Assert.assertTrue(itr.hasNext());
                List<String> item = itr.next();
                Assert.assertEquals(x1, item.get(0));
                Assert.assertEquals(x2, item.get(1));
                count++;
            }
        }

        Assert.assertEquals(expectedCount, count);
    }

    @Test
    public void test4() {
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        List<String> l3 = new ArrayList<>();
        List<String> l4 = new ArrayList<>();
        Collections.addAll(l1, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        Collections.addAll(l2, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        Collections.addAll(l3, "10", "11", "12", "13", "14", "15", "16", "17", "18", "19");
        Collections.addAll(l4, "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ");

        int count = 0;
        int expectedCount = l1.size() * l2.size() * l3.size() * l4.size();

        Tuples<String> t = new Tuples(l1, l2, l3, l4);
        Iterator<List<String>> itr = t.tuples();

        for (String x1 : l1) {
            for (String x2 : l2) {
                for (String x3 : l3) {
                    for (String x4 : l4) {
                        Assert.assertTrue(itr.hasNext());
                        List<String> item = itr.next();
                        Assert.assertEquals(x1, item.get(0));
                        Assert.assertEquals(x2, item.get(1));
                        Assert.assertEquals(x3, item.get(2));
                        Assert.assertEquals(x4, item.get(3));
                        count++;
                    }
                }
            }
        }

        Assert.assertEquals(expectedCount, count);
    }
}
