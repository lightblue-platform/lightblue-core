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
package com.redhat.lightblue.metadata.rdbms;

import com.redhat.lightblue.metadata.parser.MetadataParser;

import java.util.List;

public class Then implements ComplexConverter{

    private String loopOperator;
    private List<Expression> expressions;

    public void setLoopOperator(String loopOperator) {
        if(LoopOperators.check(loopOperator)){
            throw new IllegalStateException("Not a valid loop operator '" +loopOperator+"'. Valid Operators:"+ LoopOperators.getValues());
        }
        this.loopOperator = loopOperator;
    }

    public String getLoopOperator() {
        return loopOperator;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public String getName(){
        return "$then";
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if( (loopOperator == null || loopOperator.isEmpty()) && (expressions == null || expressions.isEmpty()) ){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No fields informed for "+getName());
        }
        if(loopOperator != null) {
            p.putString(node,getName(),loopOperator);
        }else{
            Object arri = p.newArrayField(node, getName());
            for(Expression s : expressions){
                s.convert(p,arri);
            }
        }

    }
}
