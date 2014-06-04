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
package com.redhat.lightblue.rest.crud.hystrix;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.metadata.Metadata;
import org.junit.After;

/**
 *
 * @author nmalik
 */
public abstract class AbstractRestCommandTest {
    public static class TestMediator extends Mediator {
        public String methodCalled;
        public Object[] args;

        public TestMediator(Metadata md, Factory factory) {
            super(md, factory);
            methodCalled = null;
            args = null;
        }

        @Override
        public Response insert(InsertionRequest req) {
            methodCalled = "insert";
            args = new Object[]{req};
            return new Response();
        }

        @Override
        public Response save(SaveRequest req) {
            methodCalled = "save";
            args = new Object[]{req};
            return new Response();
        }

        @Override
        public Response update(UpdateRequest req) {
            methodCalled = "update";
            args = new Object[]{req};
            return new Response();
        }

        @Override
        public Response find(FindRequest req) {
            methodCalled = "find";
            args = new Object[]{req};
            return new Response();
        }

        @Override
        public Response delete(DeleteRequest req) {
            methodCalled = "delete";
            args = new Object[]{req};
            return new Response();
        }
    }

    protected TestMediator mediator = new TestMediator(null, null);

    @After
    public void tearDown() {
        mediator.methodCalled = null;
        mediator.args = null;
    }
}
