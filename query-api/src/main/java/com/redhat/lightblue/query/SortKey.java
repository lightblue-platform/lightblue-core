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
package com.redhat.lightblue.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class SortKey extends Sort {
    private Path field;
    private boolean desc;

    public SortKey() {}

    public SortKey(Path field,boolean desc) {
        this.field=field;
        this.desc=desc;
    }

    public Path getField() {
        return this.field;
    }

    public void setField(Path argField) {
        this.field = argField;
    }

    public boolean isDesc() {
        return this.desc;
    }

    public void setDesc(boolean argDesc) {
        this.desc = argDesc;
    }

    public JsonNode toJson() {
        return factory.objectNode().put(field.toString(),desc?"$desc":"$asc");
    }

    public static SortKey fromJson(ObjectNode node) {
        if(node.size()!=1)
            throw Error.get(INVALID_SORT,node.toString());
        String field=node.fieldNames().next();
        String dir=node.get(field).asText();
        SortKey sk=new SortKey();
        sk.setField(new Path(field));
        if("$asc".equals(dir))
            sk.setDesc(false);
        else if("$desc".equals(dir))
            sk.setDesc(true);
        else
            throw Error.get(INVALID_SORT,node.toString());
        return sk;
    }
}
