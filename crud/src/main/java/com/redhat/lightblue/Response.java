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

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Error;

public class Response implements Serializable {

    private static final long serialVersionUID=1l;

    private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private JsonNode entityData;
    private final List<DataError> dataErrors=new ArrayList<DataError>();
    private final List<Error> errors=new ArrayList<Error>();

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus s) {
        status=s;
    }

    public long getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(long l) {
        modifiedCount=l;
    }

    public long getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(long l) {
        matchCount=l;
    }

    public String getTaskHandle() {
        return taskHandle;
    }

    public void setTaskHandle(String t) {
        taskHandle=t;
    }

    public SessionInfo getSessionInfo() {
        return session;
    }

    public void setSessionInfo(SessionInfo s) {
        session=s;
    }

    public List<DataError> getDataErrors() {
        return dataErrors;
    }

    public List<Error> getErrors() {
        return errors;
    }
}
