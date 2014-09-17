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

import org.junit.Assert;
import org.junit.Test;

public class CopyOnWriteIteratorTest {

    @Test
    public void noCopy() {
        ArrayList<String> list=new ArrayList<>();
        for(int i=0;i<100;i++) 
            list.add(Integer.toString(i));

        int i=0;
        CopyOnWriteIterator itr=new CopyOnWriteIterator(list);
        for(;itr.hasNext(); )
            Assert.assertEquals(Integer.toString(i++),itr.next());

        Assert.assertFalse(itr.isCopied());
        Assert.assertNull(itr.getCopiedList());
    }

    @Test
    public void oneMod() {
        ArrayList<String> list=new ArrayList<>();
        for(int i=0;i<100;i++) 
            list.add(Integer.toString(i));

        int i=0;
        CopyOnWriteIterator itr=new CopyOnWriteIterator(list);
        for(;itr.hasNext(); ) {
            Assert.assertEquals(Integer.toString(i++),itr.next());
            if(i==50)
                itr.set("changed");
        }

        Assert.assertTrue(itr.isCopied());
        List<String> newList=itr.getCopiedList();
        Assert.assertNotNull(newList);

        i=0;
        for(String x:list) 
            Assert.assertEquals(Integer.toString(i++),x);

        i=0;
        for(String x:newList) {
            if(i==49)
                Assert.assertEquals("changed",x);
            else
                Assert.assertEquals(Integer.toString(i),x);
            i++;
        }
    }
}
