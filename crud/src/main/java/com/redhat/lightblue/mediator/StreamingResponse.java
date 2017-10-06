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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.BaseResponse;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocumentStream;
import com.redhat.lightblue.util.Error;

public class StreamingResponse extends BaseResponse  {

    private static final long serialVersionUID = 1L;

    public DocumentStream<DocCtx> documentStream;
    public Long matchCount;

    public StreamingResponse(JsonNodeFactory jsonNodeFactory, OperationStatus status) {
        super(jsonNodeFactory, status);
    }
    
    public static StreamingResponse withError(JsonNodeFactory jsonNodeFactory, Error error) {
        StreamingResponse r = new StreamingResponse(jsonNodeFactory, OperationStatus.ERROR);
        r.getErrors().add(error);
        return r;
    }
}
