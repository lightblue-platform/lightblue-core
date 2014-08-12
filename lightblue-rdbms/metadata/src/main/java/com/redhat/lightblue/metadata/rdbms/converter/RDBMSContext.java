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

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.rdbms.model.RDBMS;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * @param <T> type of object returned in List (resultList)
 */
public class RDBMSContext<T> {
    private DataSource dataSource;
    private String dataSourceName;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private Boolean resultBoolean;
    private Integer resultInteger;
    private List<ResultSet> resultSetList;
    private RowMapper<T> rowMapper;
    private List<T> resultList;
    private RDBMS rdbms;
    private String sql;
    private String type;
    private EntityMetadata entityMetadata;
    private QueryExpression queryExpression;
    private Projection projection;
    private Sort sort;
    private Long from;
    private Long to;
    private Map<String, Object> temporaryVariable;

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

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EntityMetadata getEntityMetadata() {
        return entityMetadata;
    }

    public void setEntityMetadata(EntityMetadata entityMetadata) {
        this.entityMetadata = entityMetadata;
    }

    public QueryExpression getQueryExpression() {
        return queryExpression;
    }

    public void setQueryExpression(QueryExpression queryExpression) {
        this.queryExpression = queryExpression;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Boolean getResultBoolean() {
        return resultBoolean;
    }

    public Map<String, Object> getTemporaryVariable() {
        return temporaryVariable;
    }

    public void setTemporaryVariable(Map<String, Object> temporaryVariable) {
        this.temporaryVariable = temporaryVariable;
    }
}
