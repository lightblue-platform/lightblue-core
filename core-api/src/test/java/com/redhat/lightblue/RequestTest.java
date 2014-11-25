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
