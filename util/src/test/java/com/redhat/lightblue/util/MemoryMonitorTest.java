package com.redhat.lightblue.util;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.lightblue.util.MemoryMonitor.ThresholdMonitor;


public class MemoryMonitorTest {

    @Test
    public void testSizeGrowing() {

        String el1 = "foobar";
        String el2 = "foo";
        String el3 = "bar";

        MemoryMonitor<String> m = new MemoryMonitor<>( (el) ->  el.length());

        Assert.assertEquals(0, m.getDataSizeB());
        m.apply(el1);
        Assert.assertEquals(el1.length(), m.getDataSizeB());
        m.apply(el2);
        Assert.assertEquals(el1.length()+el2.length(), m.getDataSizeB());
        m.apply(el3);
        Assert.assertEquals(el1.length()+el2.length()+el3.length(), m.getDataSizeB());
    }

    @Test
    public void testThresholds() {

        String el1 = "foobar";
        String el2 = "foo";
        String el3 = "bar";

        MemoryMonitor<String> list = new MemoryMonitor<>( (el) ->  el.length());

        int fired[] = new int[2];
        fired[0] = 0;
        fired[1] = 0;

        list.registerMonitor(new ThresholdMonitor<String>(el1.length()-1, (size, threshold, str) ->{
            fired[0] += 1;
        }));

        list.registerMonitor(new ThresholdMonitor<String>(el1.length()+el2.length()+1, (size, threshold, str) ->{
            fired[1] += 1;
        }));

        Assert.assertTrue(fired[0]==0 && fired[1]==0);

        list.apply(el1);

        Assert.assertTrue(fired[0]==1 && fired[1]==0);

        list.apply(el2);

        Assert.assertTrue(fired[0]==1 && fired[1]==0);

        list.apply(el3);

        Assert.assertTrue(fired[0]==1 && fired[1]==1);
    }

    @Test
    public void testSizeDoesNotGrowIfSameReferenceCounted() {
        String el1 = "foobar";

        MemoryMonitor<String> m = new MemoryMonitor<>( (el) ->  el.length());

        Assert.assertEquals(0, m.getDataSizeB());
        m.apply(el1);
        Assert.assertEquals(el1.length(), m.getDataSizeB());
        m.apply(el1);
        Assert.assertEquals(el1.length(), m.getDataSizeB());
    }

    @Test
    public void testSizeGrowingIfEqualButNotSameReference() {
        SomeType el1 = new SomeType("1");
        SomeType el2 = new SomeType("1");
        SomeType el3 = new SomeType("1");

        MemoryMonitor<SomeType> m = new MemoryMonitor<>(el -> el.value.length());

        Assert.assertEquals(0, m.getDataSizeB());
        m.apply(el1);
        Assert.assertEquals(el1.value.length(), m.getDataSizeB());
        m.apply(el2);
        Assert.assertEquals(el1.value.length() * 2, m.getDataSizeB());
        m.apply(el3);
        Assert.assertEquals(el1.value.length() * 3, m.getDataSizeB());
    }

    class SomeType {
        String value;

        public SomeType(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SomeType someType = (SomeType) o;

            return value != null ? value.equals(someType.value) : someType.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
