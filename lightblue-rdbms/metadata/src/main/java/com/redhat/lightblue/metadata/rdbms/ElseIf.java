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

import java.util.ArrayList;
import java.util.List;

public class ElseIf implements ComplexConverter {

    private If anIf;
    private Then then;

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

    @Override
    public <T> void convert(MetadataParser<T> p, Object lastArrayNode, T node) {
        if(anIf == null){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No if informed");
        }
        if(then == null){
            throw com.redhat.lightblue.util.Error.get(RDBMSConstants.ERR_FIELD_REQ, "No then informed");
        }
        T eT = p.newNode();

        T iT = p.newNode();
        p.putObject(eT, "$if", iT);

        anIf.convert(p,null,iT);
        then.convert(p,null,eT); //it already add $then

        p.addObjectToArray(lastArrayNode,eT);
    }
}
