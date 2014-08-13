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
package com.redhat.lightblue.common.rdbms;

import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class RDBMSUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSUtils.class);

    public static DataSource getDataSource(String name) {
        LOGGER.debug("getDataSource() start");
        Error.push("RDBMSUtils");
        Error.push("getDataSource");
        DataSource ds = null;
        try {
            // relying on garbage collection to close context
            InitialContext context = new InitialContext();
            ds = (DataSource) context.lookup(name);
        } catch (NamingException e) {
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_DATASOURCE_NOT_FOUND, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        LOGGER.debug("getDataSource() stop");
        return ds;
    }
}
