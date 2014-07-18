package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class IfTest {
    
    @Test
    public void testGetSetConditions() {
        If instance = new IfImpl();
        instance.setConditions(null);
        List expResult = null;
        List result = instance.getConditions();
        assertEquals(expResult, result);
        expResult = new ArrayList();
        instance.setConditions(expResult);
        result = instance.getConditions();
        assertEquals(expResult, result);        
    }

    public class IfImpl extends If {
        @Override
        public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
            throw new UnsupportedOperationException("Not supported");
        }
    }
    
}
