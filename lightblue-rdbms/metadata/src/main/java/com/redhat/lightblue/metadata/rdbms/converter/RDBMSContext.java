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

import com.redhat.lightblue.metadata.rdbms.model.RDBMS;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;

/**
 * @param <T> type of object returned in List (resultList)
 */
public class RDBMSContext<T> {
    private DataSource dataSource = null;
    private String dataSourceName = null;
    private Connection connection = null;
    private String statement = null;
    private PreparedStatement preparedStatement = null;
    private Boolean resultBoolean = null;
    private Integer resultInteger = null;
    private List<ResultSet> resultSetList = null;
    private RowMapper<T> rowMapper = null;
    private List<T> resultList = null;
    private RDBMS rdbms = null;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public Boolean isResultBoolean() {
        return resultBoolean;
    }

    public void setResultBoolean(Boolean resultBoolean) {
        this.resultBoolean = resultBoolean;
    }

    public Integer getResultInteger() {
        return resultInteger;
    }

    public void setResultInteger(Integer resultInteger) {
        this.resultInteger = resultInteger;
    }

    public List<ResultSet> getResultSetList() {
        return resultSetList;
    }

    public void setResultSetList(List<ResultSet> resultSetList) {
        this.resultSetList = resultSetList;
    }

    public RowMapper<T> getRowMapper() {
        return rowMapper;
    }

    public void setRowMapper(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    public RDBMS getRdbms() {
        return rdbms;
    }

    public void setRdbms(RDBMS rdbms) {
        this.rdbms = rdbms;
    }

}
