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
import com.redhat.lightblue.metadata.rdbms.parser.RDBMSMetadataConstants;

import java.util.List;

public class For extends Expression {
    private int loopTimes;
    private String loopCounterVariableName;
    private List<Expression> expressions;

    public void setLoopTimes(int loopTimes) {
        this.loopTimes = loopTimes;
    }

    public int getLoopTimes() {
        return loopTimes;
    }

    public void setLoopCounterVariableName(String loopCounterVariableName) {
        this.loopCounterVariableName = loopCounterVariableName;
    }

    public String getLoopCounterVariableName() {
        return loopCounterVariableName;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        if (loopTimes == 0 || loopTimes < 0) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No loopTimes informed");
        }
        if (loopCounterVariableName == null || loopCounterVariableName.length() == 0) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No loopCounterVariableName informed");
        }
        if (expressions == null || expressions.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No $for's expressions informed");
        }
        T eT = p.newNode();
        p.putString(eT, "loopTimes", Integer.toString(loopTimes));
        p.putString(eT, "loopCounterVariableName", loopCounterVariableName);
        Object o = p.newArrayField(eT, "expressions");
        for (Expression expression : expressions) {
            expression.convert(p, o);
        }
        T s = p.newNode();
        p.putObject(s, "$for", eT);

        p.addObjectToArray(expressionsNode, s);
    }
}
