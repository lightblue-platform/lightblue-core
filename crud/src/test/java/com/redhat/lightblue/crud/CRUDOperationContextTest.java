/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.crud;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.util.JsonDoc;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class CRUDOperationContextTest {
    
    @Test
    public void getOutputDocumentsWithoutErrors_nullOutputDocument() {
        /*
        String entityName,
                                Factory f,
                                Set<String> callerRoles,
                                List<JsonDoc> docs
        */
        CRUDOperationContext context =new CRUDOperationContext(Operation.INSERT, "foo", new Factory(), null, null) {
            
            @Override
            public EntityMetadata getEntityMetadata(String entityName) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        context.addDocument(new JsonDoc(null));
        
        // simulate no output document such as if no fields are projected
        context.getDocuments().get(0).setOutputDocument(null);
        
        Assert.assertTrue(context.getOutputDocumentsWithoutErrors().isEmpty());
    }
}
