package com.redhat.lightblue.mediator;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.extensions.Extension;
import com.redhat.lightblue.extensions.ExtensionSupport;
import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;
import com.redhat.lightblue.mediator.AbstractMediatorTest.MockValueGeneratorSupport;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;

public final class MockCrudController implements CRUDController, ExtensionSupport {
    CRUDUpdateResponse updateResponse;
    CRUDSaveResponse saveResponse;
    CRUDDeleteResponse deleteResponse;
    CRUDFindResponse findResponse;
    CRUDInsertionResponse insertResponse;
    CRUDOperationContext ctx;

    Callback insertCb=x->{};
    Callback saveCb=x->{};
    Callback updateCb=x->{};
    Callback deleteCb=x->{};
    Callback findCb=x->{};

    @FunctionalInterface
    public interface Callback {
        void cb(CRUDOperationContext ctx);
    }

    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                        Projection projection) {
        this.ctx = ctx;
        insertCb.cb(ctx);
        return insertResponse;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 boolean upsert,
                                 Projection projection) {
        this.ctx = ctx;
        saveCb.cb(ctx);
        return saveResponse;
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {
        this.ctx = ctx;
        updateCb.cb(ctx);
        return updateResponse;
    }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                     QueryExpression query) {        
        deleteCb.cb(ctx);
        return deleteResponse;
    }

    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx,
                                 QueryExpression query,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        findCb.cb(ctx);
        return findResponse;
    }

    @Override
    public MetadataListener getMetadataListener() {
        return null;
    }

    @Override
    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {
    }

    @Override
    public <E extends Extension> E getExtensionInstance(Class<? extends Extension> extensionClass) {
        if (extensionClass.equals(ValueGeneratorSupport.class)) {
            return (E) new MockValueGeneratorSupport();
        } else {
            return null;
        }
    }

}
