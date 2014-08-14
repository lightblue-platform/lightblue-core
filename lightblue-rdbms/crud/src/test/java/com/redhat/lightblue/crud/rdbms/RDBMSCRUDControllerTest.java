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
package com.redhat.lightblue.crud.rdbms;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class RDBMSCRUDControllerTest {

    public static DataSource dsMock = null;
    public static Connection cMock = null;
    public static String statement = null;
    public static PreparedStatement psMock = null;
    RDBMSCRUDController cut = null; //class under test

    @Before
    public void setUp() throws Exception {
        cut = new RDBMSCRUDController(null);

    }

    @Test
    public void testInsert() throws Exception {

    }

}
