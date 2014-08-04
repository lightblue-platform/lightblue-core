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
package com.redhat.lightblue.metadata.rdbms.converter;

import com.redhat.lightblue.common.rdbms.RDBMSConstants;
import com.redhat.lightblue.util.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RDBMSUtilsMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSUtilsMetadata.class);

    public static DataSource getDataSource(RDBMSContext rDBMSContext) {
        DataSource dataSource = com.redhat.lightblue.common.rdbms.RDBMSUtils.getDataSource(rDBMSContext.getDataSourceName());
        rDBMSContext.setDataSource(dataSource);
        return dataSource;
    }

    public static Connection getConnection(RDBMSContext context) {
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

    public static PreparedStatement getPreparedStatement(RDBMSContext context) {
        if (context.getConnection() == null) {
            throw new IllegalArgumentException("No connection supplied");
        }
        if (context.getSql() == null) {
            throw new IllegalArgumentException("No sql statement supplied");
        }
        if (context.getType()== null) {
            throw new IllegalArgumentException("No sql statement type supplied");
        }
        LOGGER.debug("getPreparedStatement() start");
        Error.push("RDBMSUtils");
        Error.push("getStatement");
        PreparedStatement ps = null;
        try { 
            ps = context.getConnection().prepareStatement(context.getSql());
        } catch (SQLException e) {
            // throw new Error (preserves current error context)
            LOGGER.error(e.getMessage(), e);
            throw Error.get(RDBMSConstants.ERR_GET_STATEMENT_FAILED, e.getMessage());
        } finally {
            Error.pop();
            Error.pop();
        }
        context.setPreparedStatement(ps);
        LOGGER.debug("getPreparedStatement() stop");
        return ps;
    }
    
    public static PreparedStatement getStatement(RDBMSContext context) {
        if (context.getConnection() == null) {
            throw new IllegalArgumentException("No connection supplied");
        }
        if (context.getSql() == null) {
            throw new IllegalArgumentException("No sql statement supplied");
        }
        if (context.getType()== null) {
            throw new IllegalArgumentException("No sql statement type supplied");
        }
        LOGGER.debug("getStatement() start");
        Error.push("RDBMSUtils");
        Error.push("getStatement");
        PreparedStatement ps = null;
        try { 
            NamedParameterStatement nps = new NamedParameterStatement(context.getConnection(), context.getSql());
            //ps = .prepareStatement();
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

    public static int executeUpdate(RDBMSContext context) {
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
        context.setResultSetList(new ArrayList()); 
        
        ResultSet rs;
        try {
            rs = context.getPreparedStatement().getResultSet();
            if(rs != null){
                context.getResultSetList().add(rs);
                try {
                    while(context.getPreparedStatement().getMoreResults()){
                        context.getResultSetList().add(context.getPreparedStatement().getResultSet());
                    }
                } catch (SQLException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }

            }
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
                
        LOGGER.debug("executeUpdate() stop");
        return r;

    }
    
    public static void close(RDBMSContext context) {
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
    
    public static <T> List<T> buildAllMappedList(RDBMSContext<T> context) {
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement supplied");
        }
        if (context.getRowMapper() == null) {
            throw new IllegalArgumentException("No rowMapper supplied");
        }
        Error.push("buildMappedList");
        getDataSource(context);
        getConnection(context);
        getStatement(context);
        List<T> list = new ArrayList<>();
        context.setResultList(list);
        executeUpdate(context);
        List<ResultSet> resultSetList = context.getResultSetList();
        for(ResultSet rs : resultSetList){
            try {
                while (rs.next()) {
                    T o = context.getRowMapper().map(rs);
                    list.add(o);
                }
                rs.close();
            } catch (SQLException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }                
        Error.pop();
        close(context);
        return list;
    }

}
