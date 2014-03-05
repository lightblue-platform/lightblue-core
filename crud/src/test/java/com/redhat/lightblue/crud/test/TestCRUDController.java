/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud.test;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;

/**
 *
 * @author nmalik
 */
public class TestCRUDController implements CRUDController {

    public static TestCRUDController create(DatabaseConfiguration config) {
        return new TestCRUDController();
    }

    public TestCRUDController() {

    }

    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx, boolean upsert, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx, QueryExpression query, UpdateExpression update, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx, QueryExpression query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx, QueryExpression query, Projection projection, Sort sort, Long from, Long to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
