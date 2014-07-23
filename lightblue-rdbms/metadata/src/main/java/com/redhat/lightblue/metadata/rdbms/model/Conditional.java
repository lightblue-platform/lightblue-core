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

public class Conditional extends Expression {
    private If anIf;
    private Then then;
    private List<ElseIf> elseIfList;
    private Else anElse;

    public void setIf(If anIf) {
        this.anIf = anIf;
    }

    public If getIf() {
        return anIf;
    }

    public void setThen(Then then) {
        this.then = then;
    }

    public Then getThen() {
        return then;
    }

    public void setElseIfList(List<ElseIf> elseIfList) {
        this.elseIfList = elseIfList;
    }

    public List<ElseIf> getElseIfList() {
        return elseIfList;
    }

    public void setElse(Else anElse) {
        this.anElse = anElse;
    }

    public Else getElse() {
        return anElse;
    }

    @Override
    public <T> void convert(MetadataParser<T> p, Object expressionsNode) {
        if (anIf == null) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No $if informed");
        }
        if (then == null) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No $then informed");
        }
        T eT = p.newNode();
        T iT = p.newNode();

        anIf.convert(p, null, iT);
        p.putObject(eT, "$if", iT);

        then.convert(p, null, eT);

        if (elseIfList != null && !elseIfList.isEmpty()) {
            Object arri = p.newArrayField(eT, "$elseIf");
            for (ElseIf e : elseIfList) {
                e.convert(p, arri, null);
            }
        }

        if (anElse != null) {
            anElse.convert(p, null, eT);
        }

        p.addObjectToArray(expressionsNode, eT);
    }
}
