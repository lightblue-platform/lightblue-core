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

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

import com.mongodb.DBObject;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.crud.MetadataResolver;

public class TranslatorUpdateTest extends AbstractMongoTest {

    private Translator translator;

    @Before
    public void init() throws Exception {
        translator = new Translator(new MetadataResolver() {
            @Override
            public EntityMetadata getEntityMetadata(String entityName) {
                try {
                    return getMd("./testMetadata.json");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, nodeFactory);
    }

    @Test
    public void setTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$set': { 'field1':'blah', 'field2':'two'} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("blah", ((DBObject) obj.get("$set")).get("field1"));
        Assert.assertEquals("two", ((DBObject) obj.get("$set")).get("field2"));
    }

    @Test
    public void nestedSetTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$set': { 'field6.nf1':'blah', 'field6.nf2':'two'} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("blah", ((DBObject) obj.get("$set")).get("field6.nf1"));
        Assert.assertEquals("two", ((DBObject) obj.get("$set")).get("field6.nf2"));
    }

    @Test
    public void incTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$add': { 'field3':1, 'field4': -100} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("1", ((DBObject) obj.get("$inc")).get("field3").toString());
        Assert.assertEquals("-100", ((DBObject) obj.get("$inc")).get("field4").toString());
    }

    @Test
    public void unsetTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$unset': [ 'field3', 'field4'] }"));
        Assert.assertNotNull(obj);
        Assert.assertNotNull(((DBObject) obj.get("$unset")).get("field3"));
        Assert.assertNotNull(((DBObject) obj.get("$unset")).get("field4"));
    }

    @Test(expected=CannotTranslateException.class)
    public void arrTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        translator.translate(md, update("{ '$set': { 'field7.0.elemf1': 'blah'} }"));
    }

    @Test(expected=CannotTranslateException.class)
    public void arrTest2() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        translator.translate(md, update("{ '$set': { 'field7': '$null'} }"));
    }

    @Test(expected=CannotTranslateException.class)
    public void objTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        translator.translate(md, update("{ '$set': { 'field6': '$null'} }"));
    }
}
