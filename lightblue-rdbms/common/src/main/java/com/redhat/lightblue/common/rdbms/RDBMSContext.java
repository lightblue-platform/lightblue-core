package com.redhat.lightblue.common.rdbms;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

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
    private HashMap<Object,Object> parameters = null;

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

    public HashMap<Object, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<Object, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDBMSContext that = (RDBMSContext) o;

        if (ResultSet != null ? !ResultSet.equals(that.ResultSet) : that.ResultSet != null) return false;
        if (connection != null ? !connection.equals(that.connection) : that.connection != null) return false;
        if (dataSource != null ? !dataSource.equals(that.dataSource) : that.dataSource != null) return false;
        if (dataSourceName != null ? !dataSourceName.equals(that.dataSourceName) : that.dataSourceName != null)
            return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (preparedStatement != null ? !preparedStatement.equals(that.preparedStatement) : that.preparedStatement != null)
            return false;
        if (resultInteger != null ? !resultInteger.equals(that.resultInteger) : that.resultInteger != null)
            return false;
        if (resultList != null ? !resultList.equals(that.resultList) : that.resultList != null) return false;
        if (rowMapper != null ? !rowMapper.equals(that.rowMapper) : that.rowMapper != null) return false;
        if (statement != null ? !statement.equals(that.statement) : that.statement != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dataSource != null ? dataSource.hashCode() : 0;
        result = 31 * result + (dataSourceName != null ? dataSourceName.hashCode() : 0);
        result = 31 * result + (connection != null ? connection.hashCode() : 0);
        result = 31 * result + (statement != null ? statement.hashCode() : 0);
        result = 31 * result + (preparedStatement != null ? preparedStatement.hashCode() : 0);
        result = 31 * result + (resultInteger != null ? resultInteger.hashCode() : 0);
        result = 31 * result + (ResultSet != null ? ResultSet.hashCode() : 0);
        result = 31 * result + (rowMapper != null ? rowMapper.hashCode() : 0);
        result = 31 * result + (resultList != null ? resultList.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
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
