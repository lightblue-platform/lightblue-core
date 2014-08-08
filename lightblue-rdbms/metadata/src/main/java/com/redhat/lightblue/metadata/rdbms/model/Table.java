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
public class Table  implements SimpleConverter {
    private String name;
    private String alias;

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        T eT = p.newNode();
        if(name == null || name.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing name field");  
        }
        p.putString(eT, "name", name);
        
        if(alias != null && !alias.isEmpty()){
           p.putString(eT, "alias", alias);
        }
        
        p.addObjectToArray(expressionsNode, eT);
    }

    public <T> void parse(MetadataParser<T> p, T t) {
        String n = p.getStringProperty(t, "name");
        String a = p.getStringProperty(t, "alias");
        
        if(n == null || n.isEmpty()){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Missing name field");  
        }
        
        name  = n;
        alias = a;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
