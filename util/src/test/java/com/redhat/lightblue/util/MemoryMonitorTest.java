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

}
