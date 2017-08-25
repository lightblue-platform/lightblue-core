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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.ResultMetadata;
import com.redhat.lightblue.crud.BulkRequest;
import com.redhat.lightblue.crud.BulkResponse;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.ListDocumentStream;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.metrics.DropwizardRequestMetrics;
import com.redhat.lightblue.util.metrics.NoopRequestMetrics;
import com.redhat.lightblue.util.metrics.RequestMetrics;

public class BulkTest extends AbstractMediatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkTest.class);
    private final RequestMetrics noopMetrics = new NoopRequestMetrics();

    public interface FindCb {
        Response call(FindRequest req);
    }

    public interface InsertCb {
        Response call(InsertionRequest req);
    }

    public class TestMediator extends Mediator {

        FindCb findCb = null;
        InsertCb insertCb = null;

        public TestMediator(Metadata md, Factory f) {
            super(md, f);
        }

        @Override
        public Response find(FindRequest req) {
            if (findCb != null) {
                return findCb.call(req);
            } else {
                return super.find(req);
            }
        }

        @Override
        public Response insert(InsertionRequest req) {
            if (insertCb != null) {
                return insertCb.call(req);
            } else {
                return super.insert(req);
            }
        }
    }

    @Override
    protected Mediator newMediator(Metadata md, Factory f) {
        return new TestMediator(md, f);
    }

    @Test
    public void bulkTest() throws Exception {
        BulkRequest breq = new BulkRequest();

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        breq.add(ireq);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));
        breq.add(freq);
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        mockCrudController.insertResponse.setNumInserted(1);

        BulkResponse bresp = mediator.bulkRequest(breq, noopMetrics);

        Response response = bresp.getEntries().get(0);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());

        response = bresp.getEntries().get(1);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());
    }

    @Test
    public void bulkResultSetTooLargeTest() throws Exception {
        final JsonNode sampleDoc = loadJsonNode("./sample1.json");
        mdManager.md.getAccess().getFind().setRoles("anyone");

        BulkRequest breq = new BulkRequest();

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(sampleDoc);
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        breq.add(ireq);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));
        breq.add(freq);
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        mockCrudController.insertResponse.setNumInserted(10);
        mockCrudController.insertCb=ctx->{
            ArrayList<DocCtx> docs=new ArrayList<>();
            for(int i=0;i<10;i++) {
                docs.add(new DocCtx(new JsonDoc(sampleDoc), new ResultMetadata()));
            }
            ctx.setDocumentStream(new ListDocumentStream<DocCtx>(docs));
        };
        mockCrudController.findResponse=new CRUDFindResponse();
        mockCrudController.findResponse.setSize(10);
        mockCrudController.findCb=ctx->{
            ArrayList<DocCtx> docs=new ArrayList<>();
            for(int i=0;i<10;i++) {
                docs.add(new DocCtx(new JsonDoc(sampleDoc), new ResultMetadata()));
            }
            ctx.setDocumentStream(new ListDocumentStream<DocCtx>(docs));
        };

        mediator.factory.setWarnResultSetSizeB(10);

        BulkResponse bresp = mediator.bulkRequest(breq, noopMetrics);
        Assert.assertEquals(2, breq.getEntries().size());

        Response responseInsert = bresp.getEntries().get(0);
        Response responseFind = bresp.getEntries().get(1);

        Assert.assertEquals("Insert response should return 1262B of data", 1262, responseInsert.getResponseDataSizeB());
        Assert.assertEquals("Find response should return 12620B of data", 12620, responseFind.getResponseDataSizeB());

        mediator.factory.setMaxResultSetSizeForReadsB(12630); // the limit is more than each request alone

        bresp = mediator.bulkRequest(breq, noopMetrics);

        responseInsert = bresp.getEntries().get(0);
        responseFind = bresp.getEntries().get(1);

        Assert.assertTrue("The first request should have no errors", responseInsert.getErrors().isEmpty());
        Assert.assertEquals("The first request should have returend all data", 1262, JsonUtils.size(responseInsert.getEntityData()));
        Assert.assertEquals("The second request should error, as it exceeds the response size threshold", 1, responseFind.getErrors().size());
        Assert.assertEquals(Response.ERR_RESULT_SIZE_TOO_LARGE, responseFind.getErrors().get(0).getErrorCode());
        Assert.assertEquals("The second request contains no data", 0, responseFind.getEntityData().size());
    }

    @Test
    @Ignore
    public void bulkTest_fromJson() throws Exception {
        // TODO stop ignoring and finish impl once JSON schema is defined
        BulkRequest breq = new BulkRequest();

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        breq.add(ireq);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));
        breq.add(freq);

        // convert to string then parse back.  should be identical
        ObjectNode breqJson = (ObjectNode) breq.toJson();

        BulkRequest breqParsed = BulkRequest.fromJson(breqJson);

        LOGGER.debug("asdf");
    }

    private class PFindCb implements FindCb {
        Semaphore sem = new Semaphore(0);
        AtomicInteger nested = new AtomicInteger(0);

        @Override
        public  Response call(FindRequest req) {
            nested.incrementAndGet();
            try {
                LOGGER.debug("find: nested:"+nested+" waiting");
                sem.acquire();
                LOGGER.debug("find: nested:"+nested+" acquired");
                return new Response();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                nested.decrementAndGet();
            }
        }
    }

    private class PInsertCb implements InsertCb {
        Semaphore sem = new Semaphore(0);
        AtomicInteger nested = new AtomicInteger(0);

        @Override
        public Response call(InsertionRequest req) {
            nested.incrementAndGet();
            try {
                LOGGER.debug("insert: nested:"+nested+" waiting");
                sem.acquire();
                LOGGER.debug("insert: nested:"+nested+" acquired");
                return new Response();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                nested.decrementAndGet();
            }
        }
    }

    private abstract class ValidatorThread extends Thread {
        PFindCb find;
        PInsertCb insert;
        boolean valid = false;

        public ValidatorThread(PFindCb f, PInsertCb i) {
            find = f;
            insert = i;
        }

    }

    @Test
    public void parallelOrderedBulkTest() throws Exception {
        BulkRequest breq = new BulkRequest();

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));
        freq.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));

        breq.add(freq);
        breq.add(freq);
        breq.add(freq);
        breq.add(ireq);
        breq.add(freq);
        breq.add(freq);
        breq.add(ireq);

        PFindCb findCb = new PFindCb();
        PInsertCb insertCb = new PInsertCb();
        ((TestMediator) mediator).findCb = findCb;
        ((TestMediator) mediator).insertCb = insertCb;

        ValidatorThread validator = new ValidatorThread(findCb, insertCb) {
            @Override
            public void run() {
                try {
                    LOGGER.debug("Check if all 3 finds are waiting");
                    while (find.nested.get() < 3) {
                        Thread.sleep(1);
                    }
                    LOGGER.debug("Let the 3 find requests complete");
                    find.sem.release(3);
                    LOGGER.debug("Busy wait");
                    while (find.sem.availablePermits() > 0) {
                        Thread.sleep(1);
                    }
                    LOGGER.debug("Let insert complete");
                    insert.sem.release(1);
                    while (insert.sem.availablePermits() > 0) {
                        Thread.sleep(1);
                    }
                    LOGGER.debug("Check if all 2 finds are waiting");
                    while (find.nested.get() < 2) {
                        Thread.sleep(1);
                    }
                    LOGGER.debug("Let the remaining 2 find requests complete");
                    find.sem.release(2);
                    while (find.sem.availablePermits() > 0) {
                        Thread.sleep(1);
                    }
                    insert.sem.release(1);
                    while (insert.sem.availablePermits() > 0) {
                        Thread.sleep(1);
                    }
                    LOGGER.debug("Complete");
                    valid = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        validator.start();

        LOGGER.debug("Ordered exec");
        BulkResponse bresp = mediator.bulkRequest(breq, noopMetrics);
        validator.join();
        LOGGER.debug("Ordered exec done");

        Assert.assertTrue(validator.valid);
    }

    @Test
    public void parallelUnorderedBulkTest() throws Exception {
        BulkRequest breq = new BulkRequest();
        breq.setOrdered(false);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));
        freq.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));

        breq.add(freq);
        breq.add(freq);
        breq.add(ireq);
        breq.add(freq);
        breq.add(freq);
        breq.add(ireq);
        breq.add(freq);

        PFindCb findCb = new PFindCb();
        PInsertCb insertCb = new PInsertCb();
        ((TestMediator) mediator).findCb = findCb;
        ((TestMediator) mediator).insertCb = insertCb;

        ValidatorThread validator = new ValidatorThread(findCb, insertCb) {
            @Override
            public void run() {
                try {
                    // Wait until all 7 calls started
                    while (find.nested.get() + insert.nested.get() < 7) {
                        Thread.sleep(1);
                    }

                    // Let them all complete
                    find.sem.release(5);
                    insert.sem.release(2);

                    // Wait until they're all completed
                    while(find.nested.get() > 0 && insert.nested.get() > 0) {
                        Thread.sleep(1);
                    }

                    valid = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        validator.start();
        LOGGER.debug("Unordered exec");
        mediator.bulkRequest(breq, noopMetrics);
        validator.join();
        LOGGER.debug("Unordered exec done");

        Assert.assertTrue(validator.valid);
    }
    
    @Test
    public void metricsBulkTest() throws Exception {
        MetricRegistry metricsRegistry = new MetricRegistry();
        RequestMetrics metrics = new DropwizardRequestMetrics(metricsRegistry);
        
        BulkRequest breq = new BulkRequest();

        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("user", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        breq.add(ireq);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("country", "2.0"));
        breq.add(freq);
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        mockCrudController.insertResponse.setNumInserted(1);

        DeleteRequest dreq = new DeleteRequest();
        dreq.setEntityVersion(new EntityVersion("test", "3.0"));
        breq.add(dreq);
        mockCrudController.deleteResponse=new CRUDDeleteResponse();
        mockCrudController.deleteResponse.setNumDeleted(1);

        BulkResponse bresp = mediator.bulkRequest(breq, metrics);

        Response response = bresp.getEntries().get(0);
        
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, metricsRegistry.counter("api.insert.user.1.0.requests.active").getCount());
        Assert.assertEquals(1, metricsRegistry.timer("api.insert.user.1.0.requests.latency").getCount());
        Assert.assertNotNull(metricsRegistry.timer("api.insert.user.1.0.requests.latency").getOneMinuteRate());

        response = bresp.getEntries().get(1);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());
        Assert.assertEquals(0,metricsRegistry.counter("api.find.country.2.0.requests.active").getCount());
        Assert.assertEquals(1, metricsRegistry.timer("api.find.country.2.0.requests.latency").getCount());
        Assert.assertEquals(1, metricsRegistry.meter("api.find.country.2.0.requests.exception.Error.crud.NoAccess").getCount());
        Assert.assertNotNull(metricsRegistry.meter("api.find.country.2.0.requests.exception.Error.crud.NoAccess").getOneMinuteRate());

        response = bresp.getEntries().get(2);
        
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());
        Assert.assertEquals(0,metricsRegistry.counter("api.delete.test.3.0.requests.active").getCount());
        Assert.assertEquals(1, metricsRegistry.timer("api.delete.test.3.0.requests.latency").getCount());
        Assert.assertEquals(1, metricsRegistry.meter("api.delete.test.3.0.requests.exception.Error.crud.NoAccess").getCount());
        Assert.assertNotNull(metricsRegistry.meter("api.delete.test.3.0.requests.exception.Error.crud.NoAccess").getOneMinuteRate());
        
    }
}
