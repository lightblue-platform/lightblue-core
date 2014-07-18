package com.redhat.lightblue.metadata.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElseIfTest {
 
     @Test
    public void testGetSetIf() {
        If expResult = null;
        ElseIf instance = new ElseIf();
        instance.setIf(expResult);
        If result = instance.getIf();
        assertEquals(expResult, result);
        expResult = new IfPathEmpty();
        instance.setIf(expResult);
        result = instance.getIf();
        assertEquals(expResult, result);    
    }

    @Test
    public void testGetSetThen() {
        System.out.println("setThen");
        Then expResult = null;
        ElseIf instance = new ElseIf();
        instance.setThen(expResult);
        Then result = instance.getThen();
        assertEquals(expResult, result);
        expResult = new Then();
        instance.setThen(expResult);
        result = instance.getThen();
        assertEquals(expResult, result);
    }
}
