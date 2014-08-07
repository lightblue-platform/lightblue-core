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

/**
 *
 * @author lcestari
 */
public class ProjectionMapping implements SimpleConverter {
    private String column;
    private String field;
    private String sort;
    
    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        if(column == null || column.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing column field");  
        }
        if(field == null || field.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing 'field' field");  
        }
        p.putString(eT, "column", column);
        p.putString(eT, "field", field);
        
        if(sort == null && sort.isEmpty()){
           p.putString(eT, "sort", sort);
        }
        
        p.addObjectToArray(expressionsNode, eT);
    }

    public <T> void parse(MetadataParser<T> p, T t) {
        String c = p.getStringProperty(t, "column");
        String f = p.getStringProperty(t, "field");
        String s = p.getStringProperty(t, "sort");
        
        if(c == null || c.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing column field");  
        }
        
        if(f == null || f.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing 'field' field");  
        }
        
        column= c;
        field = f;
        sort  = s;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
    
}
