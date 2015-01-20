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
package com.redhat.lightblue;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by nmalik on 11/24/14.
 */
public class RequestTest {
    private class TestRequest extends Request {
        public TestRequest() {

        }
    }

    @Test
    public void test_shallowCopyFrom() {
        EntityVersion entityVersion = new EntityVersion();
        ClientIdentification client = new ClientIdentification() {
            @Override
            public String getPrincipal() {
                return null;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }
        };
        ExecutionOptions execution = new ExecutionOptions();

        Request request = new TestRequest();
        request.setClientId(client);
        request.setEntityVersion(entityVersion);
        request.setExecution(execution);

        Request copy = new TestRequest();
        copy.shallowCopyFrom(request);

        Assert.assertEquals(entityVersion, copy.getEntityVersion());
        Assert.assertEquals(client, copy.getClientId());
        Assert.assertEquals(execution, copy.getExecution());
    }
}
