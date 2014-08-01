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
import com.redhat.lightblue.metadata.rdbms.converter.RootConverter;
import com.redhat.lightblue.metadata.rdbms.enums.LightblueOperators;
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.List;

public class Operation implements RootConverter {
    private String name;
    private List<Expression> expressionList;
    private Bindings bindings;
    
    @Override
    public <T> void convert(MetadataParser<T> p, T parent) {
        if(this.getName() == null){
          throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Operation malformated");  
        }
        
        T oT = p.newNode();
        if (this.getBindings() != null) {
            this.getBindings().convert(p, oT);
        }
        Object expressions = p.newArrayField(oT, "expressions");
        convertExpressions(p, this.getExpressionList(), expressions);
        p.putObject(parent, this.getName(), oT);
    }
    
    public <T> void convertExpressions(MetadataParser<T> p, List<Expression> expressionList, Object expressions) {
        if (expressionList == null || expressionList.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "Expressions not informed");
        }
        for (Expression expression : expressionList) {
            expression.convert(p, expressions);
        }
    }

    public void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void setBindings(Bindings bindings) {
        this.bindings = bindings;
    }

    public Bindings getBindings() {
        return bindings;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!LightblueOperators.check(name)) {
            throw new IllegalStateException("Not a valid operation name '" + name + "'. Valid LightblueOperators:" + LightblueOperators.getValues());
        }
        this.name = name;
    }
}
