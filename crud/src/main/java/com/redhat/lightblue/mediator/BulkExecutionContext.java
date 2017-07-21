package com.redhat.lightblue.mediator;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.BulkRequest;
import com.redhat.lightblue.util.Error;

public class BulkExecutionContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkExecutionContext.class);

    final Future<Response>[] futures;
    private Response[] responses;

    private int responseDataSizeB = 0;
    private int maxResultSetSizeB = -1, warnResultSetSizeB = -1;
    private BulkRequest forRequest;
    private boolean warnThresholdBreached = false;

    public BulkExecutionContext(int size) {
        futures = new Future[size];
        responses = new Response[size];
    }

    public void ensureResponseSizeNotTooLarge(int maxResultSetSizeB, int warnResultSetSizeB, BulkRequest forRequest) {
        this.forRequest = forRequest;
        this.maxResultSetSizeB = maxResultSetSizeB;
        this.warnResultSetSizeB = warnResultSetSizeB;
    }

    public boolean isEnsureResposneSizeNotTooLarge() {
        return maxResultSetSizeB > 0;
    }

    public boolean isWarnResponseSizeLarge() {
        return warnResultSetSizeB > 0;
    }

    public boolean isCheckResponseSize() {
        return isEnsureResposneSizeNotTooLarge() || isWarnResponseSizeLarge();
    }

    private void enforceResponseSizeLimits(Response response) {
        if (isCheckResponseSize()) {
            responseDataSizeB += response.getResponseDataSizeB();
        }

        if (isEnsureResposneSizeNotTooLarge() && responseDataSizeB >= maxResultSetSizeB) {
            // remove data
            response.setEntityData(JsonNodeFactory.instance.arrayNode());
            response.getErrors().add(Error.get(Response.ERR_RESULT_SIZE_TOO_LARGE, responseDataSizeB+"B > "+maxResultSetSizeB+"B"));
        } else if (isWarnResponseSizeLarge() && !warnThresholdBreached && responseDataSizeB >= warnResultSetSizeB) {
            LOGGER.warn("crud:ResultSizeIsLarge: request={}, responseDataSizeB={}", forRequest, responseDataSizeB);
            warnThresholdBreached = true;
        }

    }

    public void setResponseAt(int index, Response response) {
        enforceResponseSizeLimits(response);

        responses[index] = response;
    }

    public Response[] getResponses() {
        return responses;
    }
}