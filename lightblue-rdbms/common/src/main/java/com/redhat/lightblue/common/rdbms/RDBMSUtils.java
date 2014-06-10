package com.redhat.lightblue.common.rdbms;

import com.redhat.lightblue.util.*;
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

    public DataSource getDataSource(RDBMSContext rDBMSContext){
        LOGGER.debug("getDataSource() start");
        com.redhat.lightblue.util.Error.push("Getting JDBC through JNDI");
        InitialContext context = null;
        DataSource ds = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup(rDBMSContext.getDataSourceName());
        } catch (NamingException e) {
            LOGGER.error("getDataSource : {}", e);
            throw new IllegalStateException(e);
        } finally {
            Error.pop();
        }
        rDBMSContext.setDataSource(ds);
        LOGGER.debug("getDataSource() stop");
        return ds;
    }

    public Connection getConnection(RDBMSContext context){
        if (context.getDataSource() == null) {
            throw new IllegalArgumentException("No dataSource informed");
        }
        LOGGER.debug("getConnection() start");
        Error.push("Getting the connection from JDBC");
        Connection c = null;
        try {
            c = context.getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("getConnection : {}", e);
            throw new IllegalStateException(e);
        } finally {
            Error.pop();
        }
        context.setConnection(c);
        LOGGER.debug("getConnection() stop");
        return c;
    }

    public PreparedStatement getStatement(RDBMSContext context){
        if (context.getConnection() == null) {
            throw new IllegalArgumentException("No connection informed");
        }
        if (context.getStatement() == null) {
            throw new IllegalArgumentException("No statement informed");
        }
        LOGGER.debug("getStatement() start");
        Error.push("Getting the statement from JDBC");
        PreparedStatement ps = null;
        try {
            ps = context.getConnection().prepareStatement(context.getStatement());
        } catch (SQLException e) {
            LOGGER.error("getDataSource NamingException: {}", e);
            throw new IllegalStateException(e);
        } finally {
            Error.pop();
        }
        context.setPreparedStatement(ps);
        LOGGER.debug("getStatement() stop");
        return ps;
    }

    public ResultSet executeQuery(RDBMSContext context){
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement informed");
        }
        ResultSet rs = null;
        LOGGER.debug("executeQuery() start");
        try {
            rs = context.getPreparedStatement().executeQuery();
        } catch (SQLException e) {
            LOGGER.error("executeQuery SQLException: {}", e);
            throw new IllegalStateException(e);
        } finally {
            Error.pop();
        }
        context.setResultSet(rs);
        LOGGER.debug("executeQuery() stop");
        return rs;

    }

    public int executeUpdate(RDBMSContext context){
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement informed");
        }
        Integer r = null;
        LOGGER.debug("executeUpdate() start");
        try {
            r = context.getPreparedStatement().executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("executeUpdate SQLException: {}", e);
            throw new IllegalStateException(e);
        } finally {
            Error.pop();
        }
        context.setResultInteger(r);
        LOGGER.debug("executeUpdate() stop");
        return r;

    }



    public <T> List<T> buildMappedList(RDBMSContext<T> context){
        if (context.getPreparedStatement() == null) {
            throw new IllegalArgumentException("No statement informed");
        }
        if (context.getRowMapper() == null) {
            throw new IllegalArgumentException("No rowMapper informed");
        }
        ResultSet resultSet = executeQuery(context);
        List<T> list = new ArrayList<T>();
        context.setResultList(list);
        try {
            while (resultSet.next()) {
                T o = context.getRowMapper().map(resultSet);
                list.add(o);
            }
        } catch (SQLException e) {
            LOGGER.error("Error with the ResultSet", e);
            throw new IllegalStateException("Error with the ResultSet", e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception ex) {
                    LOGGER.error("Problem closing the ResultSet", ex);
                }
            }
        }
        close(context);
        return list;
    }

    public void close(RDBMSContext context) {
        if (context.getConnection() != null) {
            try {
                context.getConnection().close();
            } catch (Exception e) {
                LOGGER.error("Error closing the connection", e);
            } finally {
                if (context.getPreparedStatement() != null) {
                    try {
                        context.getPreparedStatement().close();
                    } catch (Exception e) {
                        LOGGER.error("Error closing the PreparedStatement", e);
                    }
                }
            }
        }
    }
}
