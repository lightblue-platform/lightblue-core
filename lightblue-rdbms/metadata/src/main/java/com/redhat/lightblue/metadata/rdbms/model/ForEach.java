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
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import com.redhat.lightblue.util.Path;

import java.util.List;

public class ForEach extends Expression {
    private Path iterateOverField;
    private List<Expression> expressions;

    public void setIterateOverField(Path iterateOverField) {
        this.iterateOverField = iterateOverField;
    }

    public Path getIterateOverField() {
        return iterateOverField;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        if (iterateOverField == null || iterateOverField.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No $foreach's iterateOverField informed");
        }
        if (expressions == null || expressions.isEmpty()) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No $foreach's expressions informed");
        }
        T eT = p.newNode();
        p.putString(eT, "iterateOverField", iterateOverField.toString());
        Object o = p.newArrayField(eT, "expressions");
        for (Expression expression : expressions) {
            expression.convert(p, o);
        }
        T s = p.newNode();
        p.putObject(s, "$foreach", eT);

        p.addObjectToArray(expressionsNode, s);
    }
}
