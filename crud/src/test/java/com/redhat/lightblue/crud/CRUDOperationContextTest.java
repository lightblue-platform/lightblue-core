/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
                                List<JsonDoc> docs
         */
        CRUDOperationContext context = new CRUDOperationContext(CRUDOperation.INSERT, "foo", new Factory(), null, null) {

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
