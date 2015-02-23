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
package com.redhat.lightblue.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.MultipleFailureException;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;

/**
 * Custom lightblue assertions for unit testing.
 *
 * @author dcrissman
 */
public final class Assert {

    /**
     * Asserts no Errors are on a {@link Response} from lightblue.
     * @param response - {@link Response} to check.
     * @throws MultipleFailureException
     */
    public static void assertNoErrors(Response response) throws MultipleFailureException{
        List<Throwable> errors = new ArrayList<Throwable>(response.getErrors());
        if(!errors.isEmpty()){
            throw new MultipleFailureException(errors);
        }
    }

    /**
     * Asserts no DataErrors are on a {@link Response} from lightblue.
     * @param response - {@link Response} to check.
     * @throws MultipleFailureException
     */
    public static void assertNoDataErrors(Response response) throws MultipleFailureException{
        List<Throwable> errors = new ArrayList<Throwable>();
        for(DataError error : response.getDataErrors()){
            errors.add(new Exception("DataError: " + error.toJson().toString()));
        }

        if(!errors.isEmpty()){
            throw new MultipleFailureException(errors);
        }
    }

    private Assert(){}

}
