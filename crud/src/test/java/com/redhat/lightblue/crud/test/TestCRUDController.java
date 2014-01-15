/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud.test;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.MetadataResolver;
import com.redhat.lightblue.crud.UpdateExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.util.JsonDoc;
import java.util.List;

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
    public CRUDInsertionResponse insert(MetadataResolver resolver, List<JsonDoc> documents, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDSaveResponse save(MetadataResolver resolver, List<JsonDoc> documents, boolean upsert, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDUpdateResponse update(MetadataResolver resolver, String entity, QueryExpression query, UpdateExpression update, Projection projection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CRUDFindResponse find(MetadataResolver resolver, String entity, QueryExpression query, Projection projection, Sort sort, Long from, Long to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
