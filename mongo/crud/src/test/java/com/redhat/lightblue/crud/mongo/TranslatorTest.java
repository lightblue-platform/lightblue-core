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
package com.redhat.lightblue.crud.mongo;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.mongodb.DBObject;
import com.redhat.lightblue.crud.Operation;
import static com.redhat.lightblue.crud.mongo.AbstractMongoTest.nodeFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.UpdateExpression;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class TranslatorTest extends AbstractMongoTest {
    private TestCRUDOperationContext ctx;
    private Translator translator;
    private EntityMetadata md;

    @Before
    public void setup() throws IOException, ProcessingException {
        ctx = new TestCRUDOperationContext(Operation.FIND);
        // load metadata 
        md = getMd("./testMetadata.json");
        // and add it to metadata resolver (the context)
        ctx.add(md);
        // create translator with the context
        translator = new Translator(ctx, nodeFactory);
    }

    @Test
    public void translateUpdateSetField() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-set-field.json");
        UpdateExpression ue = update(updateQueryJson);
        DBObject mongoUpdateExpr = translator.translate(md, ue);

        Assert.assertNotNull(mongoUpdateExpr);
    }

    @Test
    public void translateUpdateAddField() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-add-field.json");
        UpdateExpression ue = update(updateQueryJson);
        DBObject mongoUpdateExpr = translator.translate(md, ue);

        Assert.assertNotNull(mongoUpdateExpr);
    }

    @Test
    public void translateUpdateUnsetField() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-unset-field.json");
        UpdateExpression ue = update(updateQueryJson);
        DBObject mongoUpdateExpr = translator.translate(md, ue);

        Assert.assertNotNull(mongoUpdateExpr);
    }

    @Test
    public void translateUpdateUnsetNestedArrayElement() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-unset-nested-array-element.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateUnsetNestedField() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-unset-nested-field.json");
        UpdateExpression ue = update(updateQueryJson);
        DBObject mongoUpdateExpr = translator.translate(md, ue);

        Assert.assertNotNull(mongoUpdateExpr);
    }
    /*
     array_update_expression := { $append : { path : rvalue_expression } } |  
     { $append : { path : [ rvalue_expression, ... ] }} |
     { $insert : { path : rvalue_expression } } |  
     { $insert : { path : [ rvalue_expression,...] }} |  
     { $foreach : { path : update_query_expression,   
     $update : foreach_update_expression } }
     */

    @Test
    public void translateUpdateAppendValue() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-append-value.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateAppendValues() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-append-values.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateInsertValue() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-insert-value.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateInsertValues() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-insert-values.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateForeachSimple() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-foreach-simple.json");
        UpdateExpression ue = update(updateQueryJson);
        try {
            DBObject mongoUpdateExpr = translator.translate(md, ue);
            Assert.fail("Expected CannotTranslateException");
        } catch (CannotTranslateException e) {
            // expected
        }
    }

    @Test
    public void translateUpdateListSetField() throws Exception {
        String updateQueryJson = loadResource(getClass().getSimpleName() + "-update-list-set-field.json");
        UpdateExpression ue = update(updateQueryJson);
        DBObject mongoUpdateExpr = translator.translate(md, ue);

        Assert.assertNotNull(mongoUpdateExpr);
    }
}
