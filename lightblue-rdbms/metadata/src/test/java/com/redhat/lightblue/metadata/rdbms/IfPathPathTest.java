package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfPathPathTest {
    
    public IfPathPathTest() {
    }

    @Test
    public void testGetSetPath1() {
        Path expResult = null;
        IfPathPath instance = new IfPathPath();
        instance.setPath1(expResult);
        Path result = instance.getPath1();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setPath1(expResult);
        result = instance.getPath1();
        assertEquals(expResult, result);
    }


    @Test
    public void testGetSetPath2() {
        Path expResult = null;
        IfPathPath instance = new IfPathPath();
        instance.setPath2(expResult);
        Path result = instance.getPath2();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setPath2(expResult);
        result = instance.getPath2();
        assertEquals(expResult, result);
    }


    @Test
    public void testGetSetConditional() {
        String expResult = "lessThan";
        IfPathPath instance = new IfPathPath();
        instance.setConditional(expResult);
        String result = instance.getConditional();
        assertEquals(expResult, result);
        expResult = "equalTo";
        instance.setConditional(expResult);
        result = instance.getConditional();
        assertEquals(expResult, result);
    }
}
