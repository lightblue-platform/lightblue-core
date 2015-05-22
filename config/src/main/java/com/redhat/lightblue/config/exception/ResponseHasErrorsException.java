package com.redhat.lightblue.config.exception;

import java.util.List;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.util.Error;

public class ResponseHasErrorsException extends Exception {

    private static final long serialVersionUID = -5470176624240371480L;

    private final Response response;

    public ResponseHasErrorsException(Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }

    public boolean hasDataErrors() {
        return !getDataErrors().isEmpty();
    }

    public List<Error> getErrors() {
        return response.getErrors();
    }

    public List<DataError> getDataErrors() {
        return response.getDataErrors();
    }

}
