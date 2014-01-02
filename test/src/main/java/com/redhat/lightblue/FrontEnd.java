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

package com.redhat.lightblue;

import com.mongodb.DB;
import org.bson.BSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.crud.mongo.MongoCRUDController;
import com.redhat.lightblue.crud.mongo.DBResolver;

import com.redhat.lightblue.controller.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.controller.Factory;

import com.redhat.lightblue.mediator.Mediator;

/**
 * Simple test front-end for metadata and mediator that works with one
 * DB
 */
public class FrontEnd {
    private final DB db;

    private static final JsonNodeFactory nodeFactory=
        JsonNodeFactory.withExactBigDecimals(true);

    private final DBResolver simpleDBResolver=new DBResolver() {
            public DB get(MongoDataStore s) {
                return db;
            }
        };

    public FrontEnd(DB db) {
        this.db=db;
    }

    public Metadata getMetadata() {
        Extensions<BSONObject> parserExtensions=new Extensions<BSONObject>();
        DefaultTypes typeResolver=new DefaultTypes();
        return new MongoMetadata(db,parserExtensions,typeResolver);
    }

    public Mediator getMediator() {
        Factory factory=new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        MongoCRUDController mongoCRUDController=
            new MongoCRUDController(nodeFactory,simpleDBResolver);
        factory.addCRUDController("mongo",mongoCRUDController);
        return new Mediator(getMetadata(),factory);
    }
}
