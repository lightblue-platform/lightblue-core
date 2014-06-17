package com.redhat.lightblue.common.rdbms;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RDBMSContext <T> {

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
        return "RDBMSContext{" +
                "dataSource=" + dataSource +
                ", dataSourceName='" + dataSourceName + '\'' +
                ", connection=" + connection +
                ", statement='" + statement + '\'' +
                ", preparedStatement=" + preparedStatement +
                ", resultInteger=" + resultInteger +
                ", ResultSet=" + ResultSet +
                ", rowMapper=" + rowMapper +
                ", resultList=" + resultList +
                ", parameters=" + parameters +
                '}';
    }
}
