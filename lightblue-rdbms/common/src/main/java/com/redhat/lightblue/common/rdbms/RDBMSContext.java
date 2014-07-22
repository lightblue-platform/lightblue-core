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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

public class RDBMSContext<T> {

    private DataSource dataSource = null;
    private String dataSourceName = null;
    private Connection connection = null;
    private String statement = null;
    private PreparedStatement preparedStatement = null;
    private Integer resultInteger = null;
    private ResultSet ResultSet = null;
    private RowMapper<T> rowMapper = null;
    private List<T> resultList = null;
    private Set<Parameter> parameters = null;
    private Set<TableField> tableFields = null;

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

    public Integer getResultInteger() {
        return resultInteger;
    }

    public void setResultInteger(Integer resultInteger) {
        this.resultInteger = resultInteger;
    }

    public ResultSet getResultSet() {
        return ResultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        ResultSet = resultSet;
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

    public Set<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RDBMSContext{"
                + "dataSource=" + dataSource
                + ", dataSourceName='" + dataSourceName + '\''
                + ", connection=" + connection
                + ", statement='" + statement + '\''
                + ", preparedStatement=" + preparedStatement
                + ", resultInteger=" + resultInteger
                + ", ResultSet=" + ResultSet
                + ", rowMapper=" + rowMapper
                + ", resultList=" + resultList
                + ", parameters=" + parameters
                + '}';
    }

    public Set<TableField> getTableFields() {
        return tableFields;
    }

    public void setTableFields(Set<TableField> tableFields) {
        this.tableFields = tableFields;
    }
}
