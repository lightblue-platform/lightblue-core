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

import static com.redhat.lightblue.test.Assert.assertNoDataErrors;
import static com.redhat.lightblue.test.Assert.assertNoErrors;

import org.junit.Test;
import org.junit.runners.model.MultipleFailureException;

import com.redhat.lightblue.DataError;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.util.Error;

public class AssertTest {

    @Test
    public void testAssertNoErrors_pass() throws MultipleFailureException{
        assertNoErrors(new Response(null));
    }

    @Test(expected = MultipleFailureException.class)
    public void testAssertNoErrors_fail() throws MultipleFailureException{
        Response response = new Response(null);
        response.getErrors().add(Error.get("fake error"));
        assertNoErrors(response);
    }

    @Test
    public void testAssertNoDataErrors_pass() throws MultipleFailureException{
        assertNoDataErrors(new Response(null));
    }

    @Test(expected = MultipleFailureException.class)
    public void testAssertNoDataErrors_fail() throws MultipleFailureException{
        Response response = new Response(null);
        response.getDataErrors().add(new DataError());
        assertNoDataErrors(response);
    }

}
