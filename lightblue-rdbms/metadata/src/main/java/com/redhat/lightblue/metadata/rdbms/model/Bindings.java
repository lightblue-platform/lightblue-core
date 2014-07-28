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
import com.redhat.lightblue.metadata.rdbms.util.RDBMSMetadataConstants;
import java.util.List;

public class Bindings implements RootConverter {
    private List<InOut> inList;
    private List<InOut> outList;
    
    @Override
    public <T> void convert(MetadataParser<T> p, T parent) {
        boolean bIn = this.getInList() == null || this.getInList().isEmpty();
        boolean bOut = this.getOutList() == null || this.getOutList().isEmpty();
        if (bIn && bOut) {
            throw com.redhat.lightblue.util.Error.get(RDBMSMetadataConstants.ERR_FIELD_REQUIRED, "No fields found for binding");
        }
        T bT = p.newNode();
        if (!bIn) {
            Object arri = p.newArrayField(bT, "in");
            for (InOut x : this.getInList()) {
                x.convert(p, arri, bT);
            }
        }
        if (!bOut) {
            Object arro = p.newArrayField(bT, "out");
            for (InOut x : this.getOutList()) {
                x.convert(p, arro, bT);
            }
        }
        p.putObject(parent, "bindings", bT);
    }

    public void setInList(List<InOut> inList) {
        this.inList = inList;
    }

    public List<InOut> getInList() {
        return inList;
    }

    public void setOutList(List<InOut> outList) {
        this.outList = outList;
    }

    public List<InOut> getOutList() {
        return outList;
    }
}
