package com.redhat.lightblue.mediator;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.BulkRequest;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

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

    /**
     * Bulk result set size threshold is expressed in bytes. This is just an approximation, see @{link {@link JsonUtils#size(JsonNode)} for details.
     *
     * @param maxResultSetSizeB error when this threshold is breached
     * @param warnResultSetSizeB log a warning when this threshold is breached
     * @param forRequest request which resulted in this response, for logging purposes
     */
    public void setResultSizeThresholds(int maxResultSetSizeB, int warnResultSetSizeB, BulkRequest forRequest) {
        this.forRequest = forRequest;
        this.maxResultSetSizeB = maxResultSetSizeB;
        this.warnResultSetSizeB = warnResultSetSizeB;
    }

    public boolean isErrorOnResponseSizeTooLarge() {
        return maxResultSetSizeB > 0;
    }

    public boolean isWarnOnResponseSizeLarge() {
        return warnResultSetSizeB > 0;
    }

    public boolean isCheckResponseSize() {
        return isErrorOnResponseSizeTooLarge() || isWarnOnResponseSizeLarge();
    }

    private void enforceResponseSizeLimits(Response response) {
        if (isCheckResponseSize()) {
            responseDataSizeB += response.getResponseDataSizeB();
        }

        if (isErrorOnResponseSizeTooLarge() && responseDataSizeB >= maxResultSetSizeB) {
            // remove data
            response.setEntityData(JsonNodeFactory.instance.arrayNode());
            response.getErrors().add(Error.get(Response.ERR_RESULT_SIZE_TOO_LARGE, responseDataSizeB+"B > "+maxResultSetSizeB+"B"));
        } else if (isWarnOnResponseSizeLarge() && !warnThresholdBreached && responseDataSizeB >= warnResultSetSizeB) {
            LOGGER.warn("crud:ResultSizeIsLarge: request={}, responseDataSizeB={}", forRequest, responseDataSizeB);
            warnThresholdBreached = true;
        }

    }

    /**
     * This method is not thread safe (specifically, managing responseDataSizeB isn't). That's ok, because a single thread gathers
     * responses from requests processed in parallel and adds them to {@link BulkExecutionContext}.
     *
     * @param index
     * @param response
     */
    public void setResponseAt(int index, Response response) {
        enforceResponseSizeLimits(response);

        responses[index] = response;
    }

    public Response[] getResponses() {
        return responses;
    }
}