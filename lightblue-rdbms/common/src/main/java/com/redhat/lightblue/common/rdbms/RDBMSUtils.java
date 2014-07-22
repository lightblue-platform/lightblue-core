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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RDBMSUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSUtils.class);

    public DataSource getDataSource(RDBMSContext rDBMSContext) {
        LOGGER.debug("getDataSource() start");
        Error.push("RDBMSUtils");
        Error.push("getDataSource");
        DataSource ds = null;
        try {
            // relying on garbage collection to close context
            InitialContext context = new InitialContext();
            ds = (DataSource) context.lookup(rDBMSContext.getDataSourceName());
        } catch (NamingException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_DATASOURCE_NOT_FOUND, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        rDBMSContext.setDataSource(ds);
        LOGGER.debug("getDataSource() stop");
        return ds;
    }

    public Connection getConnection(RDBMSContext context) {
        if (context.getDataSource() == null) {
            throw new IllegalArgumentException("No dataSource supplied");
        }
        LOGGER.debug("getConnection() start");
        Error.push("RDBMSUtils");
        Error.push("getConnection");
        Connection c = null;
        try {
            c = context.getDataSource().getConnection();
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_GET_CONNECTION_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        context.setConnection(c);
        LOGGER.debug("getConnection() stop");
        return c;
    }

    public PreparedStatement getStatement(RDBMSContext context) {
        if (context.getConnection() == null) {
            throw new IllegalArgumentException("No connection supplied");
        }
        if (context.getStatement() == null) {
            throw new IllegalArgumentException("No statement supplied");
        }
        LOGGER.debug("getStatement() start");
        Error.push("RDBMSUtils");
        Error.push("getStatement");
        PreparedStatement ps = null;
        try {
            ps = context.getConnection().prepareStatement(context.getStatement());
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_GET_STATEMENT_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        context.setPreparedStatement(ps);
        LOGGER.debug("getStatement() stop");
        return ps;
    }

    public ResultSet executeQuery(RDBMSContext context) {
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement supplied");
        }
        ResultSet rs = null;
        LOGGER.debug("executeQuery() start");
        Error.push("RDBMSUtils");
        Error.push("executeQuery");
        try {
            rs = context.getPreparedStatement().executeQuery();
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_EXECUTE_QUERY_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        context.setResultSet(rs);
        LOGGER.debug("executeQuery() stop");
        return rs;

    }

    public int executeUpdate(RDBMSContext context) {
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement supplied");
        }
        Integer r = null;
        LOGGER.debug("executeUpdate() start");
        Error.push("RDBMSUtils");
        Error.push("executeUpdate");
        try {
            r = context.getPreparedStatement().executeUpdate();
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_EXECUTE_UPDATE_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        context.setResultInteger(r);
        LOGGER.debug("executeUpdate() stop");
        return r;

    }

    public <T> List<T> buildMappedList(RDBMSContext<T> context) {
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement supplied");
        }
        if (context.getRowMapper() == null) {
            throw new IllegalArgumentException("No rowMapper supplied");
        }
        Error.push("RDBMSUtils");
        Error.push("buildMappedList");
        List<T> list = new ArrayList<>();
        context.setResultList(list);
        try (ResultSet resultSet = executeQuery(context)) {
            while (resultSet.next()) {
                T o = context.getRowMapper().map(resultSet);
                list.add(o);
            }
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_BUILD_RESULT_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        close(context);
        return list;
    }

    public void close(RDBMSContext context) {
        if (context.getConnection() != null) {
            try {
                context.getConnection().close();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if (context.getPreparedStatement() != null) {
                    try {
                        context.getPreparedStatement().close();
                    } catch (SQLException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
