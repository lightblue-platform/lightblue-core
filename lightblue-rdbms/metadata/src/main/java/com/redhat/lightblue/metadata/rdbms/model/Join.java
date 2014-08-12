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
package com.redhat.lightblue.metadata.rdbms.model;

import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.rdbms.converter.SimpleConverter;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lcestari
 */
public class Join implements SimpleConverter {
    private List<Table> tables;
    private String joinTablesStatement;
    private List<ProjectionMapping> projectionMappings;
    private boolean needDistinct;


    public <T> void parse(MetadataParser<T> p, T t) {
        List<T> tst = p.getObjectList(t, "tables");
        List<Table> ts = parseTables(p, tst);
        
        String jts = p.getStringProperty(t, "joinTablesStatement");
        Boolean needDistinct = (Boolean) p.getValueProperty(t, "needDistinct");
        
        List<T> pmsT = p.getObjectList(t, "projectionMappings");
        List<ProjectionMapping> pms = parseProjectionMappings(p, pmsT);
        
        this.tables = ts;
        this.joinTablesStatement = jts;
        this.projectionMappings = pms;
        this.needDistinct = needDistinct;
        
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        Object ts = p.newArrayField(eT, "tables");
        convertTables(p, ts);
        
        p.putString(eT, "joinTablesStatement", joinTablesStatement);

        if(needDistinct) {
            p.putValue(eT, "needDistinct", needDistinct);
        }

        Object ps = p.newArrayField(eT, "projectionMappings");
        convertProjectionMappings(p, ps);

        p.addObjectToArray(expressionsNode, eT);
    }

    private <T> void convertTables(MetadataParser<T> p, Object array) {
        if(this.tables == null || this.tables.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing tables field");  
        }
        for (Table t : this.tables) {
            t.convert(p, array);
        }
    }
    
    private <T> void convertProjectionMappings(MetadataParser<T> p, Object array) {
        if(this.projectionMappings == null || this.projectionMappings.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing projectionMappings field");  
        }
        for (ProjectionMapping pm : this.projectionMappings) {
            pm.convert(p, array);
        }
    }

    private <T> List<Table> parseTables(MetadataParser<T> p, List<T> tst) {
        List<Table> r = new ArrayList<>();
        if (tst == null || tst.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No tables informed for Join");
        }
        for (T t : tst) {
            Table x = new Table();
            x.parse(p,t);
            r.add(x);
        }
        return r;
    }

    private <T> List<ProjectionMapping> parseProjectionMappings(MetadataParser<T> p, List<T> pmsT) {
        List<ProjectionMapping> r = new ArrayList<>();
        if (pmsT == null || pmsT.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No projectionMappings informed for Join");
        }
        for (T t : pmsT) {
            ProjectionMapping x = new ProjectionMapping();
            x.parse(p,t);
            r.add(x);
        }
        return r;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public String getJoinTablesStatement() {
        return joinTablesStatement;
    }

    public void setJoinTablesStatement(String joinTablesStatement) {
        this.joinTablesStatement = joinTablesStatement;
    }

    public List<ProjectionMapping> getProjectionMappings() {
        return projectionMappings;
    }

    public void setProjectionMappings(List<ProjectionMapping> projectionMappings) {
        this.projectionMappings = projectionMappings;
    }

    public boolean isNeedDistinct() {
        return needDistinct;
    }

    public void setNeedDistinct(boolean needDistinct) {
        this.needDistinct = needDistinct;
    }
}
