package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.util.Path;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfPathEmptyTest {
    
    public IfPathEmptyTest() {
    }

    @Test
    public void testGetSetPath1() {
        Path expResult = null;
        IfPathEmpty instance = new IfPathEmpty();
        instance.setPath1(expResult);
        Path result = instance.getPath1();
        assertEquals(expResult, result);
        expResult = Path.ANYPATH;
        instance.setPath1(expResult);
        result = instance.getPath1();
        assertEquals(expResult, result);
    }
  
}
