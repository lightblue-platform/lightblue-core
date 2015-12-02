/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.mediator;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;

import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;

import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.JsonDoc;


public class TestCrudController implements CRUDController {

    private static final JsonNodeFactory nodeFactory=JsonNodeFactory.withExactBigDecimals(true);

    public interface GetData {
        List<JsonDoc> getData(String entityName);
    }

    private final GetData gd;

    public TestCrudController(GetData gd) {
        this.gd=gd;
    }

    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                        Projection projection) {return null;}

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 boolean upsert,
                                 Projection projection) {return null;}

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {return null;}

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                     QueryExpression query) {return null;}

    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx,
                                 QueryExpression query,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        QueryEvaluator eval=query==null?null:QueryEvaluator.getInstance(query,ctx.getEntityMetadata(ctx.getEntityName()));
        Projector projector=Projector.getInstance(projection,ctx.getEntityMetadata(ctx.getEntityName()));
        List<DocCtx> output=new ArrayList<>();
        for(JsonDoc doc:gd.getData(ctx.getEntityName())) {
            if(eval==null) {
                output.add(new DocCtx(projector.project(doc,nodeFactory)));
            } else {
                QueryEvaluationContext qctx=eval.evaluate(doc);
                if(qctx.getResult()) {
                    output.add(new DocCtx(projector.project(doc,nodeFactory)));
                }
            }
        }
        int f=from==null?0:from.intValue();
        int t=to==null?output.size():(to.intValue()+1);
        if(t<f||f>=output.size()) {
            output=new ArrayList<>();
        } else {
            if(t>output.size())
                t=output.size();
            output=output.subList(f,t);
        }
        ctx.setDocuments(output);
        CRUDFindResponse ret=new CRUDFindResponse();
        ret.setSize(output.size());
        return ret;
    }

    @Override
    public MetadataListener getMetadataListener() {
        return null;
    }

    @Override
    public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {

    }
}
