/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.common.rdbms.RDBMSContext;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.JsonDoc;
import java.util.ArrayList;

/**
 *
 * @author lcestari
 */
public class RDBMSProcessor {
    public static void process(CRUDOperationContext crudOperationContext, QueryExpression queryExpression,RDBMSContext rdbms){
        ArrayList<JsonDoc> result = new ArrayList<JsonDoc>();
        crudOperationContext.addDocuments(result);
    }
    
}
